package com.dashx.utils;

import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class Helpers {
    @SuppressWarnings("unchecked")
    public static JSONObject mapToJson(Map<String, Object> map) {
        JSONObject jsonObject = new JSONObject();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                jsonObject.put(key, mapToJson((Map<String, Object>) value));
            } else {
                jsonObject.put(key, value);
            }
        }
        return jsonObject;
    }

    /**
     * Parses a JSON string into a JSONObject.
     *
     * @param jsonString The JSON string to parse
     * @return A JSONObject representing the parsed string
     * @throws JSONException If the string is not valid JSON
     */
    public static JSONObject parseStringToJson(String jsonString) throws JSONException {
        return new JSONObject(jsonString);
    }

    /**
     * Safely parses a JSON string into a JSONObject, returning null if parsing fails.
     *
     * @param jsonString The JSON string to parse
     * @return A JSONObject representing the parsed string, or null if parsing fails
     */
    public static JSONObject parseStringToJsonSafe(String jsonString) {
        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            return null;
        }
    }
}
