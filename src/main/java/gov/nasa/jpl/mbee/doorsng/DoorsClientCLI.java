package gov.nasa.jpl.mbee.doorsng;

import gov.nasa.jpl.mbee.doorsng.MmsAdapter.ElementFactory;
import gov.nasa.jpl.mbee.doorsng.model.Requirement;
import org.apache.commons.cli.*;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;
import java.net.URI;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DoorsClientCLI {

    private static final Logger logger = Logger.getLogger(DoorsClientCLI.class.getName());
    private static String pass;

    private static Map<String, Requirement> requirementCache = new HashMap<>();
    private static Map<URI, ResourceShape> resourceShapeCache = new HashMap<>();

    public static void main(String[] args) throws ParseException {
        Console console = System.console();

        String envPass = System.getenv("DNG_PASSWORD");
        if(envPass == null || envPass.isEmpty()) {
            pass = new String(console.readPassword("Password: "));
        }
        else {
            pass = envPass;
        }

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
        String mmsProjectId = cmd.getOptionValue("mms-project");

        JSONObject response = new JSONObject();

        List<JSONObject> exports = new ArrayList<>();
        Map<String, String> errors = new HashMap<>();

        try {

            DoorsClient doors = new DoorsClient(user, pass, url, project);
            doors.setProject(project);

            ElementFactory elementFactory = new ElementFactory(mmsProjectId, url);

            if (requirement != null) {
                Set<Requirement> result = new HashSet<>();

                try {
                    exports = doors.getRequirement(requirement).export(doors, elementFactory, resourceShapeCache);
                }
                catch(Exception e) {
                    errors.put(requirement, e.getMessage());
                }
            } else {
                for(Future<Requirement> future: doors.getRequirementsFutures(32)) {
                    Requirement req = future.get();

                    try {
                        exports.addAll(req.export(doors, elementFactory, resourceShapeCache));
                    }
                    catch(Exception e) {
                        errors.put(req.getIdentifier(), e.getMessage());
                    }
                }

                errors.putAll(doors.getErrors());
            }

            response.put("elements", exports);
            JSONObject errorReport = new JSONObject();
            errorReport.put("errors", errors);
            System.err.println(errorReport);

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        } finally {

            saveFile("export.json", response.toString(4));
//            System.out.println(response.toString(4));

            if(errors.size() >= 1) {
                System.exit(1);
            }

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
