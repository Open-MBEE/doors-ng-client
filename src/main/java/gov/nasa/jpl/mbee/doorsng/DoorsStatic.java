package gov.nasa.jpl.mbee.doorsng;

import java.util.logging.Logger;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DoorsStatic {

    private static final Logger logger = Logger.getLogger(DoorsStatic.class.getName());

    public static String createRequirement(String projectArea, Requirement requirement) throws Exception {
        DoorsClient doors = new DoorsClient(projectArea);
        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(doors.create(requirement));
    }

    public static String readRequirement(String projectArea) throws Exception {
        DoorsClient doors = new DoorsClient(projectArea);
        ObjectMapper mapper = new ObjectMapper();

        Requirement[] req = doors.getRequirements();

        return mapper.writeValueAsString(req);
    }

    public static String readRequirement(String projectArea, String resourceUrl) throws Exception {
        DoorsClient doors = new DoorsClient(projectArea);
        ObjectMapper mapper = new ObjectMapper();

        Requirement req = doors.getRequirement(resourceUrl);

        if (req.getTitle() == null) {
            RequirementCollection reqCol = doors.getRequirementCollection(resourceUrl);
            return mapper.writeValueAsString(reqCol);
        }

        return mapper.writeValueAsString(req);
    }

    public static String updateRequirement(String projectArea, String resourceUrl, Requirement requirement) throws Exception {
        DoorsClient doors = new DoorsClient(projectArea);
        ObjectMapper mapper = new ObjectMapper();

        Requirement req = doors.getRequirement(resourceUrl);
        requirement.setResourceUrl(resourceUrl);
        requirement.setEtag(req.getEtag());

        return mapper.writeValueAsString(doors.update(requirement));
    }

    public static String deleteRequirement(String projectArea, String resourceUrl) throws Exception {
        DoorsClient doors = new DoorsClient(projectArea);
        ObjectMapper mapper = new ObjectMapper();

        return mapper.writeValueAsString(doors.delete(resourceUrl));
    }
}
