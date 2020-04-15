package gov.nasa.jpl.mbee.doorsng;

import gov.nasa.jpl.mbee.doorsng.model.Person;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.json.JSONObject;

import gov.nasa.jpl.mbee.doorsng.model.Requirement;

public class DoorsStandalone {

    private static final Logger logger = Logger.getLogger(DoorsStandalone.class.getName());
    private static String pass;

    private static final String vnvactivity = "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_wG6-UVH1EeeZqqBXHGi26w";
    private static final List<String> requirements = Arrays.asList(
        "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_wrFbUVH1EeeZqqBXHGi26w", //Requirement
        "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_d3ATkUcREeiggbwaJnQh2g", //Requirement - AVS
        "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_MZG5UEcREeiggbwaJnQh2g", //Requirement - FS Implementer
        "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_MIsfkVMWEeiggbwaJnQh2g", //Requirement - MS Implementer
        "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_TmaY4VMXEeiggbwaJnQh2g" //Requirement - PLD Implementer
    );

    private static final HashMap<String, String> vaHeaders;

    static {
        vaHeaders = new HashMap<>();
        //vaHeaders.put("", "id");
        //vaHeaders.put("", "Name");
        //vaHeaders.put("", "Artifact Type");
        //vaHeaders.put("", "Last Modified By");
        //vaHeaders.put("", "Last Modified Date");
        //vaHeaders.put("", "Schedule Start Date [V]");
        //vaHeaders.put("", "Scheduled Completion Date [V]");
        //vaHeaders.put("", "Actual Start Date [V]");
        //vaHeaders.put("", "Actual Completion Date [V]");
        vaHeaders.put("https://cae-jazz-uat.jpl.nasa.gov/rm/types/_vS4SQVH1EeeZqqBXHGi26w", "VAC");
        //vaHeaders.put("", "Link:Verifies or Validates (>)");
        vaHeaders.put("https://cae-jazz-uat.jpl.nasa.gov/rm/types/_vRrYYVH1EeeZqqBXHGi26w", "Verif at another level? [V]");
        //vaHeaders.put("", "VA Owner");
        //vaHeaders.put("", "Venue for R4R [V]");
        //vaHeaders.put("", "Description");
    }

    public static void main(String[] args) throws ParseException {
        Console console = System.console();
        pass = new String(console.readPassword("Password: "));

        Options options = new Options();

        options.addOption("user", true, "username");
        options.addOption("url", true, "doors url");
        options.addOption("project", true, "project area");

        CommandLineParser cliParser = new GnuParser();

        CommandLine cmd = cliParser.parse(options, args);

        String user = cmd.getOptionValue("user");
        String url = cmd.getOptionValue("url");
        String project = cmd.getOptionValue("project");

        JSONObject va = new JSONObject();
        JSONObject vi = new JSONObject();

        try {

            DoorsClient doors = new DoorsClient(user, pass, url, project);
            doors.setProject(project);
            Property[] properties = doors.getShape(OSLCConstants.RM_REQUIREMENT_TYPE, "V&V Activity").getProperties();

            va.append("VA", getReqs(vnvactivity, doors, properties));

            saveFile("va.json", va.toString(4));

            for (String type : requirements) {
                vi.append("VI", getReqs(type, doors, properties));
            }
            saveFile("vi.json", vi.toString(4));

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        } finally {

            System.out.println(va);
            System.out.println(vi);

        }

    }

    private static List<Map<String, Object>> getReqs(String type, DoorsClient doors, Property[] properties) {
        OslcQueryParameters queryParams = new OslcQueryParameters();
        String prefix = "rm=<http://www.ibm.com/xmlns/rdm/rdf/>";
        String where = String.format("rm:ofType=<%s>", type);

        queryParams.setPrefix(prefix);
        queryParams.setWhere(where);
        OslcQueryResult results = doors.submitQuery(queryParams);
        //System.out.println(results.toString());

        List<Map<String, Object>> reqs = new ArrayList<>();
        for (String resultsUrl : results.getMembersUrls()) {
            Requirement current = doors.getRequirement(resultsUrl);
            Map<String, Object> res = new HashMap<>();
            res.put("id", current.getIdentifier());
            res.put("title", current.getTitle());

            res.put("primaryText", current.getPrimaryText());
            res.put("created", current.getCreated());
            res.put("artifactType", current.getInstanceShape());
            List<String> creatorList = new ArrayList<>();
            for (URI creator : current.getCreators()) {
                ClientResponse clientResponse = doors.getResponse(creator.toString());
                Person creatorPerson = clientResponse.getEntity(Person.class);
                creatorList.add(creatorPerson.getName());
            }
            res.put("creators", creatorList);
            res.put("validatedBy", current.getValidatedBy());
            res.put("affectedBy", current.getAffectedBy());
            res.put("description", current.getDescription());
            //res.put("etag", current.getEtag());
            //res.put("resourceUrl", current.getResourceUrl());
            for (Property property : properties) {
                String value = current.getCustomField(property.getPropertyDefinition());
                if (value != null) {
                    res.put(property.getTitle(), value);
                }
            }
            reqs.add(res);
        }

        return reqs;
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

    private void test() {
        //String requirement = "https://cae-jazz-uat.jpl.nasa.gov/rm/resources/_AWOagaeVEeeg0YllPTHinw";
        //String requirement = "https://cae-jazz-uat.jpl.nasa.gov/rm/resources/CA_ebd86a8744fc431184509866b6267fdc";
        //String requirement = "https://cae-jazz-uat.jpl.nasa.gov/rm/resources/_gB5pQQvfEemDEcTe-T3Suw";
        //Requirement req = doors.getRequirement(requirement);
        //URI type = req.getInstanceShape();
        //System.out.println(types.toString());
        //ClientResponse rawClientResponse = doors.getResponse(type.toString());
        //System.out.println(rawClientResponse.getEntity(String.class));

                /*
    ECR Pending [DNG-Renamed-1]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_bmmY0dSkEemFdPAMXlyYog
Audit Date [V]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_vMsxwVH1EeeZqqBXHGi26w
Tracked Contributor
http://purl.org/dc/terms/contributor
Implementing Systems
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_k0KmEdbbEeeiatyn-Hc-EA
ForeignModifiedOn
http://jazz.net/ns/rm/dng/attribute#masterForeignModifiedOn
Modifies
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_xCFmEVH1EeeZqqBXHGi26w
Rationale [S] [DNG-Renamed-1]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_blF88dSkEemFdPAMXlyYog
Verifies or Validates
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_w-3S8VH1EeeZqqBXHGi26w
Auditor [V] [DNG-Renamed-1]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_bpMZ0dSkEemFdPAMXlyYog
Evidence
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_xFKvQVH1EeeZqqBXHGi26w
Safety-Criticality [S] [DNG-Renamed-1]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_buDEodSkEemFdPAMXlyYog
L2 Section Title
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_xTNxEYNzEeeaM-GYpXwRRw
Notes/Additional Info [REQ] [DNG-Renamed-1]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_byYyEdSkEemFdPAMXlyYog
Closes
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_xGmSoVH1EeeZqqBXHGi26w
Approval Date [S]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_vLeCsVH1EeeZqqBXHGi26w
Title
http://purl.org/dc/terms/title
Approver [S] [DNG-Renamed-1]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_bpswIdSkEemFdPAMXlyYog
VnV Approach [V]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_u54jcVH1EeeZqqBXHGi26w
Auditor [V]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_vJUGkVH1EeeZqqBXHGi26w
Specifies
http://open-services.net/ns/rm#specifies
VAC [DNG-Renamed-1]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_boM7UdSkEemFdPAMXlyYog
Implementing Systems [DNG-Renamed-1]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_btGCYdSkEemFdPAMXlyYog
Notes/Additional Info [REQ]
https://cae-jazz-uat.jpl.nasa.gov/rm/types/__261UZN9EeewRuGkCUkqIw
Implementer
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_b5Z_cTFmEemeK5JdDnyrgg
References
http://purl.org/dc/terms/references
VAC
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_vS4SQVH1EeeZqqBXHGi26w
Certifies
https://cae-jazz-uat.jpl.nasa.gov/rm/types/_xEINcVH1EeeZqqBXHGi26w
ForeignID
http://jazz.net/ns/rm/dng/attribute#masterForeignId
         */
    }
}
