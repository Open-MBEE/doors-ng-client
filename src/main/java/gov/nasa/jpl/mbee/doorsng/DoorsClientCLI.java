package gov.nasa.jpl.mbee.doorsng;

import gov.nasa.jpl.mbee.doorsng.model.Person;
import gov.nasa.jpl.mbee.doorsng.model.Requirement;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.resources.OslcQueryParameters;
import org.eclipse.lyo.client.oslc.resources.OslcQueryResult;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;
import org.json.JSONArray;
import org.json.JSONObject;

public class DoorsClientCLI {

    private static final Logger logger = Logger.getLogger(DoorsClientCLI.class.getName());
    private static String pass;

    private static Map<String, Requirement> requirementCache = new HashMap<>();
    private static Map<URI, ResourceShape> resourceShapeCache = new HashMap<>();

    public static void main(String[] args) throws ParseException {
        Console console = System.console();
        pass = new String(console.readPassword("Password: "));

        Options options = new Options();

        options.addOption("consumer", true, "consumer key");
        options.addOption("secret", true, "consumer secret");
        options.addOption("user", true, "username");
        options.addOption("url", true, "doors url");
        options.addOption("project", true, "project area");
        options.addOption("requirement", true, "requirement");
        options.addOption("resource", true, "resource");

        CommandLineParser cliParser = new GnuParser();

        CommandLine cmd = cliParser.parse(options, args);

        if (!validateOptions(cmd)) {
            System.out.println("Syntax:  java -jar <jar file> -action \"(create || read || update || delete)\" -consumer \"<consumerKey>\" -secret \"<consumerSecret>\" -user \"<username>\" -pass \"<password>\" -url \"<doors_url>\" -project \"<project_area>\" (-requirement <json> || <resourceUrl>)");
            System.out.println("Example: java -jar target/doorsng-x.x.x.jar -action \"create\" -consumer \"CONSUMERKEY\" -secret \"CONSUMERSECRET\" -user \"JPLUSERNAME\" -pass \"JPLPASSWORD\" -url \"DOORSURL\" -project \"Test Project\" -requirement {\"title\":\"Requirement 01\", \"sysmlid\":\"123-456-678\"}");
            return;
        }

        String consumer = cmd.getOptionValue("consumer");
        String secret = cmd.getOptionValue("secret");
        String user = cmd.getOptionValue("user");
        String url = cmd.getOptionValue("url");
        String project = cmd.getOptionValue("project");
        String requirement = cmd.getOptionValue("requirement");

        JSONObject response = new JSONObject();

        try {

            DoorsClient doors = new DoorsClient(user, pass, url, project);
            doors.setProject(project);

            if (requirement != null) {
                Set<Requirement> result = new HashSet<>();
                result.add(doors.getRequirement(requirement));
                response.put("result", result);
                response.put("errors", doors.getErrors());
            } else {
                response.put("result", doors.getRequirements());
                response.put("errors", doors.getErrors());
            }


        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        } finally {

            saveFile("export.json", response.toString(4));
            System.out.println(response);

        }

    }

    private static boolean validateOptions(CommandLine cmd) {

        String action = cmd.getOptionValue("action");

        if (action == null || (
            action != null && pass != null &&
                cmd.getOptionValue("user") != null &&
                cmd.getOptionValue("url") != null &&
                cmd.getOptionValue("project") != null
        )) {
            return true;
        }

        return false;

    }

    private static void saveFile(String name, String contents) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(name, true));
            writer.append(contents);
            writer.close();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }
}
