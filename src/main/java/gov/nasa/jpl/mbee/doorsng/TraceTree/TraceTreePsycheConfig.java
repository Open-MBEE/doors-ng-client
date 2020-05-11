package gov.nasa.jpl.mbee.doorsng.TraceTree;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TraceTreePsycheConfig implements TraceTreeConfig {

    private static final Logger logger = Logger.getLogger(TraceTreePsycheConfig.class.getName());

    private static final Map<String, String> vaTypes;
    static {
        Map<String, String> temp = new HashMap<>();
        temp.put("V&V Activity", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_wG6-UVH1EeeZqqBXHGi26w");
        vaTypes = Collections.unmodifiableMap(temp);
    }

    private static final Map<String, String> viTypes;
    static {
        Map<String, String> temp = new HashMap<>();
        temp.put("Requirement", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_wrFbUVH1EeeZqqBXHGi26w");
        temp.put("Requirement - AVS", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_d3ATkUcREeiggbwaJnQh2g");
        temp.put("Requirement - FS Implementer", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_MZG5UEcREeiggbwaJnQh2g");
        temp.put("Requirement - MS Implementer", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_MIsfkVMWEeiggbwaJnQh2g");
        temp.put("Requirement - PLD Implementer", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_TmaY4VMXEeiggbwaJnQh2g");
        viTypes = Collections.unmodifiableMap(temp);
    }

    public Map<String, String> getVATypes() {
        return vaTypes;
    }

    public Map<String, String> getVITypes() {
        return viTypes;
    }
/*
    'id',
    'Name',
    'Artifact Type',
    'Last Modified By',
    'Last Modified Date',
    'Schedule Start Date [V]',
    'Scheduled Completion Date [V]',
    'Actual Start Date [V]',
    'Actual Completion Date [V]',
    'VAC',
    'Link:Verifies or Validates (>)',
    'Verif at another level? [V]',
    'VA Owner',
    'Venue for R4R [V]',
    'Description'

            "Actual Completion Date [V]",
            "Actual Start Date [V]",
            "id",
            "Link:Verifies or Validates (>)",
            "Name",
            "Description",
            "Schedule Start Date [V]",
            "Scheduled Completion Date [V]",
            "VA Owner",
            "VAC",
            "Venue for R4R [V]",



    'id',
    'Name',
    'Primary Text',
    'Artifact Type',
    'Owner [S]',
    'Rationale [S]',
    'Owning System',
    'Notes/Additional Info [REQ]',
    'State (Requirement Workflow)',
    'VAC',
    'VnV Method [V]',
    'VnV Approach [V]',
    'Link:Parent Of (<)',
    'Link:Child Of (>)',
    'Last Modified By',
    'Last Modified Date',
    'Level',
    'Link:Verified or Validated By (<)'

            "id",
            "Name",
            "Primary Text",
            "Artifact Type",
            "Owner [S]",
            "Rationale [S]",
            "Owning System",
            "Notes/Additional Info [REQ]",
            "State (Requirement Workflow)",
            "VAC",
            "VnV Method [V]",
            "VnV Approach [V]",
            "Link:Parent Of (<)",
            "Link:Child Of (>)",
            "Last Modified By",
            "Last Modified Date",
            "Level",
            "Link:Verified or Validated By (<)",
 */
}
