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
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import net.oauth.OAuthException;

import org.apache.http.HttpHeaders;
import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.OslcClient;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;
import org.eclipse.lyo.client.oslc.resources.OslcQuery;
import org.eclipse.lyo.client.oslc.resources.OslcQueryParameters;
import org.eclipse.lyo.client.oslc.resources.OslcQueryResult;
import org.eclipse.lyo.client.oslc.resources.RmConstants;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
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
public class DoorsClient {

    private static final Logger logger = Logger.getLogger(DoorsClient.class.getName());
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(DoorsClient.class.getResourceAsStream("/user.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Following is a workaround for primaryText issue in DNG ( it is
    // PrimaryText instead of primaryText
    private static final QName PROPERTY_PRIMARY_TEXT_WORKAROUND = new QName(RmConstants.JAZZ_RM_NAMESPACE, "PrimaryText");
    private static final String SYSMLID_ID = "__TU3sYHCEeWxYp5ZPr3Qqg";

    private static JazzFormAuthClient client;
    private static JazzRootServicesHelper helper;
    private static String requirementFactory;
    private static String requirementCollectionFactory;
    private static String queryCapability;

    public static void main(String[] args) throws Exception {
        execute("Test Project");
    }

    /**
     * Login to the RRC server and perform some OSLC actions
     *
     * @param args
     * @throws ParseException
     */
    public static void execute(String projectArea) throws Exception {

        String webContextUrl = properties.getProperty("url");
        String user = properties.getProperty("service_account");
        String password = properties.getProperty("service_password");

        boolean initSuccess = DoorsNgUtils.init(webContextUrl, user, password, projectArea);
        if (!initSuccess) {
            return;
        }

        try {
            client = DoorsNgUtils.getClient();
            helper = DoorsNgUtils.getHelper();
            requirementFactory = DoorsNgUtils.getRequirementFactory();
            requirementCollectionFactory = DoorsNgUtils.getRequirementCollectionFactory();
            queryCapability = DoorsNgUtils.getQueryCapability();

            ResourceShape featureInstanceShape = DoorsNgUtils.getFeatureInstanceShape();
            ResourceShape collectionInstanceShape = DoorsNgUtils.getCollectionInstanceShape();

            // Requirement requirement = new Requirement();
            // requirement.setTitle("Req07");
            // requirement.setDescription("Created By EclipseLyo");
            // requirement.setSysmlId("12345");
            // requirement.addImplementedBy(new Link(new
            // URI("http://google.com"), "Link in REQ01"));
            // createUpdateRequirement(featureInstanceShape, requirementFactory,
            // client, requirement);

            // getRequirements();
            Requirement req = getRequirement("12345");
            System.out.println(String.format("Title: %s\nSysmlId: %s\nDescription: %s\nCreated: %s\nModified: %s\nCreated By: %s", req.getTitle(), req.getSysmlId(), req.getDescription(), req.getCreated(), req.getModified(), Arrays.toString(req.getCreators())));
            deleteRequirement(req);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static Requirement getRequirement(String sysmlid) {

        OslcQueryParameters queryParams = new OslcQueryParameters();
        queryParams.setPrefix("rm_property=<https://doors-ng-uat.jpl.nasa.gov:9443/rm/types/>");
        String where = String.format("rm_property:%s=\"%s\"", SYSMLID_ID, sysmlid);
        queryParams.setWhere(where);
        OslcQuery query = new OslcQuery(client, queryCapability, 1, queryParams);
        OslcQueryResult result = query.submit();
        Requirement[] reqs = processResults(result);

        return reqs[0];

    }

    private static Requirement[] getRequirements() {

        OslcQueryParameters queryParams = new OslcQueryParameters();
        OslcQuery query = new OslcQuery(client, queryCapability, 10, queryParams);
        OslcQueryResult result = query.submit();
        Requirement[] reqs = processResults(result);

        return reqs;

    }

    private static Boolean createUpdateRequirement(ResourceShape featureInstanceShape, Requirement requirement) throws URISyntaxException, IOException, OAuthException {

        if ((featureInstanceShape != null) && (requirement != null)) {
            ClientResponse response;
            String url = null;
            requirement.setInstanceShape(featureInstanceShape.getAbout());
            if (requirement.getIdentifier() == null) {
                response = client.createResource(requirementFactory, requirement, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);
            } else {
                response = client.updateResource(requirementFactory, requirement, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);
            }
            url = response.getHeaders().getFirst(HttpHeaders.LOCATION);
            response.consumeContent();
            if (url != null) {

                return true;

            }
        }

        return false;

    }

    private static Boolean createUpdateRequirementCollections(RequirementCollection collection) throws URISyntaxException, IOException, OAuthException {

        if (collection != null) {
            ClientResponse response;
            if (collection.getIdentifier() == null) {
                response = client.createResource(requirementCollectionFactory, collection, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);
            } else {
                response = client.updateResource(requirementCollectionFactory, collection, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);
            }
            String url = response.getHeaders().getFirst(HttpHeaders.LOCATION);
            response.consumeContent();
            if (url != null) {

                return true;

            }
        }

        return false;

    }

    private static Boolean deleteRequirement(Requirement requirement) {
        if (requirement.getIdentifier() != null) {
            System.out.println(requirement.getSubjects()[0]);
        }
        return false;
    }

    private static Requirement[] processResults(OslcQueryResult result) {

        Set<Requirement> req = new HashSet<Requirement>();

        do {
            for (String resultsUrl : result.getMembersUrls()) {
                System.out.println(resultsUrl);
                ClientResponse response = null;
                try {
                    response = client.getResource(resultsUrl, OSLCConstants.CT_RDF);

                    if (response != null) {
                        req.add(response.getEntity(Requirement.class));
                    }
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Unable to process artfiact at url: " + resultsUrl, e);
                }

            }
            if (result.hasNext()) {
                result = result.next();
            } else {
                break;
            }
        } while (true);

        return req.toArray(new Requirement[req.size()]);
    }
}
