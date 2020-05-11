package gov.nasa.jpl.mbee.doorsng.TraceTree;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TraceTreeEuropaConfig implements TraceTreeConfig {

    private static final Logger logger = Logger.getLogger(TraceTreePsycheConfig.class.getName());

    private static final Map<String, String> vaTypes;
    static {
        Map<String, String> temp = new HashMap<>();
        temp.put("V&V Activity", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_4lqWcYAJEeqe14cBdsd0Qw");
        vaTypes = Collections.unmodifiableMap(temp);
    }

    private static final Map<String, String> viTypes;
    static {
        Map<String, String> temp = new HashMap<>();
        temp.put("Requirement", "https://cae-jazz-uat.jpl.nasa.gov/rm/types/_KOzXN23VEeWoy88nVKDVYg");
        viTypes = Collections.unmodifiableMap(temp);
    }

    public Map<String, String> getVATypes() {
        return vaTypes;
    }

    public Map<String, String> getVITypes() {
        return viTypes;
    }
}
