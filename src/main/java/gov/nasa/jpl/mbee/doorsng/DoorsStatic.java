package gov.nasa.jpl.mbee.doorsng;

import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DoorsStatic {

    private static final Logger logger = Logger.getLogger(DoorsStatic.class.getName());

    public static JSONObject createRequirement(String projectArea, Requirement requirement) throws Exception {
        DoorsClient doors = new DoorsClient(projectArea);

        return new JSONObject(doors.create(requirement));
    }

    public static JSONObject readRequirement(String projectArea) throws Exception {
        DoorsClient doors = new DoorsClient(projectArea);

        Requirement[] req = doors.getRequirements();

        return new JSONObject(req);
    }

    public static JSONObject readRequirement(String projectArea, String resourceUrl) throws Exception {
        DoorsClient doors = new DoorsClient(projectArea);

        Requirement req = doors.getRequirement(resourceUrl);

        if (req.getTitle() == null) {
            RequirementCollection reqCol = doors.getRequirementCollection(resourceUrl);
            return new JSONObject(reqCol);
        }

        return new JSONObject(req);
    }

    public static JSONObject updateRequirement(String projectArea, String resourceUrl, Requirement requirement) throws Exception {
        DoorsClient doors = new DoorsClient(projectArea);

        Requirement req = doors.getRequirement(resourceUrl);
        requirement.setResourceUrl(resourceUrl);
        requirement.setEtag(req.getEtag());

        return new JSONObject(doors.update(requirement));
    }

    public static JSONObject deleteRequirement(String projectArea, String resourceUrl) throws Exception {
        DoorsClient doors = new DoorsClient(projectArea);

        return new JSONObject(doors.delete(resourceUrl));
    }
}
