package gov.nasa.jpl.mbee.doorsng.JsonAdapter;

import gov.nasa.jpl.mbee.doorsng.DoorsClient;
import gov.nasa.jpl.mbee.doorsng.model.Requirement;
import org.apache.commons.cli.*;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;
import org.json.JSONObject;

import java.io.Console;
import java.net.URI;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DNGExport {

    private static final Logger logger = Logger.getLogger(DNGExport.class.getName());
    private static String pass;

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
        options.addOption("threads", true, "number of threads / concurrent connections to use when crawling DNG");

        CommandLineParser cliParser = new GnuParser();

        CommandLine cmd = cliParser.parse(options, args);

        if (!validateOptions(cmd)) {
            System.out.println("Syntax:  java -jar <jar file> -action \"(create || read || update || delete)\" -consumer \"<consumerKey>\" -secret \"<consumerSecret>\" -user \"<username>\" -pass \"<password>\" -url \"<doors_url>\" -project \"<project_area>\" (-requirement <json> || <resourceUrl>)");
            System.out.println("Example: java -jar target/doorsng-x.x.x.jar -action \"create\" -consumer \"CONSUMERKEY\" -secret \"CONSUMERSECRET\" -user \"JPLUSERNAME\" -pass \"JPLPASSWORD\" -url \"DOORSURL\" -project \"Test Project\" -requirement {\"title\":\"Requirement 01\", \"sysmlid\":\"123-456-678\"}");
            return;
        }

        String user = cmd.getOptionValue("user");
        String url = cmd.getOptionValue("url");
        String project = cmd.getOptionValue("project");
        String requirement = cmd.getOptionValue("requirement");
        String mmsProjectId = cmd.getOptionValue("mms-project");
        int nThreads = cmd.hasOption("threads")? Integer.min(1, Integer.parseInt(cmd.getOptionValue("threads"))): 1;

        Map<String, String> errors = new HashMap<>();

        try {

            DoorsClient doors = new DoorsClient(user, pass, url, project);
            doors.setProject(project);

            ElementFactory elementFactory = new ElementFactory(mmsProjectId, url);

            Uml2JsonClass rootPm = elementFactory.createClass(mmsProjectId+"_pm", project);

            System.out.println("{\"elements\":["+rootPm.getSerialization());

            if (requirement != null) {
                try {
                    String jsonArray = doors.getRequirement(requirement).export(doors, elementFactory, resourceShapeCache).toString();
                    System.out.print(",\n"+jsonArray.substring(1, jsonArray.length()-1));
                }
                catch(Exception e) {
                    errors.put(requirement, e.getMessage());
                }
            } else {
                ExecutorService pool = Executors.newFixedThreadPool(nThreads);

                for(Future<Requirement> future: doors.getRequirementsFutures(nThreads)) {
                    pool.submit(() -> {
                        Requirement req = null;
                        try {
                            req = future.get();

                            // JSON stream to stdout
                            String jsonArray = req.export(doors, elementFactory, resourceShapeCache).toString();
                            System.out.print(",\n"+jsonArray.substring(1, jsonArray.length()-1));
                        }
                        catch(Exception e) {
                            if(req != null) {
                                errors.put(req.getIdentifier(), e.getMessage());
                            }
                            else {
                                logger.log(Level.SEVERE, e.getMessage(), e);
                            }
                        }
                    });
                }

                pool.shutdown();
                pool.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);

                errors.putAll(doors.getErrors());
            }


            System.out.println("\n]}");

            JSONObject errorReport = new JSONObject();
            errorReport.put("errors", errors);
            System.err.println(errorReport);

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        } finally {

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
}
