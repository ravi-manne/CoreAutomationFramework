package core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;

public class JsonUtils {

    private JsonNode rootNode;

    // Constructor to load the JSON file
    public JsonUtils(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        rootNode = objectMapper.readTree(new File(filePath));
    }

    // Method to get data by a specific key
    public String getValue(String key) {
        JsonNode valueNode = rootNode.path(key);
        return valueNode.isMissingNode() ? null : valueNode.asText();
    }

    // Method to get all data as a Map
    public Map<String, String> getAllData() {
        Map<String, String> jsonData = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> fields = rootNode.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            jsonData.put(field.getKey(), field.getValue().asText());
        }
        return jsonData;
    }

    // Method to get nested data as a Map
    public Map<String, String> getNestedData(String parentKey) {
        Map<String, String> nestedData = new HashMap<>();
        JsonNode parentNode = rootNode.path(parentKey);

        if (!parentNode.isMissingNode() && parentNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = parentNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                nestedData.put(field.getKey(), field.getValue().asText());
            }
        }
        return nestedData;
    }
}
