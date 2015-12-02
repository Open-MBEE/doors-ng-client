package gov.nasa.jpl.mbee.doorsng;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DoorsClientStandalone {

    private static final Logger logger = Logger.getLogger(DoorsClientStandalone.class.getName());

    public static void main(String[] args) throws ParseException {

        Options options = new Options();

        options.addOption("action", true, "action");
        options.addOption("requirement", true, "requirement");
        options.addOption("collection", true, "collection");
        options.addOption("folder", true, "folder");
        options.addOption("project",true,"project area");

        CommandLineParser cliParser = new GnuParser();

        CommandLine cmd = cliParser.parse(options, args);

        if (!validateOptions(cmd)) {
            logger.severe("Syntax:  java <class_name> (-requirement <json> || -collection <collection> || -folder <folder>) -project \"<project_area>\"");
            logger.severe("Example: java DoorsClientStandalone.class -requirement {\"sysmlid\":\"123-456-678\"} -collection {\"title\":\"something\",\"sysmlids\": [\"123-456-789\",\"987-654-321\"]} -project \"Test Project\"");
            return;
        }

        String action = cmd.getOptionValue("action");
        String requirement = cmd.getOptionValue("requirement");
        String collection = cmd.getOptionValue("collection");
        String folder = cmd.getOptionValue("folder");
        String projectArea = cmd.getOptionValue("project");

        ObjectMapper mapper = new ObjectMapper();
        String response = "";

        try {
            DoorsClient doors = new DoorsClient(projectArea);

            if (requirement != null) {
                Requirement req = mapper.readValue(requirement, Requirement.class);

                if ("read".equals(action)) {
                    if(req.getSysmlid() != null) {
                        response = mapper.writeValueAsString(doors.getRequirement(req.getSysmlid()));
                    } else {
                        response = mapper.writeValueAsString(doors.getRequirements());
                    }
                } else if ("create".equals(action) || "update".equals(action)) {
                    response = mapper.writeValueAsString(doors.createUpdate(req));
                } else if ("delete".equals(action)) {
                    response = mapper.writeValueAsString(doors.delete(req));
                }

            } else if (collection != null) {
                RequirementCollection reqCol = mapper.readValue(collection, RequirementCollection.class);

                if ("read".equals(action)) {
                    if(reqCol.getTitle() != null) {
                        response = mapper.writeValueAsString(doors.getRequirementCollection(reqCol.getTitle()));
                    } else {
                        response = mapper.writeValueAsString(doors.getRequirementCollections());
                    }
                } else if ("create".equals(action) || "update".equals(action)) {
                    response = mapper.writeValueAsString(doors.createUpdate(reqCol));
                } else if ("delete".equals(action)) {
                    response = mapper.writeValueAsString(doors.delete(reqCol));
                }
            } else if (folder != null) {
                if ("create".equals(action)) {
                    Folder fold = mapper.readValue(folder, Folder.class);
                    response = mapper.writeValueAsString(doors.createFolder(fold));
                } else if ("read".equals(action)) {
                    response = mapper.writeValueAsString(doors.getFolder(folder));
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            System.out.println(response);
        }

    }

    private static boolean validateOptions(CommandLine cmd) {
        if (cmd.hasOption("action") && cmd.hasOption("project") && (cmd.hasOption("requirement") || cmd.hasOption("collection") || cmd.hasOption("folder"))) {

            return true;

        }

        return false;

    }
}
