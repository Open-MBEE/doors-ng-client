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
import java.net.URLDecoder;

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
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.eclipse.lyo.client.exception.RootServicesException;
import org.eclipse.lyo.client.exception.ResourceNotFoundException;
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
    private static Map<String, URI> projectProperties = new HashMap<String, URI>();

    private static final String webContextUrl = properties.getProperty("url");
    private static final String user = properties.getProperty("service_account");
    private static final String password = properties.getProperty("service_password");

    public DoorsClient(String projectArea) throws Exception {

        helper = new JazzRootServicesHelper(webContextUrl, OSLCConstants.OSLC_RM_V2);
        String authUrl = webContextUrl.replaceFirst("/rm", "/jts");
        client = helper.initFormClient(user, password, authUrl);

        if (client.login() == HttpStatus.SC_OK) {
            String catalogUrl = helper.getCatalogUrl();
            String serviceProviderUrl = client.lookupServiceProviderUrl(catalogUrl, projectArea);

            setResources(serviceProviderUrl);
       }

    }

    private void setResources(String serviceProviderUrl) {
        try {

            URI serviceProvider = new URI(serviceProviderUrl);
            String[] serviceProviderPath = serviceProvider.getPath().split("/");

            queryCapability = URLDecoder.decode(client.lookupQueryCapability(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_TYPE));
            requirementFactory = URLDecoder.decode(client.lookupCreationFactory(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_TYPE));
            requirementCollectionFactory = URLDecoder.decode(client.lookupCreationFactory(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_COLLECTION_TYPE));
            rootFolder = URLDecoder.decode(serviceProvider.getScheme() + "://" + serviceProvider.getAuthority() + "/rm/folders/" + serviceProviderPath[serviceProviderPath.length - 2]);
            folderQuery = URLDecoder.decode(serviceProvider.getScheme() + "://" + serviceProvider.getAuthority() + "/rm/folders?oslc.where=public_rm:parent=" + serviceProvider.getScheme() + "://" + serviceProvider.getAuthority() + "/rm/folders/" +  serviceProviderPath[serviceProviderPath.length - 2]);
            folderFactory = URLDecoder.decode(serviceProvider.getScheme() + "://" + serviceProvider.getAuthority() + "/rm/folders/?projectUrl=" + serviceProvider.getScheme() + "://" + serviceProvider.getAuthority() + "/jts/process/project-areas/" + serviceProviderPath[serviceProviderPath.length - 2]);

            featureInstanceShape = RmUtil.lookupRequirementsInstanceShapes(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_TYPE, client, "Requirement");
            collectionInstanceShape = RmUtil.lookupRequirementsInstanceShapes(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_COLLECTION_TYPE, client, "Requirement Collection");

        } catch (Exception e) {
            return;
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

    public String create(Requirement requirement) {

        ClientResponse response;

        requirement.setInstanceShape(featureInstanceShape.getAbout());

        try {

            response = client.createResource(requirementFactory, requirement, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);
            response.consumeContent();

            if(response.getStatusCode() == HttpStatus.SC_CREATED) {

                return response.getHeaders().getFirst(HttpHeaders.LOCATION);

            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return null;

    }

    public String update(Requirement requirement) {

        ClientResponse response;
        Integer status = null;

        requirement.setInstanceShape(featureInstanceShape.getAbout());
        Requirement check = getRequirement(requirement.getResourceUrl());

        try {

            response = client.updateResource(requirement.getResourceUrl(), requirement, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML, check.getEtag());
            response.consumeContent();

            if(response.getStatusCode() == HttpStatus.SC_OK) {
                return requirement.getResourceUrl();

            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return null;

    }

    public RequirementCollection getRequirementCollection(String resourceUrl) {

        ClientResponse response = null;
        try {

            response = client.getResource(resourceUrl, OSLCConstants.CT_RDF);

            if(response.getStatusCode() == HttpStatus.SC_OK) {
                RequirementCollection collection = response.getEntity(RequirementCollection.class);
                collection.setEtag(response.getHeaders().getFirst(OSLCConstants.ETAG));

                return collection;

            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return new RequirementCollection();

    }

    public String create(RequirementCollection collection) {

        ClientResponse response;

        collection.setInstanceShape(collectionInstanceShape.getAbout());

        try {

            response = client.createResource(requirementCollectionFactory, collection, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);
            response.consumeContent();

            if(response.getStatusCode() == HttpStatus.SC_CREATED) {

                return response.getHeaders().getFirst(HttpHeaders.LOCATION);

            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return null;

    }

    public String update(RequirementCollection collection) {

        ClientResponse response;
        Integer status = null;

        collection.setInstanceShape(collectionInstanceShape.getAbout());

        RequirementCollection check = getRequirementCollection(collection.getResourceUrl());

        try {

            response = client.updateResource(collection.getResourceUrl(), collection, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML, check.getEtag());
            response.consumeContent();

            if(response.getStatusCode() == HttpStatus.SC_OK) {

                return collection.getResourceUrl();

            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return null;

    }

    public Folder getFolder(String resourceUrl) {

        ClientResponse response;

        try {

            response = client.getResource(resourceUrl, OSLCConstants.CT_RDF);

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
        ClientResponse response;

        String parentFolder = rootFolder;
        if (folder.getParent() != null) {
            parentFolder = URLDecoder.decode(folder.getParent());
        }

        String xml = "<rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:oslc=\"http://open-services.net/ns/core\" xmlns:nav=\"http://jazz.net/ns/rm/navigation\" xmlns:calm=\"http://jazz.net/xmlns/prod/jazz/calm/1.0/\"><nav:folder rdf:about=\"\"><dcterms:title>" + folder.getTitle() + "</dcterms:title> <dcterms:description>" + folder.getDescription() + "</dcterms:description><nav:parent rdf:resource=\"" + parentFolder + "\"/></nav:folder></rdf:RDF>";

        try {

            response = client.createResource(folderFactory, xml, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML);
            response.consumeContent();

            if(response.getStatusCode() == HttpStatus.SC_CREATED || response.getStatusCode() == HttpStatus.SC_OK) {
                return response.getHeaders().getFirst(HttpHeaders.LOCATION);
            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return null;

    }

    public Boolean delete(String resourceUrl) {
        ClientResponse response;

        try {

            response = client.deleteResource(resourceUrl);
            response.consumeContent();

            if(response.getStatusCode() == HttpStatus.SC_OK) {
                return true;
            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return false;

    }

    public URI getField(String name) {
        if(projectProperties.isEmpty()) {
            projectProperties = getFields();
        }

        return projectProperties.get(name);
    }

    public Map<String, URI> getFields() {
        Property[] properties = featureInstanceShape.getProperties();

        for (Property property : properties) {
            projectProperties.put(property.getTitle(), property.getPropertyDefinition());
        }

        return projectProperties;
    }

    private static Requirement[] processRequirements(OslcQueryResult result) {

        Set<Requirement> req = new HashSet<Requirement>();

		do {

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

			if (result.hasNext()) {
				result = result.next();
			} else {
				break;
			}

		} while(true);

        return req.toArray(new Requirement[req.size()]);

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
