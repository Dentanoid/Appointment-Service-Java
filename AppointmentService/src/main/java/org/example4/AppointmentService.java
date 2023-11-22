package org.example4;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;

import java.util.Iterator;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example4.Schemas.Appointments;
import org.example4.Schemas.AvailableTimes;
import org.example4.TopicManagement.TopicManager;

public class AppointmentService {
    public static MqttMain mqttManager1;
    public static MqttMain mqttManager2;

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

        TopicManager topicManager = new TopicManager(topic, payload);
    }

    /*
    private static void patientDeleteAppointment(String payload) {
        JsonNode jsonNode = Utils.deserialize(payload);
        String objectId = jsonNode.get("_id").toString();

        ObjectId appointmentId;

        try {
            appointmentId = new ObjectId(objectId);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid ObjectId format");
            return;
        }

        Bson searchQuery = new Document("_id", appointmentId);

        try {
            Document foundDocument = DatabaseManager.appointmentsCollection.find(searchQuery).first();

            if (foundDocument != null) {
                // Delete from the Appointments collection and get the document
                Document deletedDocument = DatabaseManager.appointmentsCollection.findOneAndDelete(searchQuery);
                // MqttManager.getMqttManager().publishMessage("grp20/notification/patient/cancel", deletedDocument.toJson());
                mqttManager.publishMessage("grp20/notification/patient/cancel", deletedDocument.toJson());

                // Remove the "patient_id" field from the document
                deletedDocument.remove("patient_id");

                // Insert the modified document into the AvailableTimes collection
                DatabaseManager.availableTimesCollection.insertOne(deletedDocument);

                System.out.println("Document deleted, patient_id removed, and migrated successfully.");
            } else {
                System.out.println("Object with this objectId is not found");
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }
    */
}