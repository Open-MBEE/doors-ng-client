package gov.nasa.jpl.mbee.doorsng;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;
import org.eclipse.lyo.client.exception.RootServicesException;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;
import org.eclipse.lyo.client.oslc.resources.Requirement;
import org.eclipse.lyo.client.oslc.resources.RmUtil;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;

/**
 * Utility class for handling Doors integration
 * @author cinyoung
 *
 */
public class DoorsNgUtils {
    private static JazzFormAuthClient client = null;
    private static ResourceShape collectionInstanceShape;
    private static ResourceShape featureInstanceShape;
    private static JazzRootServicesHelper helper = null;
    private static final Logger logger = Logger.getLogger(DoorsNgUtils.class.getName());
    private static String queryCapability;

    private static String requirementFactory;

    public static boolean clientLogin(String webContextUrl, String user, String password, String projectArea) {
        try {
            if (client == null) {
                //STEP 1: Initialize a Jazz rootservices helper and indicate we're looking for the RequirementManagement catalog
                helper = new JazzRootServicesHelper(webContextUrl,OSLCConstants.OSLC_RM_V2);
        
                //STEP 2: Create a new Form Auth client with the supplied user/password
                //RRC is a fronting server, so need to use the initForm() signature which allows passing of an authentication URL.
                //For RRC, use the JTS for the authorization URL
        
                //This is a bit of a hack for readability.  It is assuming RRC is at context /rm.  Could use a regex or UriBuilder instead.
                String authUrl = webContextUrl.replaceFirst("/rm","/jts");
                client = helper.initFormClient(user, password, authUrl);
            }
            
            if (client.login() == HttpStatus.SC_OK) {
                return true;
            }
        } catch (RootServicesException re) {
            logger.log(Level.SEVERE,"Unable to access the Jazz rootservices document at: " + webContextUrl + "/rootservices", re);
        } catch (Exception e) {
            logger.log(Level.SEVERE,e.getMessage(),e);
        }

        return false;
    }

    public static JazzFormAuthClient getClient() {
        return client;
    }

    public static ResourceShape getCollectionInstanceShape() {
        return collectionInstanceShape;
    }

    public static ResourceShape getFeatureInstanceShape() {
        return featureInstanceShape;
    }
    
    public static JazzRootServicesHelper getHelper() {
        return helper;
    }

    public static String getQueryCapability() {
        return queryCapability;
    }
    
    
    public static String getRequirementFactory() {
        return requirementFactory;
    }

    public static boolean init(String webContextUrl, String user, String password, String projectArea) {
        if (!clientLogin(webContextUrl, user, password, projectArea)) return false;
        
        try {
            //STEP 4: Get the URL of the OSLC ChangeManagement catalog
            String catalogUrl = helper.getCatalogUrl();
    
            //STEP 5: Find the OSLC Service Provider for the project area we want to work with
            String serviceProviderUrl = client.lookupServiceProviderUrl(catalogUrl, projectArea);
    
            //STEP 6: Get the Query Capabilities URL so that we can run some OSLC queries
            queryCapability = client.lookupQueryCapability(serviceProviderUrl,
                                                                  OSLCConstants.OSLC_RM_V2,
                                                                  OSLCConstants.RM_REQUIREMENT_TYPE);
            //STEP 7: Create base requirements
            //Get the Creation Factory URL for change requests so that we can create one
    
            requirementFactory = client.lookupCreationFactory(
                    serviceProviderUrl, OSLCConstants.OSLC_RM_V2,
                    OSLCConstants.RM_REQUIREMENT_TYPE);
    
            //Get Feature Requirement Type URL
            featureInstanceShape = RmUtil.lookupRequirementsInstanceShapes(
                    serviceProviderUrl, OSLCConstants.OSLC_RM_V2,
                    OSLCConstants.RM_REQUIREMENT_TYPE, client, "Requirement");
            // TODO: resource shape defines properties that we can pull in
    
            collectionInstanceShape = RmUtil.lookupRequirementsInstanceShapes(
                    serviceProviderUrl, OSLCConstants.OSLC_RM_V2,
                    OSLCConstants.RM_REQUIREMENT_COLLECTION_TYPE, client, "Requirement Collection");
        } catch (Exception e) {
            logger.log(Level.SEVERE,e.getMessage(),e);
            return false;
        }
        
        return true;
    }

    static int count = 0;
    public static CustomRequirement getNewRequirement(String title) {
        CustomRequirement requirement = new CustomRequirement();
        requirement.setInstanceShape( featureInstanceShape.getAbout() );
        requirement.setTitle(title);
        requirement.setSysmlId( String.format("%d", count++) );
        return requirement;
    }
}
