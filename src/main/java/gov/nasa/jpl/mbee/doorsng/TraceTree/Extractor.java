package gov.nasa.jpl.mbee.doorsng.TraceTree;

import gov.nasa.jpl.mbee.doorsng.DoorsClient;
import gov.nasa.jpl.mbee.doorsng.TraceTree.adaptations.Europa;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


public class Extractor {

    private static final Logger logger = Logger.getLogger(Europa.class.getName());
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

            ExtractorConfig ttpc = (ExtractorConfig) Class.forName(project).getDeclaredConstructor().newInstance();

            Map<String, String> workflowMap = ttpc.getWorkflowMap();

            Map<String, List<Map<String, Object>>> reqs = new HashMap<>();
            reqs.put("va", new ArrayList<>());
            reqs.put("vi", new ArrayList<>());
            Map<String, String> vaTypes = ttpc.getVATypes();
            for (String key : vaTypes.keySet()) {
                Property[] properties = doors.getShape(OSLCConstants.RM_REQUIREMENT_TYPE, key).getProperties();
                List<Map<String, Object>> vnvs = ttpc.getVAReqs(vaTypes.get(key), doors, properties, workflowMap);
                reqs.get("va").addAll(vnvs);
            }

            Date today = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String vaFilename = project + "_va_" + formatter.format(today);

            JSONArray va = new JSONArray(reqs.get("va"));
            saveFile(vaFilename + ".json", va.toString(4));
            saveFile(vaFilename + ".csv", CDL.toString(va));

            Map<String, String> viTypes = ttpc.getVITypes();
            for (String key : viTypes.keySet()) {
                Property[] properties = doors.getShape(OSLCConstants.RM_REQUIREMENT_TYPE, key).getProperties();
                List<Map<String, Object>> vis = ttpc.getVIReqs(viTypes.get(key), doors, properties, workflowMap);
                reqs.get("vi").addAll(vis);
            }

            String viFilename = project + "_vi_" + formatter.format(today);

            JSONArray vi = new JSONArray(reqs.get("vi"));
            saveFile(viFilename + ".json", vi.toString(4));
            saveFile(viFilename + ".csv", CDL.toString(vi));
        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        } finally {

            if (stdout.equals("true")) {
                System.out.println(stdout);
            }

        }

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
