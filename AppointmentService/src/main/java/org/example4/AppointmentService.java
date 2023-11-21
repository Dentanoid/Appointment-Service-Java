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
    // public static MqttManager mqttMain;

    public static void main(String[] args) {
        DatabaseManager.initializeDatabaseConnection();
        MqttManager.initializeSubscriptions();
    }

    // Once this service has recieved the payload, it has to be managed
    public static void manageRecievedPayload(String topic, String payload) {
        TopicManager topicManager = new TopicManager(topic, payload);
        // topicManager.client.executeRequestedOperation(topic);

        // topicManager.client.createAppointment();
        // topicManager.client.deleteAppointment();

        /*
        if (topic.contains("availabletime")) {
            // Dentist creates available time
            if (topic.contains("create")) {
                dentistCreateAvailableTime(payload);
            }
        }
        // Perform operations on already agreed timeslots between dentists and patients
        else if (topic.contains("appointments"))
        {
            // Patient books appointment
            if (topic.contains("create")) {
                patientCreateAppointment(payload);
            }
            // Dentist deletes booked appointment
            else if (topic.contains("delete")) {
                dentistDeleteAppointment(payload);
            }
            // Patient cancels appointments
            else if (topic.contains("cancel")) {
                patientDeleteAppointment(payload);
            }
        }
        */
    }

    // POST - Create new instance in database
    private static void addCollection(MongoCollection<Document> coll, Document dataToRegister) {
        coll.insertOne(dataToRegister);
    }

    // READ - Get all instances in a collection
    private static void printCollection(MongoCollection<Document> collection) {
        FindIterable<Document> documents = collection.find();
        Iterator<Document> it = documents.iterator();
        while (it.hasNext()) {
            Document doc = it.next();
            System.out.println(doc.toJson());
        }
    }

    // POST - Dentist creates a timeslot in which patients can book appointments
    private static void dentistCreateAvailableTime(String payload) {
        // Document availableTimesDocument = convertStringToDocument(payload, new AvailableTimes());
        // DatabaseManager.availableTimesCollection.insertOne(availableTimesDocument);

        Document availableTimesDocument = DatabaseManager.convertPayloadToDocument(payload, new AvailableTimes());        
        DatabaseManager.saveDocumentInCollection(DatabaseManager.availableTimesCollection, availableTimesDocument);

        MqttManager.getMqttManager().publishMessage("pub/availabletime/create", availableTimesDocument.toJson());
    }

    // Patient registers on existing slot found in 'AvailableTimes' collection
    private static void patientCreateAppointment(String payload) {
        // Document appointmentDocument = convertStringToDocument(payload, new Appointments());
        // DatabaseManager.appointmentsCollection.insertOne(appointmentDocument);

        Document appointmentDocument = DatabaseManager.convertPayloadToDocument(payload, new Appointments());
        DatabaseManager.saveDocumentInCollection(DatabaseManager.appointmentsCollection, appointmentDocument);

        MqttManager.getMqttManager().publishMessage("pub/appointments/create", appointmentDocument.toJson());
    }

    // Delete instance from 'AvailableTimes' collection
    private static void dentistDeleteAppointment(String payload) {
        // This method recieves a payload that contains the objectId to delete and the other appointment attributes to be sent in the notification

        // TODO: Get ObjectId from db instance:
        // 1) query DB to get the instance
        // 2)
        System.out.println(payload);

        try {
            // ObjectId appointmenObjectId = new ObjectId(appointmentId);
            ObjectId appointmenObjectId = new ObjectId(payload);

            Bson searchQuery = new Document("_id", appointmenObjectId);
            Document document = DatabaseManager.appointmentsCollection.findOneAndDelete(searchQuery);

            DatabaseManager.availableTimesCollection.findOneAndDelete(searchQuery);

            MqttManager.getMqttManager().publishMessage("pub/appointments/delete", document.toJson());
            System.out.println("Appointment deleted successfully.");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

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
                MqttManager.getMqttManager().publishMessage("grp20/notification/patient/cancel", deletedDocument.toJson());

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

    // IDEA: Refactor into MongoDBSchema.java:

    /*
    // Convert the payload-string to a document that can be stored in the database
    private static Document convertStringToDocument(String payload, CollectionSchema classSchema) {
        Gson gson = new Gson();
        CollectionSchema schemaClass = gson.fromJson(payload, classSchema.getClass());
        return schemaClass.getDocument();
    }
    */
}