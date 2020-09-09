package gov.nasa.jpl.mbee.doorsng;

import gov.nasa.jpl.mbee.doorsng.model.Person;
import gov.nasa.jpl.mbee.doorsng.model.Requirement;
import java.io.Console;
import java.net.URI;
import java.util.ArrayList;
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

import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.resources.OslcQueryParameters;
import org.eclipse.lyo.client.oslc.resources.OslcQueryResult;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;
import org.json.JSONArray;

public class DoorsClientCLI {

    private static final Logger logger = Logger.getLogger(DoorsClientCLI.class.getName());
    private static String pass;

    private static Map<String, Requirement> requirementCache = new HashMap<>();
    private static Map<URI, ResourceShape> resourceShapeCache = new HashMap<>();

    public static void main(String[] args) throws ParseException {


        Options options = new Options();

        options.addOption("user", true, "username");
        options.addOption("url", true, "doors url");
        options.addOption("project", true, "project area");
        options.addOption("stdout", true, "print to stdout");
        options.addOption("pass", true, "Use stdin instead");
        options.addOption("consumerKey", true, "project area");
        options.addOption("consumerSecret", true, "project area");

        CommandLineParser cliParser = new GnuParser();

        CommandLine cmd = cliParser.parse(options, args);

        String user = cmd.getOptionValue("user");
        String url = cmd.getOptionValue("url");
        String project = cmd.getOptionValue("project");
        String stdout = cmd.getOptionValue("stdout", "false");

        String consumerKey = cmd.getOptionValue("consumerKey");
        String consumerSecret = cmd.getOptionValue("consumerSecret");

        String envPass = System.getenv("DNG_PASSWORD");
        pass = cmd.getOptionValue("pass", "");

        if (pass.isEmpty() && (envPass == null || envPass.isEmpty())) {
            Console console = System.console();
            pass = new String(console.readPassword("Password: "));
        } else if (pass.isEmpty()) {
            pass = envPass;
        }

        try {
            DoorsClient doors;
            if (consumerKey != null && !consumerKey.isEmpty() && !consumerSecret.isEmpty()) {
                doors = new DoorsClient(consumerKey, consumerSecret, user, pass, url, false);
            } else {
                doors = new DoorsClient(user, pass, url, project);
            }

            doors.setProject(project);

            String type = "https://cae-jazz.jpl.nasa.gov/rm/types/_wG6-UVH1EeeZqqBXHGi26w";

            Property[] properties = doors.getShape(OSLCConstants.RM_REQUIREMENT_TYPE, "V&V Activity").getProperties();

            OslcQueryParameters queryParams = new OslcQueryParameters();
            String prefix = "rm=<http://www.ibm.com/xmlns/rdm/rdf/>";
            String where = String.format("rm:ofType=<%s>", type);

            queryParams.setSelect("*");
            queryParams.setPrefix(prefix);
            queryParams.setWhere(where);
            OslcQueryResult results = doors.submitQuery(queryParams);

            List<Map<String, Object>> reqs = new ArrayList<>();
            int i = 0;
            for (String resultsUrl : results.getMembersUrls()) {
                Requirement current;
                if (requirementCache.get(resultsUrl) == null) {
                    current = doors.getRequirement(resultsUrl);
                } else {
                    current = requirementCache.get(resultsUrl);
                }

                if (current == null) {
                    continue;
                }

                Map<String, Object> res = new HashMap<>();

                res.put("id", current.getIdentifier());
                res.put("Name", current.getTitle());
                res.put("PrimaryText", current.getPrimaryText());
                res.put("Created", current.getCreated());

                ResourceShape resourceShape;
                if (resourceShapeCache.get(current.getInstanceShape()) == null) {
                    resourceShape = doors
                        .getResource(ResourceShape.class, current.getInstanceShape());
                } else {
                    resourceShape = resourceShapeCache.get(current.getInstanceShape());
                }

                res.put("Artifact Type", resourceShape.getTitle());

                List<String> creatorList = new ArrayList<>();
                for (URI creator : current.getCreators()) {
                    ClientResponse clientResponse = doors.getResponse(creator.toString());
                    Person creatorPerson = clientResponse.getEntity(Person.class);
                    creatorList.add(creatorPerson.getName());
                }
                res.put("Last Modified Date", current.getModified());
                res.put("Last Modified By", creatorList.get(creatorList.size() - 1));
                res.put("Created By", creatorList);
                Link[] verifieds = current.getValidatedBy();
                StringBuilder sb = new StringBuilder();
                for (Link verified : verifieds) {
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(verified.getLabel());
                }
                if (sb.length() > 0) {
                    res.put("Link:Verified or Validated By (<)", sb.toString());
                }
                res.put("Affected By", current.getAffectedBy());
                res.put("Description", current.getDescription());

                for (Property property : properties) {
                    if (property.getTitle() != null) {
                        String value = current.getCustomField(property.getPropertyDefinition());
                        res.put(property.getTitle(), value != null ? value : "");
                    }
                }

                reqs.add(res);
            }

            JSONArray response = new JSONArray(reqs);
            System.out.println(response.toString(4));

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        } finally {

            if (stdout.equals("true")) {
                System.out.println(stdout);
            }

        }

    }

}
