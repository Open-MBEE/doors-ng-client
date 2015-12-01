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
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.ws.rs.core.Response.Status;

import net.oauth.OAuthException;

import org.apache.http.HttpStatus;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.protocol.HttpContext;
import org.apache.wink.client.ClientConfig;
import org.apache.wink.client.ClientResponse;
import org.apache.wink.client.RestClient;
import org.apache.wink.client.httpclient.ApacheHttpClientConfig;
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
            properties.load(DoorsClient.class.getResourceAsStream("/doors.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static final QName PROPERTY_PRIMARY_TEXT_WORKAROUND = new QName(RmConstants.JAZZ_RM_NAMESPACE, "PrimaryText");
    private static final String SYSMLID_ID = "__TU3sYHCEeWxYp5ZPr3Qqg";

    private static final String webContextUrl = properties.getProperty("url");
    private static final String user = properties.getProperty("service_account");
    private static final String password = properties.getProperty("service_password");

    private static JazzFormAuthClient client;
    private static JazzRootServicesHelper helper;
    private static String requirementFactory;
    private static String requirementCollectionFactory;
    private static String queryCapability;
    private static String folderQuery;
    private static String folderFactory;
    private static ResourceShape featureInstanceShape;
    private static ResourceShape collectionInstanceShape;

    public DoorsClient(String projectArea) {
        Boolean initSuccess = DoorsNgUtils.init(webContextUrl, user, password, projectArea);
        if (!initSuccess) {
            return;
        }

        try {
            client = DoorsNgUtils.getClient();
            helper = DoorsNgUtils.getHelper();
            requirementFactory = DoorsNgUtils.getRequirementFactory();
            requirementCollectionFactory = DoorsNgUtils.getRequirementCollectionFactory();
            queryCapability = DoorsNgUtils.getQueryCapability();
            folderQuery = DoorsNgUtils.getFolderQuery();
            folderFactory = DoorsNgUtils.getFolderFactory();
            featureInstanceShape = DoorsNgUtils.getFeatureInstanceShape();
            collectionInstanceShape = DoorsNgUtils.getCollectionInstanceShape();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public Requirement getRequirement(String sysmlid) {

        Map<String, String> params = new HashMap<>();
        params.put("sysmlid", sysmlid);

        OslcQueryResult result = getQuery(params);
        Requirement[] reqs = processRequirements(result);

        return reqs.length > 0 ? reqs[reqs.length - 1] : new Requirement();

    }

    public Requirement[] getRequirements() {

        Map<String, String> params = new HashMap<>();

        OslcQueryResult result = getQuery(params);
        Requirement[] reqs = processRequirements(result);

        return reqs;

    }

    public RequirementCollection getRequirementCollection(String title) {

        Map<String, String> params = new HashMap<>();
        params.put("title", title);

        OslcQueryResult result = getQuery(params);
        RequirementCollection[] cols = processRequirementCollections(result);

        return cols.length > 0 ? cols[cols.length - 1] : new RequirementCollection();

    }

    public RequirementCollection[] getRequirementCollections() {

        Map<String, String> params = new HashMap<>();

        OslcQueryResult result = getQuery(params);
        RequirementCollection[] cols = processRequirementCollections(result);

        return cols;

    }

    public String createUpdate(Requirement requirement) {

        Requirement check = getRequirement(requirement.getSysmlid());
        ClientResponse response;
        Integer status = null;

        requirement.setInstanceShape(featureInstanceShape.getAbout());
        System.out.println(check.getExtendedProperties().get(RmConstants.PROPERTY_PARENT_FOLDER));

        if (requirement.getParent() != null) {
            try {
                requirement.getExtendedProperties().put(RmConstants.PROPERTY_PARENT_FOLDER, new URI(requirement.getParent()));
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
        System.out.println(requirement.getExtendedProperties().get(RmConstants.PROPERTY_PARENT_FOLDER));

        try {
            if (check.getResourceUrl() == null) {
                response = client.createResource(requirementFactory, requirement, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);
                if(response.getStatusCode() == HttpStatus.SC_CREATED) {
                    return response.getHeaders().getFirst(HttpHeaders.LOCATION);
                }
            } else {
                response = client.updateResource(check.getResourceUrl(), requirement, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML, check.getEtag());
                if(response.getStatusCode() == HttpStatus.SC_OK) {
                    return check.getResourceUrl();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return null;

    }

    public String createUpdate(RequirementCollection collection) {

        RequirementCollection check = getRequirementCollection(collection.getTitle());
        ClientResponse response;
        Integer status = null;

        collection.setInstanceShape(collectionInstanceShape.getAbout());

        try {
            collection.clearUses();
            for (String sysmlid : collection.getSysmlids()) {
                Requirement req = getRequirement(sysmlid);
                collection.addUses(new URI(req.getResourceUrl()));
            }
            if (collection.getResourceUrl() == null) {

                response = client.createResource(requirementCollectionFactory, collection, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);
                if(response.getStatusCode() == HttpStatus.SC_CREATED) {
                    return response.getHeaders().getFirst(HttpHeaders.LOCATION);
                }
            } else {
                response = client.updateResource(check.getResourceUrl(), collection, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML, check.getEtag());
                if(response.getStatusCode() == HttpStatus.SC_OK) {
                    return check.getResourceUrl();
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return null;

    }

    public Boolean delete(Requirement requirement) {

        Requirement check = getRequirement(requirement.getSysmlid());

        try {
            if (check.getIdentifier() != null) {
                ClientResponse delres = client.deleteResource(check.getResourceUrl());
                if(delres.getStatusCode() == HttpStatus.SC_OK) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return false;

    }

    public Boolean delete(RequirementCollection collection) {

        RequirementCollection check = getRequirementCollection(collection.getIdentifier());

        try {
            if (collection.getIdentifier() != null) {
                ClientResponse delres = client.deleteResource(check.getResourceUrl());
                if(delres.getStatusCode() == HttpStatus.SC_OK) {
                    return true;
                }

            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return false;

    }

    public String createFolder(String name, String description) {

        String xml = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:oslc=\"http://open-services.net/ns/core\" xmlns:nav=\"http://jazz.net/ns/rm/navigation\" xmlns:calm=\"http://jazz.net/xmlns/prod/jazz/calm/1.0/\"><nav:folder rdf:about=\"\"><dcterms:title>" + name + "</dcterms:title> <dcterms:description>" + description + "</dcterms:description><nav:parent rdf:resource=\"" + folderQuery + "\"/></nav:folder></rdf:RDF>";

        try {
            ClientResponse response = client.createResource(folderFactory, xml, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);
            if(response.getStatusCode() == HttpStatus.SC_CREATED || response.getStatusCode() == HttpStatus.SC_OK) {
                return response.getHeaders().getFirst(HttpHeaders.LOCATION);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
        return null;
    }

    private OslcQueryResult getQuery(Map<String, String> params) {

        OslcQueryParameters queryParams = new OslcQueryParameters();
        String prefix = "";
        String where = "";

        if (params.get("sysmlid") != null) {
            prefix = "rm_property=<https://doors-ng-uat.jpl.nasa.gov:9443/rm/types/>";
            where = String.format("rm_property:%s=\"%s\"", SYSMLID_ID, params.get("sysmlid"));
        } else if (params.get("name") != null) {
            prefix = "dcterms=<http://purl.org/dc/terms/>";
            where = String.format("dcterms:title=\"%s\"", params.get("name"));
        }

        queryParams.setPrefix(prefix);
        queryParams.setWhere(where);
        queryParams.setSelect("*");

        OslcQuery query = new OslcQuery(client, queryCapability, 200, queryParams);
        OslcQueryResult result = query.submit();

        return result;

    }

    private static Requirement[] processRequirements(OslcQueryResult result) {

        Set<Requirement> req = new HashSet<Requirement>();

        for (String resultsUrl : result.getMembersUrls()) {
            ClientResponse response = null;
            try {
                response = client.getResource(resultsUrl, OSLCConstants.CT_RDF);

                if(response.getStatusCode() == HttpStatus.SC_OK) {
                    Requirement res = response.getEntity(Requirement.class);
                    res.setResourceUrl(resultsUrl);
                    res.setEtag(response.getHeaders().getFirst(OSLCConstants.ETAG));
                    req.add(res);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        }

        return req.toArray(new Requirement[req.size()]);

    }

    private static RequirementCollection[] processRequirementCollections(OslcQueryResult result) {

        Set<RequirementCollection> req = new HashSet<RequirementCollection>();

        for (String resultsUrl : result.getMembersUrls()) {
            ClientResponse response = null;
            try {
                response = client.getResource(resultsUrl, OSLCConstants.CT_RDF);

                if(response.getStatusCode() == HttpStatus.SC_OK) {
                    RequirementCollection res = response.getEntity(RequirementCollection.class);
                    res.setResourceUrl(resultsUrl);
                    res.setEtag(response.getHeaders().getFirst(OSLCConstants.ETAG));
                    req.add(res);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }

        }

        return req.toArray(new RequirementCollection[req.size()]);

    }

    private static void processRawResponse(ClientResponse response) throws IOException {
        InputStream is = response.getEntity(InputStream.class);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = in.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println();
    }
}
