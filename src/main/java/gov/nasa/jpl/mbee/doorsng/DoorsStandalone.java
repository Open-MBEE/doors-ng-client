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

import gov.nasa.jpl.mbee.doorsng.model.Requirement;
import gov.nasa.jpl.mbee.doorsng.model.Folder;

public class DoorsStandalone {

    private static final Logger logger = Logger.getLogger(DoorsStandalone.class.getName());

    public static void main(String[] args) throws ParseException {

        Options options = new Options();

        options.addOption("consumer", true, "consumer key");
        options.addOption("secret", true, "consumer secret");
        options.addOption("user", true, "username");
        options.addOption("pass", true, "password");
        options.addOption("url", true, "doors url");
        options.addOption("project", true, "project area");
        options.addOption("action", true, "action");
        options.addOption("requirement", true, "requirement");

        CommandLineParser cliParser = new GnuParser();

        CommandLine cmd = cliParser.parse(options, args);

        if (!validateOptions(cmd)) {
            System.out.println("Syntax:  java -jar <jar file> -action \"(create || read || update || delete)\" -consumer \"<consumerKey>\" -secret \"<consumerSecret>\" -user \"<username>\" -pass \"<password>\" -url \"<doors_url>\" -project \"<project_area>\" (-requirement <json> || <resourceUrl>)");
            System.out.println("Example: java -jar target/doorsng-x.x.x.jar -action \"create\" -consumer \"CONSUMERKEY\" -secret \"CONSUMERSECRET\" -user \"JPLUSERNAME\" -pass \"JPLPASSWORD\" -url \"DOORSURL\" -project \"Test Project\" -requirement {\"title\":\"Requirement 01\", \"sysmlid\":\"123-456-678\"}");
            return;
        }

        String action = cmd.getOptionValue("action");
        String consumer = cmd.getOptionValue("consumer");
        String secret = cmd.getOptionValue("secret");
        String user = cmd.getOptionValue("user");
        String pass = cmd.getOptionValue("pass");
        String url = cmd.getOptionValue("url");
        String project = cmd.getOptionValue("project");
        String requirement = cmd.getOptionValue("requirement");

        JSONObject response = new JSONObject();

        try {

            DoorsClient doors = new DoorsClient(user, pass, url, project);
            doors.setProject(project);

            if ("read".equals(action)) {
                if (requirement != null) {
                    response.put("result", doors.getRequirement(requirement));
                } else {
                    response.put("result", doors.getRequirements());
                }
            }

            if ("create".equals(action)) {
                if (requirement != null) {
                    JSONObject json = new JSONObject(requirement);
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
                        if (folderResource != null) {
                            req.setParent(URI.create(folderResource));
                        }
                    }
                    if (json.has("type")) {
                        response.put("result", doors.create(req, json.getString("type")));
                    } else {
                        response.put("result", doors.create(req));
                    }
                }
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
            if ("create".equals(action)) {
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
