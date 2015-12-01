package gov.nasa.jpl.mbee.doorsng;

import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.http.HttpStatus;
import org.eclipse.lyo.client.exception.RootServicesException;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.jazz.JazzFormAuthClient;
import org.eclipse.lyo.client.oslc.jazz.JazzRootServicesHelper;
import org.eclipse.lyo.client.oslc.resources.RmUtil;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;

/**
 * Utility class for handling Doors integration
 * 
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
    private static String requirementCollectionFactory;
    private static String folderQuery;
    private static String folderFactory;

    public static boolean clientLogin(String webContextUrl, String user, String password, String projectArea) {
        try {
            if (client == null) {
                helper = new JazzRootServicesHelper(webContextUrl, OSLCConstants.OSLC_RM_V2);

                String authUrl = webContextUrl.replaceFirst("/rm", "/jts");
                client = helper.initFormClient(user, password, authUrl);
            }

            if (client.login() == HttpStatus.SC_OK) {
                return true;
            }
        } catch (RootServicesException re) {
            logger.log(Level.SEVERE,
                    "Unable to access the Jazz rootservices document at: " + webContextUrl + "/rootservices", re);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
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
    
    public static String getRequirementCollectionFactory() {
        return requirementCollectionFactory;
    }

    public static String getFolderQuery() {
        return folderQuery;
    }

    public static String getFolderFactory() {
        return folderFactory;
    }

    public static boolean init(String webContextUrl, String user, String password, String projectArea) {
        if (!clientLogin(webContextUrl, user, password, projectArea))
            return false;

        try {
            String catalogUrl = helper.getCatalogUrl();
            String serviceProviderUrl = client.lookupServiceProviderUrl(catalogUrl, projectArea);

            URL serviceProvider = new URL(serviceProviderUrl);
            String[] serviceProviderPath = serviceProvider.getPath().split("/");

            queryCapability = client.lookupQueryCapability(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_TYPE);
            requirementFactory = client.lookupCreationFactory(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_TYPE);
            requirementCollectionFactory = client.lookupCreationFactory(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_COLLECTION_TYPE);
            featureInstanceShape = RmUtil.lookupRequirementsInstanceShapes(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_TYPE, client, "Requirement");
            collectionInstanceShape = RmUtil.lookupRequirementsInstanceShapes(serviceProviderUrl, OSLCConstants.OSLC_RM_V2, OSLCConstants.RM_REQUIREMENT_COLLECTION_TYPE, client, "Requirement Collection");
            folderQuery = serviceProvider.getProtocol() + "://" + serviceProvider.getAuthority() + "/rm/folders/" +  serviceProviderPath[serviceProviderPath.length - 2];
            folderFactory = serviceProvider.getProtocol() + "://" + serviceProvider.getAuthority() + "/rm/folders?projectUrl=" + serviceProvider.getProtocol() + "://" + serviceProvider.getAuthority() + "/jts/process/project-areas/" +  serviceProviderPath[serviceProviderPath.length - 2];
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }

        return true;
    }
}
