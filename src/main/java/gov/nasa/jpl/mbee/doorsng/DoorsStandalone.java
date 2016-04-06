package gov.nasa.jpl.mbee.doorsng;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.json.JSONObject;
import org.json.JSONArray;

public class DoorsStandalone {

    private static final Logger logger = Logger.getLogger(DoorsStandalone.class.getName());

    public static void main(String[] args) throws ParseException {

        Options options = new Options();

        options.addOption("user", true, "username");
        options.addOption("pass", true, "password");
        options.addOption("url", true, "doors url");
        options.addOption("project", true, "project area");
        options.addOption("action", true, "action");
        options.addOption("requirement", true, "requirement");

        CommandLineParser cliParser = new GnuParser();

        CommandLine cmd = cliParser.parse(options, args);

        if (!validateOptions(cmd)) {
            System.out.println("Syntax:  java -jar <jar file> -action \"(create || read || update || delete)\" -user \"<username>\" -pass \"<password>\" -url \"<doors_url>\" -project \"<project_area>\" (-requirement <json> || <resourceUrl>)");
            System.out.println("Example: java -jar target/doorsng-x.x.x.jar -action \"create\" -user \"JPLUSERNAME\" -pass \"JPLPASSWORD\" -url \"DOORSURL\" -project \"Test Project\" -requirement {\"title\":\"Requirement 01\", \"sysmlid\":\"123-456-678\"}");
            return;
        }

        String action = cmd.getOptionValue("action");
        String user = cmd.getOptionValue("user");
        String pass = cmd.getOptionValue("pass");
        String url = cmd.getOptionValue("url");
        String project = cmd.getOptionValue("project");
        String requirement = cmd.getOptionValue("requirement");

        JSONObject response = new JSONObject();

        try {

            DoorsClient doors = new DoorsClient(user, pass, url, project);

            if ("read".equals(action)) {
                if (requirement != null) {
                    response.put("result", doors.getRequirement(requirement));
                } else {
                    response.put("result", doors.getRequirements());
                }
            }

            if ("create".equals(action)) {
                JSONObject json = new JSONObject(requirement);
                System.out.println(json);
                Requirement req = new Requirement();
                if (json.has("title")) {
                    req.setTitle(json.getString("title"));
                }
                if (json.has("description")) {
                    req.setDescription(json.getString("description"));
                }
                if (json.has("primaryText")) {
                    req.setPrimaryText(json.getString("primaryText"));
                }
                if (json.has("parent")) {
                    Folder folder = new Folder();
                    folder.setTitle(json.getString("parent"));
                    String folderResource = doors.create(folder);
                    System.out.println(folderResource);
                    if (folderResource != null) {
                        req.setParent(URI.create(folderResource));
                    }
                }

                response.put("result", doors.create(req));
            }
            // TODO: Finish update functions
            if ("update".equals(action)) {
                JSONObject json = null;
                if (requirement != null) {
                    json = new JSONObject(requirement);
                }
                System.out.println(json);
            }

            if ("delete".equals(action)) {
                if (requirement != null) {
                    response.put("result", doors.delete(requirement));
                } else {
                    Requirement[] reqs = doors.getRequirements();
                    for (Requirement req : reqs) {
                        String parentResource = req.getParent();
                        doors.delete(req.getResourceUrl());
                        while (parentResource != null && !parentResource.contains(doors.rootFolder)) {
                            Folder parentFolder = doors.getFolder(parentResource);
                            if (parentFolder.getResourceUrl() != null) {
                                doors.delete(parentFolder.getResourceUrl());
                                parentResource = parentFolder.getParent();
                            } else {
                                parentResource = null;
                            }
                            System.out.println(parentResource);
                        }
                    }
                }
            }

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        } finally {

            System.out.println(response);

        }

    }

    private static boolean validateOptions(CommandLine cmd) {

        String action = cmd.getOptionValue("action");

        if (
            action != null &&
            cmd.getOptionValue("user") != null &&
            cmd.getOptionValue("pass") != null &&
            cmd.getOptionValue("url") != null &&
            cmd.getOptionValue("project") != null
            ) {
            if ("create".equals(action) && (cmd.hasOption("requirement"))) {
                return true;
            } else if ("read".equals(action)) {
                return true;
            } else if ("update".equals(action) && (cmd.hasOption("requirement"))) {
                return true;
            } else if ("delete".equals(action)) {
                return true;
            }
        }

        return false;

    }
}
