package org.example4;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import java.io.File;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;

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

    public static String getSubstringAtIndex(String topic, int i, boolean getLastIndex) {
        String[] splitString = topic.split("/");
        return topic.split("/")[getLastIndex ? splitString.length - 1 : i];
    }

    /*
    public static boolean isOverlapping(String ) {
        LocalTime startA = LocalTime.of( 11 , 00 );
        LocalTime stopA = LocalTime.of( 12 , 00 );

        LocalTime startB = LocalTime.of( 11 , 59 );
        LocalTime stopB = LocalTime.of( 14 , 00 );

        boolean isOverlapping = (startA.isBefore(stopB) && stopA.isAfter(startB)) || (startB.isBefore(startA) && startB.isAfter(stopA));
    }
    */
}
