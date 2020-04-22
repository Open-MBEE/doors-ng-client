package gov.nasa.jpl.mbee.doorsng.TraceTree;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

public class TraceTreePsycheConfig implements TraceTreeConfig {

    private static final Logger logger = Logger.getLogger(TraceTreePsycheConfig.class.getName());

    private static final List<String> vaTypes = Arrays.asList(
        "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_wG6-UVH1EeeZqqBXHGi26w"
    );

    private static final List<String> viTypes = Arrays.asList(
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

    private static final HashMap<String, String> viHeaders;
    static {
        viHeaders = new HashMap<>();
        //viHeaders.put("", "id");
        //viHeaders.put("", "Name");
        //viHeaders.put("", "Artifact Type");
        //viHeaders.put("", "Last Modified By");
        //viHeaders.put("", "Last Modified Date");
        //viHeaders.put("", "Schedule Start Date [V]");
        //viHeaders.put("", "Scheduled Completion Date [V]");
        //viHeaders.put("", "Actual Start Date [V]");
        //viHeaders.put("", "Actual Completion Date [V]");
        viHeaders.put("https://cae-jazz-uat.jpl.nasa.gov/rm/types/_vS4SQVH1EeeZqqBXHGi26w", "VAC");
        //viHeaders.put("", "Link:Verifies or Validates (>)");
        viHeaders.put("https://cae-jazz-uat.jpl.nasa.gov/rm/types/_vRrYYVH1EeeZqqBXHGi26w", "Verif at another level? [V]");
        //viHeaders.put("", "VA Owner");
        //viHeaders.put("", "Venue for R4R [V]");
        //viHeaders.put("", "Description");
    }

    public HashMap<String, String> getVAHeaders() {
        return vaHeaders;
    }

    public HashMap<String, String> getVIHeaders() {
        return viHeaders;
    }

    public List<String> getVATypes() {
        return vaTypes;
    }

    public List<String> getVITypes() {
        return viTypes;
    }
}
