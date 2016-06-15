package gov.nasa.jpl.mbee.doorsng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.net.HttpCookie;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.InvalidCredentialsException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.exception.ResourceNotFoundException;
import org.eclipse.lyo.client.oslc.OAuthRedirectException;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.resources.OslcQuery;
import org.eclipse.lyo.client.oslc.resources.OslcQueryParameters;
import org.eclipse.lyo.client.oslc.resources.OslcQueryResult;
import org.eclipse.lyo.client.oslc.resources.RmConstants;
import org.eclipse.lyo.client.oslc.resources.RmUtil;
import org.eclipse.lyo.oslc4j.core.model.OslcMediaType;
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;

import gov.nasa.jpl.mbee.doorsng.model.Requirement;
import gov.nasa.jpl.mbee.doorsng.model.RequirementCollection;
import gov.nasa.jpl.mbee.doorsng.model.Folder;
import gov.nasa.jpl.mbee.doorsng.lib.DoorsOAuthClient;
import gov.nasa.jpl.mbee.doorsng.lib.DoorsFormAuthClient;
import gov.nasa.jpl.mbee.doorsng.lib.DoorsRootServicesHelper;

public class DoorsClient {

    private static final Logger logger = Logger.getLogger(DoorsClient.class.getName());

    private static DoorsFormAuthClient client;
    private static DoorsOAuthClient oclient;
    private static DoorsRootServicesHelper helper;
    private static String requirementFactory;
    private static String requirementCollectionFactory;
    private static String queryCapability;
    private static String folderFactory;

    private static String JSESSIONID;

    private static Map<String, URI> projectProperties = null;
    private static Map<String, String> projectPropertiesDetails = null;

    public static String doorsUrl;
    public static String project;
    public static String projectId;
    public static String rootFolder;

    public DoorsClient(String user, String password, String webContextUrl, String projectArea) throws Exception {

        projectProperties = new HashMap<String, URI>();
        projectPropertiesDetails = new HashMap<String, String>();

        doorsUrl = webContextUrl;

        helper = new DoorsRootServicesHelper(webContextUrl, OSLCConstants.OSLC_RM_V2);
        String authUrl = webContextUrl.replaceFirst("/rm", "/jts");
        client = helper.initFormClient(user, password, authUrl);

        if (client.login() == HttpStatus.SC_OK) {
            JSESSIONID = client.getSessionId();
            if (projectArea != null) {
                setProject(projectArea);
            }
       }

    }

    public DoorsClient(String consumerKey, String consumerSecret, String user, String password, String webContextUrl) throws Exception {

        projectProperties = new HashMap<String, URI>();
        projectPropertiesDetails = new HashMap<String, String>();
        doorsUrl = webContextUrl;
        helper = new DoorsRootServicesHelper(doorsUrl, OSLCConstants.OSLC_RM_V2);
        String authUrl = doorsUrl.replaceFirst("/rm", "/jts");
        oclient = helper.initOAuthClient(consumerKey, consumerSecret);

        if (client != null) {
            try {

                oclient.getResource(doorsUrl,OSLCConstants.CT_RDF);

            } catch (OAuthRedirectException oauthE) {

                validateTokens(oclient, oauthE, consumerKey, consumerSecret, user, password, authUrl);
                ClientResponse response = client.getResource(doorsUrl, OSLCConstants.CT_RDF);
                response.getEntity(InputStream.class).close();

            }
        }

    }

    public void setProject(String projectArea) {
        project = projectArea;
        try {

            setResources(client.lookupServiceProviderUrl(helper.getCatalogUrl(), project));

        } catch (ResourceNotFoundException e) {

            try {

                createProject(projectArea);

            } catch (Exception f) {

                logger.log(Level.SEVERE, "Couldn't create project", f);

            }

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    public String getProject() {
        return client.getProject();
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

        OslcQuery query = new OslcQuery(client, queryCapability);
        OslcQueryResult result = query.submit();

        Requirement[] reqs = processRequirements(result);

        return reqs;

    }

    public Requirement[] queryRequirements(String field, String value) {

        Map<String, String> params = new HashMap<String, String>();
        params.put(field, value);

        OslcQueryResult result = getQuery(params);
        Requirement[] reqs = processRequirements(result);

        return reqs;
    }

    public String create(Requirement requirement) {

        return create(requirement, null);

    }

    public String create(Requirement requirement, String shapeTitle) {

        ClientResponse response;
        ResourceShape shape;

        try {

            if (shapeTitle == null) {
                shape = getShape(OSLCConstants.RM_REQUIREMENT_TYPE, "Requirement");
            } else {
                shape = getShape(OSLCConstants.RM_REQUIREMENT_TYPE, shapeTitle);
            }

            requirement.setInstanceShape(shape.getAbout());

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

        return update(requirement, null);

    }

    public String update(Requirement requirement, String shapeTitle) {

        ClientResponse response;
        ResourceShape shape;

        try {

            if (shapeTitle == null) {
                shape = getShape(OSLCConstants.RM_REQUIREMENT_TYPE, "Requirement");
            } else {
                shape = getShape(OSLCConstants.RM_REQUIREMENT_TYPE, shapeTitle);
            }

            requirement.setInstanceShape(shape.getAbout());

            Requirement check = getRequirement(requirement.getResourceUrl());

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

        return create(collection, null);

    }

    public String create(RequirementCollection collection, String shapeTitle) {

        ClientResponse response;
        ResourceShape shape;

        try {

            if (shapeTitle == null) {
                shape = getShape(OSLCConstants.RM_REQUIREMENT_COLLECTION_TYPE, "Requirement Collection");
            } else {
                shape = getShape(OSLCConstants.RM_REQUIREMENT_COLLECTION_TYPE, shapeTitle);
            }

            collection.setInstanceShape(shape.getAbout());

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

        return update(collection, null);

    }

    public String update(RequirementCollection collection, String shapeTitle) {

        ClientResponse response;
        ResourceShape shape;

        try {

            if (shapeTitle == null) {
                shape = getShape(OSLCConstants.RM_REQUIREMENT_COLLECTION_TYPE, "Requirement Collection");
            } else {
                shape = getShape(OSLCConstants.RM_REQUIREMENT_COLLECTION_TYPE, shapeTitle);
            }

            collection.setInstanceShape(shape.getAbout());

            RequirementCollection check = getRequirementCollection(collection.getResourceUrl());

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
            return processFolderQuery(response);

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return new Folder();
    }

    public Folder[] getFolders() {

        return getFolders(null);
    }

    public Folder[] getFolders(String parentResourceUrl) {

        Set<Folder> folders = new HashSet<Folder>();
        String folderQueryCapability = doorsUrl + "folders";
        String resourceUrl = parentResourceUrl;
        if (resourceUrl == null) {
            resourceUrl = doorsUrl + "folders/" + projectId;
        }

        OslcQueryParameters queryParams = new OslcQueryParameters();
        queryParams.setWhere("public_rm:parent=" + resourceUrl);
        OslcQuery query = new OslcQuery(client, folderQueryCapability, queryParams);
        OslcQueryResult result = query.submit();

        for (String resultsUrl : result.getMembersUrls()) {

            ClientResponse response;

            try {

                response = client.getResource(resultsUrl, OSLCConstants.CT_RDF);
                Folder folder = processFolderQuery(response);
                folders.add(folder);

            } catch (Exception e) {

                logger.log(Level.SEVERE, e.getMessage(), e);

            }
        }

        return folders.toArray(new Folder[folders.size()]);
    }

    public String create(Folder folder) {
        ClientResponse response;

        String parentFolder = rootFolder;
        if (folder.getParent() != null) {
            try {

                parentFolder = URLDecoder.decode(folder.getParent(), "UTF-8");

            } catch (Exception e) {

                logger.log(Level.SEVERE, e.getMessage(), e);

            }
        }

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><rdf:RDF xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"><ns:folder xmlns:ns=\"http://com.ibm.rdm/navigation#\" rdf:about=\"\"><ns:title xmlns:ns=\"http://purl.org/dc/terms/\">" + folder.getTitle() + "</ns:title><ns:description xmlns:ns=\"http://purl.org/dc/terms/\">" + folder.getDescription() + "</ns:description><ns:parent rdf:resource=\"" + parentFolder + "\" xmlns:ns=\"http://com.ibm.rdm/navigation#\"/></ns:folder></rdf:RDF>";

        try {

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("net.jazz.jfs.owning-context", doorsUrl + "/process/project-areas/" + projectId);
            response = client.createResource(folderFactory, xml, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML, headers);
            response.consumeContent();

            if(response.getStatusCode() == HttpStatus.SC_CREATED || response.getStatusCode() == HttpStatus.SC_OK) {
                return response.getHeaders().getFirst(HttpHeaders.LOCATION);
            } else {
                System.out.println("Response: " + response.getMessage());
            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }

        return null;

    }

    public String createProject(String project) throws Exception {
        return createProject(project, "RRSTemplateID");
    }

    public String createProject(String project, String template) throws Exception {
        ClientResponse response;
        String projectFactory = doorsUrl + "/projects";

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><jp:project-area xmlns:jp=\"http://jazz.net/xmlns/prod/jazz/process/0.6/\" xmlns:rm=\"http://www.ibm.com/xmlns/rdm/rdf/\" jp:name=\"" + project + "\" jp:templateId=\"" + template + "\" jp:templateLocale=\"en_US\"><jp:summary></jp:summary><jp:description>NewAutoProjectTest</jp:description><rm:spaceName>AUTOGENRATED</rm:spaceName><rm:spaceDescription>testing auto-creation</rm:spaceDescription><rm:componentName>xxx</rm:componentName><rm:componentDescription>NewAutoProject</rm:componentDescription><rm:spaceUri></rm:spaceUri><rm:defaultConfigurationUri></rm:defaultConfigurationUri><jp:visibility jp:access=\"PROJECT_HIERARCHY\"/></jp:project-area>";

        try {

            Map<String, String> headers = new HashMap<String, String>();
            headers.put("X-Jazz-CSRF-Prevent", JSESSIONID);
            response = client.createResource(projectFactory, xml, OslcMediaType.APPLICATION_RDF_XML, OslcMediaType.APPLICATION_RDF_XML, headers);
            response.consumeContent();

            if(response.getStatusCode() == HttpStatus.SC_CREATED || response.getStatusCode() == HttpStatus.SC_OK) {
                return response.getHeaders().getFirst(HttpHeaders.LOCATION);
            } else {
                System.out.println("Response: " + response.getMessage());
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
        Property[] properties;
        try {

            properties = getShape(OSLCConstants.RM_REQUIREMENT_TYPE, "Requirement").getProperties();
            for (Property property : properties) {
                projectProperties.put(property.getTitle(), property.getPropertyDefinition());
                projectPropertiesDetails.put(property.getTitle(), property.getName());
            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }
        return projectProperties;
    }


    public static Map<String, String> getQueryMap(String query) {
        Map<String, String> map = new HashMap<String, String>();
        String[] params = query.split("&");

        for (String param : params) {
            String name = param.split("=")[0];
            String value = param.split("=")[1];
            map.put(name, value);
        }

        return map;
    }

    public ResourceShape getShape(String oslcResourceType, String requiredInstanceShape) throws Exception {
        return RmUtil.lookupRequirementsInstanceShapes(client.lookupServiceProviderUrl(helper.getCatalogUrl(), project), OSLCConstants.OSLC_RM_V2, oslcResourceType, client, requiredInstanceShape);
    }

    private OslcQueryResult getQuery(Map<String, String> params) {

        if(projectPropertiesDetails.isEmpty()) {
            getFields();
        }

        OslcQueryParameters queryParams = new OslcQueryParameters();
        String prefix = "";
        String where = "";

        if (params.get("name") != null) {
            prefix = "dcterms=<http://purl.org/dc/terms/>";
            where = String.format("dcterms:title=\"%s\"", params.get("name"));
        } else {
            Iterator<Entry<String, String>> it = params.entrySet().iterator();
            if (it.hasNext()) {
                Entry<String, String> pair = it.next();
                prefix = "rm_property=<" + doorsUrl + "/types/>";
                where = String.format("rm_property:%s=\"%s\"", projectPropertiesDetails.get((String) pair.getValue()), (String) pair.getKey());
            }
        }

        queryParams.setPrefix(prefix);
        queryParams.setWhere(where);

        OslcQuery query = new OslcQuery(client, queryCapability, 20, queryParams);
        OslcQueryResult result = query.submit();

        return result;

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

    private static Folder processFolderQuery(ClientResponse response) throws IOException {

        Folder result = new Folder();

        InputStream is = response.getEntity(InputStream.class);
        BufferedReader in = new BufferedReader(new InputStreamReader(is));

        String line = null;

        while ((line = in.readLine()) != null) {
            Matcher rm = Pattern.compile("<nav:folder rdf:about=\"(.*?)\">").matcher(line);
            Matcher tm = Pattern.compile("<dcterms:title>(.*?)</dcterms:title>").matcher(line);
            Matcher pm = Pattern.compile("<nav:parent rdf:resource=\"(.*?)\"/>").matcher(line);
            if (rm.find()) {
                result.setResourceUrl((String) rm.group(1));
            }
            if (tm.find()) {
                result.setTitle((String) tm.group(1));
            }
            if (pm.find()) {
                result.setParent(URI.create(pm.group(1)));
            }
        }

        return result;
    }

    private void setResources(String serviceProviderUrl) {
        try {

            URI serviceProvider = URI.create(serviceProviderUrl);
            String[] serviceProviderPath = serviceProvider.getPath().split("/");

            projectId = serviceProviderPath[serviceProviderPath.length - 2];
            queryCapability = client.lookupQueryCapability(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_TYPE);
            requirementFactory = URLDecoder.decode(client.lookupCreationFactory(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_TYPE), "UTF-8");
            requirementCollectionFactory = URLDecoder.decode(client.lookupCreationFactory(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_COLLECTION_TYPE), "UTF-8");
            rootFolder = doorsUrl + "/folders/" + projectId;
            folderFactory = doorsUrl + "/folders/?projectUrl=" + serviceProvider.getScheme() + "://" + serviceProvider.getAuthority() + "/jts/process/project-areas/" + projectId;

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        }
    }

    private static void validateTokens(DoorsOAuthClient client, OAuthRedirectException oauthE, String consumerKey, String consumerSecret, String user, String password, String authURL) throws Exception {

        String requestToken = oauthE.getAccessor().requestToken;
        String tokenSecret = oauthE.getAccessor().tokenSecret;
        String redirect = oauthE.getRedirectURL() + "?oauth_token=" + requestToken;

        HttpGet request2 = new HttpGet(redirect);
        HttpClientParams.setRedirecting(request2.getParams(), false);
        HttpResponse response = client.getHttpClient().execute(request2);
        EntityUtils.consume(response.getEntity());

        Header location = response.getFirstHeader("Location");
        JSESSIONID = response.getFirstHeader("Set-Cookie").getValue().split("=")[1].split(";")[0];

        HttpGet request3 = new HttpGet(location.getValue());
        HttpClientParams.setRedirecting(request3.getParams(), false);
        response = client.getHttpClient().execute(request3);
        EntityUtils.consume(response.getEntity());

        HttpPost formPost = new HttpPost(authURL + "/j_security_check");
        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("j_username", user));
        nvps.add(new BasicNameValuePair("j_password", password));
        formPost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
        HttpResponse formResponse = client.getHttpClient().execute(formPost);
        EntityUtils.consume(formResponse.getEntity());

        location = formResponse.getFirstHeader("Location");

        HttpGet request4 = new HttpGet(location.getValue());
        HttpClientParams.setRedirecting(request4.getParams(), false);
        response = client.getHttpClient().execute(request4);
        EntityUtils.consume(response.getEntity());

        location = response.getFirstHeader("Location");

        HttpPost formPost2 = new HttpPost(authURL + "/j_security_check");
        formPost2.getParams().setParameter("oauth_token", requestToken);
        formPost2.getParams().setParameter("authorize", "true");
        formPost2.addHeader("Content-Type","application/x-www-form-urlencoded;charset=UTF-8");
        HttpResponse formResponse2 = client.getHttpClient().execute(formPost2);
        EntityUtils.consume(formResponse2.getEntity());

        HttpGet request5 = new HttpGet(location.getValue());
        HttpClientParams.setRedirecting(request5.getParams(), false);
        response = client.getHttpClient().execute(request5);
        EntityUtils.consume(response.getEntity());
    }

    /***
     * DoorsStereotypeProfileGenerator utility class will process the input stream
     * of artifact types and attributes returned by this method
     */
    public InputStream getAllArtifactTypes(String project) throws Exception {

        HttpGet httpget = new HttpGet(doorsUrl+"publish/resources?projectName=" + project);
        InputStream artifactTypes = null;
        httpget.setHeader("Accept", "application/xml");
        httpget.setHeader("X-Jazz-CSRF-Prevent",JSESSIONID);

       try {

           HttpResponse response = client.getHttpClient().execute(httpget);

           artifactTypes = response.getEntity().getContent();


       }  catch (IOException e) {
           e.printStackTrace();
       }

       return artifactTypes;

   }


}
