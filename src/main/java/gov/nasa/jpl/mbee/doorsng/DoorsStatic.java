package gov.nasa.jpl.mbee.doorsng;

import java.util.Set;
import java.util.logging.Logger;
import org.json.JSONObject;

import gov.nasa.jpl.mbee.doorsng.model.Requirement;
import gov.nasa.jpl.mbee.doorsng.model.RequirementCollection;

public class DoorsStatic {

    private static final Logger logger = Logger.getLogger(DoorsStatic.class.getName());

    public static DoorsClient initClient(String consumerKey, String consumerSecret, String user, String password, String webContextUrl, String projectArea) throws Exception {
        return new DoorsClient(user, password, webContextUrl, projectArea);
    }

    public static JSONObject createRequirement(DoorsClient doors, Requirement requirement) throws Exception {
        return new JSONObject(doors.create(requirement));
    }

    public static JSONObject readRequirement(DoorsClient doors) throws Exception {
        Set<Requirement> req = doors.getRequirements();

        return new JSONObject(req);
    }

    public static JSONObject readRequirement(DoorsClient doors, String resourceUrl) throws Exception {
        Requirement req = doors.getRequirement(resourceUrl);

        if (req.getTitle() == null) {
            RequirementCollection reqCol = doors.getRequirementCollection(resourceUrl);
            return new JSONObject(reqCol);
        }

        return new JSONObject(req);
    }

    public static JSONObject updateRequirement(DoorsClient doors, String resourceUrl, Requirement requirement) throws Exception {
        Requirement req = doors.getRequirement(resourceUrl);
        requirement.setResourceUrl(resourceUrl);
        requirement.setEtag(req.getEtag());

        return new JSONObject(doors.update(requirement));
    }

    public static JSONObject deleteRequirement(DoorsClient doors, String resourceUrl) throws Exception {
        return new JSONObject(doors.delete(resourceUrl));
    }
}
