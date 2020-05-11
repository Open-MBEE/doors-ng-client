package gov.nasa.jpl.mbee.doorsng.TraceTree;

import static java.util.stream.Collectors.toList;

import gov.nasa.jpl.mbee.doorsng.DoorsClient;
import gov.nasa.jpl.mbee.doorsng.model.Person;
import java.io.BufferedWriter;
import java.io.Console;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
import org.eclipse.lyo.oslc4j.core.model.AbstractResource;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;
import org.json.JSONArray;

import gov.nasa.jpl.mbee.doorsng.model.Requirement;

public class TraceTreeExport {

    private static final Logger logger = Logger.getLogger(TraceTreeEuropaConfig.class.getName());
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

            TraceTreeConfig ttpc = null;
            if (project.equals("Europa")) {
                ttpc = new TraceTreeEuropaConfig();
            } else {
                ttpc = new TraceTreePsycheConfig();
            }

            Map<String, List<Map<String, Object>>> reqs = new HashMap<>();
            reqs.put("va", new ArrayList<>());
            reqs.put("vi", new ArrayList<>());
            Map<String, String> vaTypes = ttpc.getVATypes();
            for (String key : vaTypes.keySet()) {
                Property[] properties = doors.getShape(OSLCConstants.RM_REQUIREMENT_TYPE, key).getProperties();
                List<Map<String, Object>> vnvs = getTTReqs(vaTypes.get(key), doors, properties);
                reqs.get("va").addAll(vnvs);
            }

            Date today = new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
            String vaFilename = project + "_va_" + formatter.format(today);

            JSONArray va = new JSONArray(reqs.get("va"));
            saveFile(vaFilename + ".json", va.toString(4));
            saveFile(vaFilename + ".csv", toCSV(reqs.get("va")));

            Map<String, String> viTypes = ttpc.getVITypes();
            for (String key : viTypes.keySet()) {
                Property[] properties = doors.getShape(OSLCConstants.RM_REQUIREMENT_TYPE, key).getProperties();
                List<Map<String, Object>> vis = getTTReqs(viTypes.get(key), doors, properties);
                reqs.get("vi").addAll(vis);
            }

            String viFilename = project + "_vi_" + formatter.format(today);

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

    public static List<Map<String, Object>> getTTReqs(String type, DoorsClient doors, Property[] properties) {
        OslcQueryParameters queryParams = new OslcQueryParameters();
        String prefix = "rm=<http://www.ibm.com/xmlns/rdm/rdf/>";
        String where = String.format("rm:ofType=<%s>", type);

        queryParams.setPrefix(prefix);
        queryParams.setWhere(where);
        OslcQueryResult results = doors.submitQuery(queryParams);

        List<Map<String, Object>> reqs = new ArrayList<>();
        for (String resultsUrl : results.getMembersUrls()) {
            Requirement current;
            if (requirementCache.get(resultsUrl) == null) {
                current = doors.getRequirement(resultsUrl);
            } else {
                current = requirementCache.get(resultsUrl);
            }
            Map<String, Object> res = new HashMap<>();

            //VI id,Name,Primary Text,Artifact Type,Owner [S],Rationale [S],Owning System,Notes/Additional Info [REQ],State (Requirement Workflow),VAC,VnV Method [V],VnV Approach [V],Link:Parent Of (<),Link:Child Of (>),Last Modified By,Last Modified Date,Level,Link:Verified or Validated By (<)
            //VA id,Name,Artifact Type,Last Modified By,Last Modified Date,Schedule Start Date [V],Scheduled Completion Date [V],Actual Start Date [V],Actual Completion Date [V],VAC,Link:Verifies or Validates (>),Verif at another level? [V],VA Owner,Venue for R4R [V],Description

            res.put("id", current.getIdentifier());
            res.put("Name", current.getTitle());

            res.put("Primary Text", current.getPrimaryText());
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
            res.put("Last Modified On", current.getModified());
            res.put("Created By", creatorList);
            res.put("Verified or Validated By (<)", current.getValidatedBy());
            res.put("Affected By", linkToList(current.getAffectedBy()));
            res.put("Description", current.getDescription());
            Map<String, URI> propertyMap = createPropertyMap(properties);
            for (Property property : properties) {
                if (property.getTitle() != null) {
                    String value = current.getCustomField(property.getPropertyDefinition());
                    switch (property.getTitle()) {
                        case "Child Of":
                            res.put("Link:Child Of (>)", processLink(value, doors, propertyMap));
                            break;
                        case "Verified or Validated By":
                            res.put("Verified or Validated By (<)", processLink(value, doors, propertyMap));
                            break;
                        case "Parent Of":
                            res.put("Link:Parent Of (<)", processLink(value, doors, propertyMap));
                            break;
                        case "Owning System":
                            try {
                                ResourceShape rs = doors.getResource(ResourceShape.class, new URI(value));
                                System.out.println(rs.getTitle() + " " + rs.);
                                res.put("Owning System",
                                    getKeyByValue(propertyMap, new URI(value)));
                            } catch (URISyntaxException ue) {
                                //Do Nothing
                            }
                            break;
                        default:
                            res.put(property.getTitle(), value != null ? value : "");
                            break;
                    }
                }
            }
            reqs.add(res);
        }

        return reqs;
    }

    private static List<Map<String, Object>> getReqs(String type, DoorsClient doors, Property[] properties) {
        OslcQueryParameters queryParams = new OslcQueryParameters();
        String prefix = "rm=<http://www.ibm.com/xmlns/rdm/rdf/>";
        String where = String.format("rm:ofType=<%s>", type);

        queryParams.setPrefix(prefix);
        queryParams.setWhere(where);
        OslcQueryResult results = doors.submitQuery(queryParams);

        List<Map<String, Object>> reqs = new ArrayList<>();
        for (String resultsUrl : results.getMembersUrls()) {
            Requirement current;
            if (requirementCache.get(resultsUrl) == null) {
                current = doors.getRequirement(resultsUrl);
            } else {
                current = requirementCache.get(resultsUrl);
            }
            Map<String, Object> res = new HashMap<>();
            res.put("id", current.getIdentifier());
            res.put("Name", current.getTitle());

            res.put("Primary Text", current.getPrimaryText());
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
            res.put("Last Modified On", current.getModified());
            res.put("Created By", creatorList);
            res.put("Validated By", linkToList(current.getValidatedBy()));
            res.put("Affected By", linkToList(current.getAffectedBy()));
            res.put("Description", current.getDescription());
            Map<String, URI> propertyMap = createPropertyMap(properties);
            for (Property property : properties) {
                if (property.getTitle() != null) {
                    String value = current.getCustomField(property.getPropertyDefinition());
                    switch (property.getTitle()) {
                        case "Child Of":
                            res.put("Link:Child Of (>)", processLink(value, doors, propertyMap));
                            break;
                        case "Verified or Validated By":
                            res.put("Verified or Validated By (<)", processLink(value, doors, propertyMap));
                            break;
                        case "Parent Of":
                            res.put("Link:Parent Of (<)", processLink(value, doors, propertyMap));
                            break;
                        default:
                            res.put(property.getTitle(), value != null ? value : "");
                            break;
                    }
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

    private static String processLink(String value, DoorsClient doors, Map<String, URI> propertyMap) {
        StringBuilder sb = new StringBuilder();
        if (value != null) {
            value =
                value.startsWith("[") ? value.substring(1, value.length() - 1) : value;
            String[] values = value.split(",");
            for (String val : values) {
                Requirement child = new Requirement();
                String sanitized = val.replaceAll("[\\n\\t ]", "");
                if (requirementCache.get(sanitized) == null) {
                    requirementCache.put(sanitized, doors.getRequirement(sanitized));
                    child = requirementCache.get(sanitized);
                } else {
                    child = requirementCache.get(sanitized);
                }
                sb.append(requirementToString(child, propertyMap));
                sb.append(String.format("%n"));
            }
        }
        return sb.toString();
    }

    private static String requirementToString(Requirement requirement, Map<String, URI> propertyMap) {
        return String.format("%1$s: %2$s {LINK id=%1$s uri=%3$s}",
            requirement.getIdentifier(),
            requirement.getTitle(), requirement.getResourceUrl());
    }

    private static Map<String, URI> createPropertyMap(Property[] properties) {
        HashMap<String, URI> propertyMap = new HashMap<>();
        for (Property property : properties) {
            //System.out.println(property.getTitle() + "\n" + property.getName() + "\n" + property.getPropertyDefinition() + "\n\n\n");
            if (property.getTitle() != null) {
                propertyMap.put(property.getTitle(), property.getPropertyDefinition());
            }
        }
        return propertyMap;
    }

    public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
        for (Map.Entry<T, E> entry : map.entrySet()) {
            if (Objects.equals(value, entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static String toCSV(List<Map<String, Object>> list) {
        List<String> headers = list.stream().flatMap(map -> map.keySet().stream()).distinct().collect(toList());
        final StringBuffer sb = new StringBuffer();
        sb.append("\"");
        for (int i = 0; i < headers.size(); i++) {
            sb.append(sanitize(headers.get(i)));
            sb.append(i == headers.size() - 1 ? "\"\n" : "\",\"");
        }
        for (Map<String, Object> map : list) {
            sb.append("\"");
            for (int i = 0; i < headers.size(); i++) {
                sb.append(sanitize(map.get(headers.get(i))));
                sb.append(i == headers.size() - 1 ? "\"\n" : "\",\"");
            }
        }
        return sb.toString();
    }

    private static String sanitize(Object string) {
        return string != null ? string.toString().replaceAll("\"","\\\\\"") : "";
        //return string != null ? string.toString().replaceAll(",","\\,") : "";
    }
}
