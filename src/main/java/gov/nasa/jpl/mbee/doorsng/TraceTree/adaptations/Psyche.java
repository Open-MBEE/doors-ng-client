package gov.nasa.jpl.mbee.doorsng.TraceTree.adaptations;

import gov.nasa.jpl.mbee.doorsng.DoorsClient;
import gov.nasa.jpl.mbee.doorsng.TraceTree.ExtractorConfig;
import gov.nasa.jpl.mbee.doorsng.model.Person;
import gov.nasa.jpl.mbee.doorsng.model.Requirement;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.wink.client.ClientResponse;
import org.eclipse.lyo.client.oslc.resources.OslcQueryParameters;
import org.eclipse.lyo.client.oslc.resources.OslcQueryResult;
import org.eclipse.lyo.oslc4j.core.model.Link;
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.eclipse.lyo.oslc4j.core.model.ResourceShape;

public class Psyche implements ExtractorConfig {

    private static final Logger logger = Logger.getLogger(Psyche.class.getName());

    private static Map<String, Requirement> requirementCache = new HashMap<>();
    private static Map<URI, ResourceShape> resourceShapeCache = new HashMap<>();
    private static Map<URI, Object> listCache = new HashMap<>();
    private static Map<String, Set<String>> childrenMap = new HashMap<>();

    private static final Map<String, String> vaTypes;
    static {
        Map<String, String> temp = new HashMap<>();
        //temp.put("V&V Activity", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_wG6-UVH1EeeZqqBXHGi26w");
        temp.put("V&V Activity", "https://cae-jazz.jpl.nasa.gov/rm/types/_wG6-UVH1EeeZqqBXHGi26w");
        vaTypes = Collections.unmodifiableMap(temp);
    }

    private static final Map<String, String> viTypes;
    static {
        Map<String, String> temp = new HashMap<>();
        //temp.put("Requirement", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_wrFbUVH1EeeZqqBXHGi26w");
        //temp.put("Requirement - AVS", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_d3ATkUcREeiggbwaJnQh2g");
        //temp.put("Requirement - FS Implementer", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_MZG5UEcREeiggbwaJnQh2g");
        //temp.put("Requirement - MS Implementer", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_MIsfkVMWEeiggbwaJnQh2g");
        //temp.put("Requirement - PLD Implementer", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_TmaY4VMXEeiggbwaJnQh2g");
        temp.put("Requirement", "https://cae-jazz.jpl.nasa.gov/rm/types/_wrFbUVH1EeeZqqBXHGi26w");
        temp.put("Requirement - AVS", "https://cae-jazz.jpl.nasa.gov/rm/types/_d3ATkUcREeiggbwaJnQh2g");
        temp.put("Requirement - FS Implementer", "https://cae-jazz.jpl.nasa.gov/rm/types/_MZG5UEcREeiggbwaJnQh2g");
        temp.put("Requirement - MS Implementer", "https://cae-jazz.jpl.nasa.gov/rm/types/_MIsfkVMWEeiggbwaJnQh2g");
        temp.put("Requirement - PLD Implementer", "https://cae-jazz.jpl.nasa.gov/rm/types/_TmaY4VMXEeiggbwaJnQh2g");
        viTypes = Collections.unmodifiableMap(temp);
    }

    private static final Map<String, String> workflowMap;
    static {
        Map<String, String> temp = new HashMap<>();

        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s0", "[REQ] Draft");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s1", "[REQ] Preliminary");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s2", "[REQ] Baselined");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s3", "[VnV] Planned");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s4", "[VnV] In Progress");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s5", "[VnV] Needs Closure Statement");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s6", "[VnV] Needs Approval");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s7", "[VnV] Needs Audit");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s8", "[VnV] Closed");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s9", "[VnV] Needs Rework");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s10", "[CM] Modifying");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s11", "[CM] Locked");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s12", "[CM] Archived");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s13", "[REQ] To be Deleted");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s14", "[REQ] To be Moved to L3");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s15", "[REQ] To be Moved to L4");
        temp.put("http://www.ibm.com/xmlns/rdm/workflow/Requirement_Workflow#Requirement_Workflow.state.s16", "[REQ] Parking Lot");

        workflowMap = Collections.unmodifiableMap(temp);
    }

    public Map<String, String> getVATypes() {
        return vaTypes;
    }

    public Map<String, String> getVITypes() {
        return viTypes;
    }

    public Map<String, String> getWorkflowMap() {
        return workflowMap;
    }

    public List<Map<String, Object>> getVIReqs(String type, DoorsClient doors, Property[] properties, Map<String, String> workflowMap) {
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
                try {
                    ClientResponse clientResponse = doors.getResponse(resultsUrl);
                    String currentReq = clientResponse.getEntity(String.class);
                    BufferedWriter writer = new BufferedWriter(new FileWriter("vis/" + i + ".xml", true));
                    writer.append(currentReq);
                    writer.close();
                    i++;
                    continue;
                } catch (Exception e) {
                    //
                }
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
            //res.put("Primary Text", "Currently not supported");

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
                //res.put("Link:Verified or Validated By (<)", processLink(sb.toString(), doors));
            }
            res.put("Affected By", linkToList(current.getAffectedBy()));
            res.put("Description", current.getDescription());
            for (Property property : properties) {
                if (property.getTitle() != null) {
                    String value = current.getCustomField(property.getPropertyDefinition());
                    switch (property.getTitle()) {
                        case "Child Of":
                            res.put("Link:Child Of (>)", processLink(value, doors));
                            break;
                        case "Validated By":
                        case "Verified or Validated By (<)":
                            res.put("Link:Verified or Validated By (<)", processLink(value, doors));
                            break;
                        case "Verifies or Validates":
                            res.put("Link:Verifies or Validates (>)", processLink(value, doors));
                            break;
                        case "Child Systems":
                        case "Child Disposition - SSE":
                            res.put(property.getTitle(), processProperties(value, doors));
                            break;
                        case "Parent Of":
                            res.put("Link:Parent Of (<)", value);
                            break;
                        case "Activity Type":
                        case "VnV Method [V]":
                        case "VnV Priority [V]":
                        case "Level":
                        case "VAC":
                        case "Owning System":
                            if (value != null) {
                                try {
                                    URI key = new URI(value);
                                    if (!listCache.containsKey(key)) {
                                        String[] spl = value.split("#");
                                        ClientResponse response = doors.getResponse(spl[0]);
                                        parseListRdf(response.getEntity(String.class));
                                    }
                                    res.put(property.getTitle(), listCache.get(key));
                                } catch (URISyntaxException ue) {
                                    //Do Nothing
                                } catch (Exception e) {
                                    // Nothing
                                }
                            } else {
                                res.put(property.getTitle(), "");
                            }
                            break;
                        case "State (Requirement Workflow)":
                            String stringVal = "";
                            if (value != null) {
                                try {
                                    URI key = new URI(value);
                                    stringVal = listCache.containsKey(key) ? (String) listCache.get(key) : workflowMap.getOrDefault(value, "");
                                } catch (URISyntaxException ue) {
                                    //
                                }
                            }
                            res.put(property.getTitle(), stringVal);
                            break;
                        case "VA Owner":
                        case "Owner [S]":
                            if (value != null) {
                                ClientResponse clientResponse = doors.getResponse(value);
                                Person owner = clientResponse.getEntity(Person.class);
                                res.put(property.getTitle(), owner != null ? owner.getName() : "");
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

        return appendChildren(reqs);
    }

    public List<Map<String, Object>> getVAReqs(String type, DoorsClient doors, Property[] properties, Map<String, String> workflowMap) {
        OslcQueryParameters queryParams = new OslcQueryParameters();
        String prefix = "rm=<http://www.ibm.com/xmlns/rdm/rdf/>";

        queryParams.setSelect("*");
        queryParams.setPrefix(prefix);
        OslcQueryResult results = doors.submitQuery(queryParams);

        List<Map<String, Object>> reqs = new ArrayList<>();
        int i = 0;
        for (String resultsUrl : results.getMembersUrls()) {
            Requirement current;
            if (requirementCache.get(resultsUrl) == null) {
                try {
                    ClientResponse clientResponse = doors.getResponse(resultsUrl);
                    String currentReq = clientResponse.getEntity(String.class);
                    BufferedWriter writer = new BufferedWriter(new FileWriter("vas/" + i + ".xml", true));
                    writer.append(currentReq);
                    writer.close();
                    i++;
                    continue;
                } catch (Exception e) {
                    //
                }
                current = doors.getRequirement(resultsUrl);
            } else {
                current = requirementCache.get(resultsUrl);
            }

            if (current == null) {
                continue;
            }

            Map<String, Object> res = new HashMap<>();

            res.put("about", current.getAbout().toString());

            res.put("id", current.getIdentifier());
            res.put("Name", current.getTitle());

            res.put("Primary Text", current.getPrimaryText());
            //res.put("Primary Text", "Currently not supported");
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
                //res.put("Link:Verified or Validated By (<)", processLink(sb.toString(), doors));
            }
            res.put("Affected By", linkToList(current.getAffectedBy()));
            res.put("Description", current.getDescription());
            for (Property property : properties) {
                if (property.getTitle() != null) {
                    String value = current.getCustomField(property.getPropertyDefinition());
                    switch (property.getTitle()) {
                        case "Child Of":
                            processParents(value, current);
                            res.put("Link:Child Of (>)", processLink(value, doors));
                            break;
                        case "Validated By":
                        case "Verified or Validated By (<)":
                            res.put("Link:Verified or Validated By (<)", processLink(value, doors));
                            break;
                        case "Verifies or Validates":
                            res.put("Link:Verifies or Validates (>)", processLink(value, doors));
                            break;
                        case "Child Systems":
                        case "Child Disposition - SSE":
                            res.put(property.getTitle(), processProperties(value, doors));
                            break;
                        case "Parent Of":
                            res.put("Link:Parent Of (<)", value);
                            break;
                        case "Activity Type":
                        case "VnV Method [V]":
                        case "VnV Priority [V]":
                        case "Level":
                        case "VAC":
                        case "Owning System":
                            if (value != null) {
                                try {
                                    URI key = new URI(value);
                                    if (!listCache.containsKey(key)) {
                                        String[] spl = value.split("#");
                                        ClientResponse response = doors.getResponse(spl[0]);
                                        parseListRdf(response.getEntity(String.class));
                                    }
                                    res.put(property.getTitle(), listCache.get(key));
                                } catch (URISyntaxException ue) {
                                    //Do Nothing
                                } catch (Exception e) {
                                    // Nothing
                                }
                            } else {
                                res.put(property.getTitle(), "");
                            }
                            break;
                        case "State (Requirement Workflow)":
                            String stringVal = "";
                            if (value != null) {
                                try {
                                    URI key = new URI(value);
                                    stringVal = listCache.containsKey(key) ? (String) listCache.get(key) : workflowMap.getOrDefault(value, "");
                                } catch (URISyntaxException ue) {
                                    //
                                }
                            }
                            res.put(property.getTitle(), stringVal);
                            break;
                        case "VA Owner":
                        case "Owner [S]" :
                            if (value != null) {
                                ClientResponse clientResponse = doors.getResponse(value);
                                Person vaowner = clientResponse.getEntity(Person.class);
                                res.put("VA Owner", vaowner != null ? vaowner.getName() : "");
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

        return appendChildren(reqs);
    }


    private static List<String> linkToList(Link[] links) {
        List<String> result = new ArrayList<>();
        for (Link link : links) {
            result.add(link.getLabel());
        }
        return result;
    }

    private static String processLink(String value, DoorsClient doors) {
        StringBuilder sb = new StringBuilder();
        if (value != null) {
            value =
                value.startsWith("[") ? value.substring(1, value.length() - 1) : value;
            String[] values = value.split(",");
            for (String val : values) {
                Requirement child = new Requirement();
                String sanitized = val.replaceAll("[\\n\\t ]", "");
                if (!sanitized.isEmpty() && requirementCache.get(sanitized) == null) {
                    Requirement pulledChild = doors.getRequirement(sanitized);
                    if (pulledChild != null) {
                        //System.out.println("ProcessLink got requirement: " + pulledChild.getIdentifier());
                        requirementCache.put(sanitized, pulledChild);
                        child = requirementCache.get(sanitized);
                    }
                } else if (!sanitized.isEmpty()) {
                    child = requirementCache.get(sanitized);
                }
                sb.append(requirementToString(child));
                sb.append(String.format("%n"));
            }
        }
        return sb.toString();
    }

    private static void processParents(String parents, Requirement child) {
        if (parents != null) {
            parents =
                parents.startsWith("[") ? parents.substring(1, parents.length() - 1) : parents;
            String childString = requirementToString(child);
            String[] values = parents.split(",");
            for (String val : values) {
                String sanitized = val.replaceAll("[\\n\\t ]", "");
                Set<String> children = childrenMap.getOrDefault(sanitized, new HashSet<>());
                children.add(childString);
                childrenMap.put(sanitized, children);
            }
        }
    }

    private static List<Map<String, Object>> appendChildren(List<Map<String, Object>> results) {
        List<Map<String, Object>> appended = new ArrayList<>();
        for (Map<String, Object> result : results) {
            if (result.getOrDefault("about", null) != null) {
                if (childrenMap.getOrDefault(result.get("about").toString(), null) != null) {
                    System.out.println("Children found!");
                    StringBuilder sb = new StringBuilder();
                    for (String child : childrenMap.get(result.get("about").toString())) {
                        sb.append(child);
                        sb.append(String.format("%n"));
                    }
                    result.put("Link: Parent Of (<)", sb.toString());
                }
            }
            appended.add(result);
        }
        return appended;
    }

    private static String requirementToString(Requirement requirement) {
        return String.format("%1$s: %2$s {LINK id=%1$s uri=%3$s}",
            requirement.getIdentifier(),
            requirement.getTitle(), requirement.getAbout());
    }

    public static String processProperties(String value, DoorsClient doors) {
        StringBuilder sb = new StringBuilder();
        if (value != null) {
            value =
                value.startsWith("[") ? value.substring(1, value.length() - 1) : value;
            String[] values = value.split(",");
            for (String val : values) {
                if (val == null) {
                    continue;
                }

                try {
                    String sanitized = val.replaceAll("[\\n\\t ]", "");
                    URI key = new URI(sanitized);
                    System.out.println("ProcessLink checking: " + sanitized);
                    if (!listCache.containsKey(key)) {
                        String[] spl = value.split("#");
                        ClientResponse response = doors.getResponse(spl[0]);
                        parseListRdf(response.getEntity(String.class));
                    }
                    sb.append(listCache.get(key));
                    sb.append(String.format("%n"));
                } catch (URISyntaxException ue) {
                    //Do Nothing
                } catch (Exception e) {
                    // Nothing
                }
            }
        }
        return sb.toString();
    }

    public static void parseListRdf(String rdf) {
        Model m = ModelFactory.createDefaultModel();
        m.read(new ByteArrayInputStream(rdf.getBytes()), null, "RDF/XML");
        for (StmtIterator it = m.listStatements(); it.hasNext(); ) {
            Statement statement = it.next();
            try {
                URI uri = new URI(statement.getSubject().getURI());
                if (listCache.containsKey(uri)) {
                    continue;
                }
                if (statement.getSubject().toString().equals(uri.toString())) {
                    RDFNode resource = statement.getObject();
                    if (resource.isResource()) {
                        Resource resourceAsResource = resource.asResource();
                        //System.out.println("RESOURCE URI: " + resourceAsResource.getURI());
                        listCache.put(uri, resourceAsResource.toString());
                    } else if (resource.isLiteral()) {
                        Literal resourceAsLiteral = resource.asLiteral();
                        //System.out.println("RESOURCE LITERAL: " + resourceAsLiteral.getString());
                        listCache.put(uri, resourceAsLiteral.getValue().toString());
                    } else {
                        listCache.put(uri, resource.toString());
                    }
                }
            } catch (URISyntaxException use) {
                // Nothing
            }

        }
    }
}
