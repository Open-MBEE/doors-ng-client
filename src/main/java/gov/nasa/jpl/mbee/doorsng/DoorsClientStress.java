package gov.nasa.jpl.mbee.doorsng;
/*******************************************************************************
 * Copyright (c) 2012, 2014 IBM Corporation.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 *
 *  The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 *  and the Eclipse Distribution License is available at
 *  http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *  Contributors:
 *
 *     Michael Fiedler     - initial API and implementation
 *     Gabriel Ruelas      - Fix handling of Rich text, include parsing extended properties
 *******************************************************************************/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import net.oauth.OAuthException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.http.HttpHeaders;
import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.OslcClient;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;
import org.eclipse.lyo.client.oslc.resources.OslcQuery;
import org.eclipse.lyo.client.oslc.resources.OslcQueryParameters;
import org.eclipse.lyo.client.oslc.resources.OslcQueryResult;
import org.eclipse.lyo.client.oslc.resources.Requirement;
import org.eclipse.lyo.client.oslc.resources.RequirementCollection;
import org.eclipse.lyo.client.oslc.resources.RmConstants;
import org.eclipse.lyo.client.oslc.resources.RmUtil;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;

/**
 * Samples of logging in to Rational Requirements Composer and running OSLC
 * operations
 *
 *
 * - run an OLSC Requirement query and retrieve OSLC Requirements and
 * de-serialize them as Java objects - TODO: Add more requirement sample
 * scenarios
 *
 */
public class DoorsClientStress {

    private static final Logger logger                           = Logger.getLogger(DoorsClientStress.class.getName());

    // Following is a workaround for primaryText issue in DNG ( it is
    // PrimaryText instead of primaryText
    private static final QName  PROPERTY_PRIMARY_TEXT_WORKAROUND = new QName(RmConstants.JAZZ_RM_NAMESPACE,
            "PrimaryText");

    /**
     * Login to the RRC server and perform some OSLC actions
     * 
     * @param args
     * @throws ParseException
     */
    public static void main(String[] args) throws ParseException {

        Options options = new Options();

        options.addOption("url", true, "url");
        options.addOption("user", true, "user ID");
        options.addOption("password", true, "password");
        options.addOption("project", true, "project area");

        CommandLineParser cliParser = new GnuParser();

        // Parse the command line
        CommandLine cmd = cliParser.parse(options, args);

        if (!validateOptions(cmd)) {
            logger.severe(
                    "Syntax:  java <class_name> -url https://<server>:port/<context>/ -user <user> -password <password> -project \"<project_area>\"");
            logger.severe(
                    "Example: java RRCFormSample -url https://exmple.com:9443/rm -user ADMIN -password ADMIN -project \"JKE Banking (Requirements Management)\"");
            return;
        }

        String webContextUrl = cmd.getOptionValue("url");
        String user = cmd.getOptionValue("user");
        String password = cmd.getOptionValue("password");
        String projectArea = cmd.getOptionValue("project");

        boolean initSuccess = DoorsNgUtils.init(webContextUrl, user, password, projectArea);
        if (!initSuccess)
            return;

        try {
            JazzRootServicesHelper helper = DoorsNgUtils.getHelper();
            JazzFormAuthClient client = DoorsNgUtils.getClient();
            ResourceShape featureInstanceShape = DoorsNgUtils.getFeatureInstanceShape();
            ResourceShape collectionInstanceShape = DoorsNgUtils.getCollectionInstanceShape();
            String requirementFactory = DoorsNgUtils.getRequirementFactory();
            String queryCapability = DoorsNgUtils.getQueryCapability();

            Date start;
            Date end;
            int nmax = 1;
            String msgFormat = "creating %d requirements: %d ms (%f req/s)";

            for (int jj = 0; jj < 5; jj++) {
                start = new Date();
                for (int ii = 0; ii < nmax; ii++) {
                    createRequirement(featureInstanceShape, requirementFactory, client);
                }
                end = new Date();
                long duration = end.getTime() - start.getTime();
                System.out.println(String.format(msgFormat, nmax, duration, nmax * 1000. / duration));

                nmax = (int) Math.pow(10, jj);
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static void createRequirement(ResourceShape featureInstanceShape, String requirementFactory,
            JazzFormAuthClient client) throws URISyntaxException, IOException, OAuthException {
        CustomRequirement requirement = null;

        if ((featureInstanceShape != null) && (requirementFactory != null)) {
            // Create REQ01
            requirement = DoorsNgUtils.getNewRequirement("Req01");

            requirement.setDescription("Created By EclipseLyo");
            requirement.addImplementedBy(new Link(new URI("http://google.com"), "Link in REQ01"));
            // Create the Requirement
            ClientResponse creationResponse = client.createResource(requirementFactory, requirement,
                    OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);
            String url = creationResponse.getHeaders().getFirst(HttpHeaders.LOCATION);
            creationResponse.consumeContent();
        }

    }

    private static void processPagedQueryResults(OslcQueryResult result, OslcClient client, boolean asJavaObjects) {
        int page = 1;
        // For now, just show first 5 pages
        do {
            System.out.println("\nPage " + page + ":\n");
            processCurrentPage(result, client, asJavaObjects);
            if (result.hasNext() && page < 5) {
                result = result.next();
                page++;
            } else {
                break;
            }
        } while (true);
    }

    private static void processCurrentPage(OslcQueryResult result, OslcClient client, boolean asJavaObjects) {

        for (String resultsUrl : result.getMembersUrls()) {
            System.out.println(resultsUrl);

            ClientResponse response = null;
            try {

                // Get a single artifact by its URL
                response = client.getResource(resultsUrl, OSLCConstants.CT_RDF);

                if (response != null) {
                    // De-serialize it as a Java object
                    if (asJavaObjects) {
                        Requirement req = response.getEntity(Requirement.class);
                        // printRequirementInfo(req); //print a few attributes
                        System.out.println(String.format("%s: %s", req.getIdentifier(), req.getTitle()));
                    } else {

                        // Just print the raw RDF/XML (or process the XML as
                        // desired)
                        processRawResponse(response);

                    }
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Unable to process artfiact at url: " + resultsUrl, e);
            }

        }

    }

    private static void processRawResponse(ClientResponse response) throws IOException {
        InputStream is = response.getEntity(InputStream.class);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println();
        response.consumeContent();
    }

    private static boolean validateOptions(CommandLine cmd) {
        boolean isValid = true;

        if (!(cmd.hasOption("url") && cmd.hasOption("user") && cmd.hasOption("password") && cmd.hasOption("project"))) {

            isValid = false;
        }
        return isValid;
    }

}
