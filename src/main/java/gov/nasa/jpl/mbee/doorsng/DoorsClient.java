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
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.QName;

import org.apache.http.HttpStatus;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;
import org.eclipse.lyo.oslc4j.core.model.OslcConstants;
import org.eclipse.lyo.client.exception.RootServicesException;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.OslcClient;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;
import org.eclipse.lyo.client.oslc.resources.OslcQuery;
import org.eclipse.lyo.client.oslc.resources.OslcQueryParameters;
import org.eclipse.lyo.client.oslc.resources.OslcQueryResult;
import org.eclipse.lyo.client.oslc.resources.RmUtil;
import org.eclipse.lyo.client.oslc.resources.RmConstants;

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

    private static JazzFormAuthClient client;
    private static JazzRootServicesHelper helper;
    private static String requirementFactory;
    private static String requirementCollectionFactory;
    private static String queryCapability;
    private static String rootFolder;
    private static String folderQuery;
    private static String folderFactory;
    private static ResourceShape featureInstanceShape;
    private static ResourceShape collectionInstanceShape;

    private static final String webContextUrl = properties.getProperty("url");
    private static final String user = properties.getProperty("service_account");
    private static final String password = properties.getProperty("service_password");

    public DoorsClient(String projectArea) {

        try {

            if (client == null) {
                helper = new JazzRootServicesHelper(webContextUrl, OSLCConstants.OSLC_RM_V2);

                String authUrl = webContextUrl.replaceFirst("/rm", "/jts");
                client = helper.initFormClient(user, password, authUrl);
            }

            if (client.login() == HttpStatus.SC_OK) {
                String catalogUrl = helper.getCatalogUrl();
                String serviceProviderUrl = client.lookupServiceProviderUrl(catalogUrl, projectArea);

                URI serviceProvider = new URI(serviceProviderUrl);
                String[] serviceProviderPath = serviceProvider.getPath().split("/");

                queryCapability = client.lookupQueryCapability(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_TYPE);
                requirementFactory = client.lookupCreationFactory(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_TYPE);
                requirementCollectionFactory = client.lookupCreationFactory(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_COLLECTION_TYPE);
                featureInstanceShape = RmUtil.lookupRequirementsInstanceShapes(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_TYPE, client, "Requirement");
                collectionInstanceShape = RmUtil.lookupRequirementsInstanceShapes(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_COLLECTION_TYPE, client, "Requirement Collection");
                rootFolder = serviceProvider.getScheme() + "://" + serviceProvider.getAuthority() + "/rm/folders/" + serviceProviderPath[serviceProviderPath.length - 2];
                folderFactory = serviceProvider.getScheme() + "://" + serviceProvider.getAuthority() + "/rm/folders/?projectUrl=" + serviceProvider.getScheme() + "://" + serviceProvider.getAuthority() + "/jts/process/project-areas/" + serviceProviderPath[serviceProviderPath.length - 2];
                folderQuery = serviceProvider.getScheme() + "://" + serviceProvider.getAuthority() + "/rm/folders?oslc.where=public_rm:parent=" + serviceProvider.getScheme() + "://" + serviceProvider.getAuthority() + "/rm/folders/" +  serviceProviderPath[serviceProviderPath.length - 2];

            }

        } catch (RootServicesException re) {

            logger.log(Level.SEVERE, "Unable to access the Jazz rootservices document at: " + webContextUrl + "/rootservices", re);

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

    }

    public Requirement getRequirement(String resourceUrl) {

        ClientResponse response = null;

        try {

            response = client.getResource(resourceUrl, OSLCConstants.CT_RDF);

            if(response.getStatusCode() == HttpStatus.SC_OK) {
                Requirement requirement = response.getEntity(Requirement.class);
                requirement.setEtag(response.getHeaders().getFirst(OSLCConstants.ETAG));

                return requirement;

            }
        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return new Requirement();

    }

    public Requirement[] getRequirements() {

        OslcQuery query = new OslcQuery(client, queryCapability, 200, new OslcQueryParameters());
        OslcQueryResult result = query.submit();

        Requirement[] reqs = processRequirements(result);

        return reqs;

    }

    public RequirementCollection getRequirementCollection(String resourceUrl) {

        ClientResponse response = null;
        try {

            response = client.getResource(resourceUrl, OSLCConstants.CT_RDF);

            if(response.getStatusCode() == HttpStatus.SC_OK) {
                RequirementCollection collection = response.getEntity(RequirementCollection.class);

                return collection;

            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return new RequirementCollection();

    }

    public String create(Requirement requirement) {

        ClientResponse response;

        requirement.setInstanceShape(featureInstanceShape.getAbout());

        try {

            response = client.createResource(requirementFactory, requirement, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);
            if(response.getStatusCode() == HttpStatus.SC_CREATED) {

                return response.getHeaders().getFirst(HttpHeaders.LOCATION);

            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return null;

    }

    public String update(Requirement requirement) {

        Requirement check = getRequirement(requirement.getResourceUrl());
        ClientResponse response;
        Integer status = null;

        requirement.setInstanceShape(featureInstanceShape.getAbout());

        try {

            response = client.updateResource(check.getResourceUrl(), requirement, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML, check.getEtag());
            if(response.getStatusCode() == HttpStatus.SC_OK) {

                return check.getResourceUrl();

            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return null;

    }

    public String create(RequirementCollection collection) {

        ClientResponse response;

        collection.setInstanceShape(collectionInstanceShape.getAbout());

        try {

            response = client.createResource(requirementCollectionFactory, collection, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);

            if(response.getStatusCode() == HttpStatus.SC_CREATED) {

                return response.getHeaders().getFirst(HttpHeaders.LOCATION);

            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return null;

    }

    public String update(RequirementCollection collection) {

        RequirementCollection check = getRequirementCollection(collection.getResourceUrl());
        ClientResponse response;
        Integer status = null;

        collection.setInstanceShape(collectionInstanceShape.getAbout());

        try {

            response = client.updateResource(check.getResourceUrl(), collection, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML, check.getEtag());

            if(response.getStatusCode() == HttpStatus.SC_OK) {

                return check.getResourceUrl();

            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return null;

    }

    public Folder getFolder(String folderUrl) {

        ClientResponse response;

        try {

            response = client.getResource(folderUrl, OSLCConstants.CT_RDF);
            if(response.getStatusCode() == HttpStatus.SC_OK) {
                Folder folder = response.getEntity(Folder.class);

                return folder;

            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return new Folder();
    }

    public String create(Folder folder) {

        String parentFolder = rootFolder;
        if (folder.getParent() != null) {
            parentFolder = folder.getParent();
        }

        String xml = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:oslc=\"http://open-services.net/ns/core\" xmlns:nav=\"http://jazz.net/ns/rm/navigation\" xmlns:calm=\"http://jazz.net/xmlns/prod/jazz/calm/1.0/\"><nav:folder rdf:about=\"\"><dcterms:title>" + folder.getTitle() + "</dcterms:title> <dcterms:description>" + folder.getDescription() + "</dcterms:description><nav:parent rdf:resource=\"" + parentFolder + "\"/></nav:folder></rdf:RDF>";

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

    public Boolean delete(String resourceUrl) {

        try {

            ClientResponse delres = client.deleteResource(resourceUrl);
            if(delres.getStatusCode() == HttpStatus.SC_OK) {
                return true;
            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return false;

    }

    private OslcQueryResult getQuery(String id) {

        OslcQueryParameters queryParams = new OslcQueryParameters();

        queryParams.setPrefix("dcterms=<http://purl.org/dc/terms/>");
        queryParams.setWhere(String.format("dcterms:identifier=\"%s\"", id));
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
