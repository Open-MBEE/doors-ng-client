package gov.nasa.jpl.mbee.doorsng.TraceTree;

import static java.util.stream.Collectors.toList;

import gov.nasa.jpl.mbee.doorsng.DoorsClient;
import gov.nasa.jpl.mbee.doorsng.model.Person;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;
import java.net.URI;
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

import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.oslc.OSLCConstants;
import org.eclipse.lyo.client.oslc.resources.OslcQueryParameters;
import org.eclipse.lyo.client.oslc.resources.OslcQueryResult;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.json.JSONArray;

import gov.nasa.jpl.mbee.doorsng.model.Requirement;

public class TraceTreeExport {

    private static final Logger logger = Logger.getLogger(TraceTreeEuropaConfig.class.getName());
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
            Property[] viproperties = doors.getShape(OSLCConstants.RM_REQUIREMENT_TYPE, "Requirement").getProperties();
            //Property[] vnvproperties = doors.getShape(OSLCConstants.RM_REQUIREMENT_TYPE, "V&V Activity").getProperties();

            TraceTreeConfig ttpc = null;
            if (project.equals("Europa")) {
                ttpc = new TraceTreeEuropaConfig();
            } else {
                ttpc = new TraceTreePsycheConfig();
            }

            Map<String, List<Map<String, Object>>> reqs = new HashMap<>();
            reqs.put("va", new ArrayList<>());
            reqs.put("vi", new ArrayList<>());
            for (String vnvactivity : ttpc.getVATypes()) {
                List<Map<String, Object>> vnvs = getReqs(vnvactivity, doors, viproperties);
                for (Map<String, Object> vnv : vnvs) {
                    for (Map.Entry<String, Object> entry : vnv.entrySet()) {
                        System.out.println(entry.getKey() + ":" + entry.getValue().toString());
                    }
                    reqs.get("va").add(vnv);
                }
            }

            long now = new Date().getTime();
            String vaFilename = project + "_va-" + now;

            JSONArray va = new JSONArray(reqs.get("va"));
            saveFile(vaFilename + ".json", va.toString(4));
            saveFile(vaFilename + ".csv", toCSV(reqs.get("va")));

            for (String viurl : ttpc.getVITypes()) {
                List<Map<String, Object>> vis = getReqs(viurl, doors, viproperties);
                for (Map<String, Object> vnv : vis) {
                    reqs.get("vi").add(vnv);
                }
            }

            String viFilename = project + "_vi-" + now;

            JSONArray vi = new JSONArray(reqs.get("vi"));
            saveFile(viFilename + ".json", vi.toString(4));
            saveFile(viFilename + ".csv", toCSV(reqs.get("vi")));

        } catch (Exception e) {

            logger.log(Level.SEVERE, e.getMessage(), e);

        } finally {

            if (stdout.equals("true")) {
                System.out.println(stdout);
            }

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
            res.put("dngID", current.getIdentifier());
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
            res.put("validatedBy", linkToList(current.getValidatedBy()));
            res.put("affectedBy", linkToList(current.getAffectedBy()));
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

    private static List<String> linkToList(Link[] links) {
        List<String> result = new ArrayList<>();
        for (Link link : links) {
            result.add(link.getLabel());
        }
        return result;
    }

    private static String toCSV(List<Map<String, Object>> list) {
        List<String> headers = list.stream().flatMap(map -> map.keySet().stream()).distinct().collect(toList());
        final StringBuffer sb = new StringBuffer();
        sb.append("\"");
        for (int i = 0; i < headers.size(); i++) {
            sb.append(sanitize(headers.get(i)));
            sb.append(i == headers.size()-1 ? "\"\n" : "\",\"");
        }
        for (Map<String, Object> map : list) {
            for (int i = 0; i < headers.size(); i++) {
                sb.append(sanitize(map.get(headers.get(i))));
                sb.append(i == headers.size()-1 ? "\"\n" : "\",\"");
            }
        }
        return sb.toString();
    }

    private static String sanitize(Object string) {
        return string !=null ? string.toString().replaceAll("\"","\\\\\"") : "";
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
