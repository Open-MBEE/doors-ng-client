package gov.nasa.jpl.mbee.doorsng;

import gov.nasa.jpl.mbee.doorsng.model.Requirement;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
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

import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.json.CDL;
import org.json.JSONArray;
import org.json.JSONObject;

public class DoorsClientCLI {

    private static final Logger logger = Logger.getLogger(DoorsClientCLI.class.getName());
    private static String pass;

    public static void main(String[] args) throws ParseException {

        Options options = new Options();

        options.addOption("user", true, "username");
        options.addOption("url", true, "doors url");
        options.addOption("project", true, "project area");
        options.addOption("stdout", true, "print to stdout");
        options.addOption("pass", true, "Use stdin instead");

        CommandLineParser cliParser = new GnuParser();

        CommandLine cmd = cliParser.parse(options, args);

        String user = cmd.getOptionValue("user");
        String url = cmd.getOptionValue("url");
        String project = cmd.getOptionValue("project");
        String stdout = cmd.getOptionValue("stdout", "false");

        String envPass = System.getenv("DNG_PASSWORD");
        pass = cmd.getOptionValue("pass", "");

        if (pass.isEmpty() && (envPass == null || envPass.isEmpty())) {
            Console console = System.console();
            pass = new String(console.readPassword("Password: "));
        } else if (pass.isEmpty()) {
            pass = envPass;
        }

        try {

            DoorsClient doors = new DoorsClient(user, pass, url, project);
            doors.setProject(project);
            Set<Requirement> requirements = doors.getRequirements();
            System.out.println(new JSONObject(requirements).toString(4));

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        } finally {

            if (stdout.equals("true")) {
                System.out.println(stdout);
            }

        }

    }

}
