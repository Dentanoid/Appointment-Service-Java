package org.example4;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;

public class Utils {
    public static ObjectMapper objectMapper;
    public static ObjectWriter objectWriter;

    // Serialize: Object --> String
    public static void serialize(String filePath, Object object)
    {
        try
        {
            objectWriter.writeValue(new File(filePath), object);
        }
        catch (IOException error)
        {
            error.printStackTrace();
        }
    }

    // Serialize: String --> Object
    public static JsonNode deserialize(String payload) { // The file accessed by 'filePath' is a placeholder that will be replaced by MQTT connections
        // Deserialization
        try
        {
            ObjectMapper objectMapper = new ObjectMapper();

            JsonNode jsonNodeObj = objectMapper.readTree(payload);
            return jsonNodeObj;
        }
        catch (IOException exception)
        {
            exception.printStackTrace();
        }

        return null;
    }
}
