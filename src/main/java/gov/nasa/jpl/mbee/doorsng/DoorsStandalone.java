package gov.nasa.jpl.mbee.doorsng;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URI;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

public class DoorsStandalone {

    private static final Logger logger = Logger.getLogger(DoorsStandalone.class.getName());

    public static void main(String[] args) throws ParseException {

        Options options = new Options();

        options.addOption("action", true, "action");
        options.addOption("resource", true, "resource");
        options.addOption("requirement", true, "requirement");
        options.addOption("collection", true, "collection");
        options.addOption("project",true,"project area");

        CommandLineParser cliParser = new GnuParser();

        CommandLine cmd = cliParser.parse(options, args);

        if (!validateOptions(cmd)) {
            System.out.println("Syntax:  java -jar <jar file> -action \"(get || create || delete)\" -project \"<project_area>\" (-requirement <json> || -collection <collection>)");
            System.out.println("Example: java -jar target/doorsng-0.2.0.jar -action \"create\" -project \"Test Project\" -requirement {\"title\":\"Requirement 01\", \"sysmlid\":\"123-456-678\"} -collection {\"title\":\"Some Collection\",\"uses\": [\"123-456-789\",\"987-654-321\"]}");
            return;
        }

        String action = cmd.getOptionValue("action");
        String resource = cmd.getOptionValue("resource");
        String requirement = cmd.getOptionValue("requirement");
        String collection = cmd.getOptionValue("collection");
        String projectArea = cmd.getOptionValue("project");

        ObjectMapper mapper = new ObjectMapper();
        String response = "";

        try {
            DoorsClient doors = new DoorsClient(projectArea);

            if ("get".equals(action)) {
                if (resource != null) {
                    Requirement req = doors.getRequirement(resource);
                    if (req.getTitle() == null) {
                        RequirementCollection reqCol = doors.getRequirementCollection(resource);
                        response = mapper.writeValueAsString(reqCol);
                    } else {
                        response = mapper.writeValueAsString(req);
                    }
                } else {
                    Requirement[] reqs = doors.getRequirements();
                    response = mapper.writeValueAsString(reqs);
                }
            }
            
            if ("create".equals(action)) {
        	if (requirement != null) {
        	    JsonNode jn = mapper.readTree(requirement);
        	    Requirement req = new Requirement();
                    Iterator it = jn.elements();
                    if (it.hasNext()) {
                        Map.Entry pair = (Map.Entry) it.next();
                        req.setCustomField( doors.getField((String) pair.getKey()), (String) pair.getValue());
                    }
                    response = mapper.writeValueAsString(doors.create(req));
        	} else if (collection != null) {
        	    JsonNode jn = mapper.readTree(collection);
        	    RequirementCollection reqCol = new RequirementCollection();
            	    reqCol.setTitle(jn.get("title").toString());
                    JsonNode uses = jn.get("uses");
                    if (uses.isArray()) {
                        ArrayList<URI> allUses = new ArrayList<URI>();		
                        for (JsonNode use : uses) {
                            allUses.add(URI.create(use.toString()));
                        }
                        reqCol.setUses(allUses.toArray(new URI[allUses.size()]));
                    }
                    response = mapper.writeValueAsString(doors.create(reqCol));
        	}
            }
            //TODO: Finish update functions
            if ("update".equals(action)) {
        	if (requirement != null) {
        	    JsonNode jn = mapper.readTree(requirement);
        	    
        	} else if (collection != null) {
        	    JsonNode jn = mapper.readTree(collection);
        	    
        	}
            }
            
            if ("delete".equals(action)) {
        	response = mapper.writeValueAsString(doors.delete(resource));
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        } finally {
            System.out.println(response);
        }

    }

    private static boolean validateOptions(CommandLine cmd) {
	
	String action = cmd.getOptionValue("action");

        if (action != null && cmd.hasOption("project")) {
            if ("create".equals(action) && (cmd.hasOption("requirement") || cmd.hasOption("collection"))) {
        	return true;
            } else if ("update".equals(action) && (cmd.hasOption("requirement") || cmd.hasOption("collection"))) {
        	return true;
            } else if ("delete".equals(action) && cmd.hasOption("resource")) {
        	return true;
            } else if ("get".equals(action)) {
        	return true;
            }
        }

        return false;

    }
}
