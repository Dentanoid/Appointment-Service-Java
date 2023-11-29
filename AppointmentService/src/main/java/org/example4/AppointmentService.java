package org.example4;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Iterator;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example4.DatabaseManagement.DatabaseManager;
import org.example4.DatabaseManagement.Schemas.Appointments;
import org.example4.DatabaseManagement.Schemas.AvailableTimes;
import org.example4.TopicManagement.TopicManager;

public class AppointmentService {
    public static void main(String[] args) {
        DatabaseManager.initializeDatabaseConnection();
        // DatabaseManager.deleteAllCollectionInstances(); // <-- For developers when testing

        MqttMain.initializeMqttConnection();
    }

    // Once this service has recieved the payload, it has to be managed
    public static void manageRecievedPayload(String topic, String payload) {
        System.out.println("**********************************************");
        System.out.println("MANAGE RECIEVED PAYLOAD");
        System.out.println("**********************************************");

        // TopicManager topicManager = new TopicManager(topic, payload);
        TopicManager topicManager = new TopicManager();
        topicManager.manageTopic(topic, payload);
    }
}