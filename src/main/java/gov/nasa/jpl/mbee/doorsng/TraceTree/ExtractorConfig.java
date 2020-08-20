package gov.nasa.jpl.mbee.doorsng.TraceTree;

import gov.nasa.jpl.mbee.doorsng.DoorsClient;
import java.util.List;
import java.util.Map;
import org.eclipse.lyo.oslc4j.core.model.Property;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public interface ExtractorConfig {

    public Map<String, String> getVATypes();

    public Map<String, String> getVITypes();

    public Map<String, String> getWorkflowMap();

    public List<Map<String, Object>> getVAReqs(String type, DoorsClient doors,
        Property[] properties, Map<String, String> workflowMap);

    public List<Map<String, Object>> getVIReqs(String type, DoorsClient doors,
        Property[] properties, Map<String, String> workflowMap);

    public static String toString(JSONArray ja) throws JSONException {
        JSONObject jo = ja.optJSONObject(0);
        if (jo != null) {
            JSONArray names = jo.names();
            if (names != null) {
                return rowToString(names) + toString(names, ja);
            }
        }

        return null;
    }

    public static String toString(JSONArray names, JSONArray ja) throws JSONException {
        if (names != null && names.length() != 0) {
            StringBuffer sb = new StringBuffer();

            for(int i = 0; i < ja.length(); ++i) {
                JSONObject jo = ja.optJSONObject(i);
                if (jo != null) {
                    sb.append(rowToString(jo.toJSONArray(names)));
                }
            }

            return sb.toString();
        } else {
            return null;
        }
    }

    public static String rowToString(JSONArray ja) {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < ja.length(); ++i) {
            if (i > 0) {
                sb.append(',');
            }

            Object object = ja.opt(i);
            if (object == null) {
                object = " ";
            }
            String string = object.toString();
            if (string.length() > 0 && (string.indexOf(44) >= 0 || string.indexOf(10) >= 0
                || string.indexOf(13) >= 0 || string.indexOf(0) >= 0 || string.charAt(0) == '"')) {
                sb.append('"');
                int length = string.length();

                for (int j = 0; j < length; ++j) {
                    char c = string.charAt(j);
                    if (c >= ' ' && c != '"') {
                        sb.append(c);
                    }
                }

                sb.append('"');
            } else {
                sb.append(string);
            }
        }

        sb.append('\n');
        return sb.toString();
    }
}
