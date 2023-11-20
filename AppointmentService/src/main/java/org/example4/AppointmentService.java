package org.example4;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example4.Schemas.Appointments;
import org.example4.Schemas.AvailableTimes;
import org.example4.Schemas.CollectionSchema;

public class AppointmentService {
    private static MongoClient client;
    private static MongoDatabase appointmentDatabase;
    private static MqttMain mqttMain;
    private static MongoCollection<Document> availableTimesCollection;
    private static MongoCollection<Document> appointmentsCollection;

    public static void main(String[] args) {
        initializeDatabaseConnection();
        initializeMqttConnection();
    }

    private static void initializeDatabaseConnection() {
        client = MongoClients.create("mongodb+srv://DentistUser:dentist123@dentistsystemdb.7rnyky8.mongodb.net/?retryWrites=true&w=majority");
        appointmentDatabase = client.getDatabase("AppointmentService");
        availableTimesCollection = appointmentDatabase.getCollection("AvailableTimes");
        appointmentsCollection = appointmentDatabase.getCollection("Appointments");
    }

    private static void initializeMqttConnection() {
        mqttMain = new MqttMain("tcp://broker.hivemq.com:1883");

        // Temporary sub-topics:
        // 1) "sub/availabletime/create" --> Dentist (WORKS)

        // 2) "sub/appointments/delete" --> Dentist
        // 3) "sub/appointments/create" --> Patient (WORKS)
        // 4) "sub/appointments/cancel" --> Patient

        mqttMain.subscribe("sub/appointments/delete");
    }

    // Once this service has recieved the payload, it has to be managed
    public static void manageRecievedPayload(String topic, String payload) {

        // Dentist
        if (topic.contains("availabletime")) {
            // Create available time
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
        Document availableTimesDocument = convertStringToDocument(payload, new AvailableTimes());
        availableTimesCollection.insertOne(availableTimesDocument);

        mqttMain.publishMessage("pub/availabletime/create", availableTimesDocument.toJson());
    }

    // Patient registers on existing slot found in 'AvailableTimes' collection
    private static void patientCreateAppointment(String payload) {
        Document appointmentDocument = convertStringToDocument(payload, new Appointments());
        appointmentsCollection.insertOne(appointmentDocument);
        mqttMain.publishMessage("pub/appointments/create", appointmentDocument.toJson());
    }

    // Delete instance from 'AvailableTimes' collection
    private static void dentistDeleteAppointment(String payload) {
        // This method recieves a payload that contains the objectId to delete and the other appointment attributes to be sent in the notification

        System.out.println(payload);

        try {
            // ObjectId appointmenObjectId = new ObjectId(appointmentId);
            ObjectId appointmenObjectId = new ObjectId(payload);

            Bson searchQuery = new Document("_id", appointmenObjectId);
            Document document = appointmentsCollection.findOneAndDelete(searchQuery);

            availableTimesCollection.findOneAndDelete(searchQuery);

            mqttMain.publishMessage("pub/appointments/delete", document.toJson());
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
            Document foundDocument = appointmentsCollection.find(searchQuery).first();

            if (foundDocument != null) {
                // Delete from the Appointments collection and get the document
                Document deletedDocument = appointmentsCollection.findOneAndDelete(searchQuery);
                mqttMain.publishMessage("grp20/notification/patient/cancel", deletedDocument.toJson());

                // Remove the "patient_id" field from the document
                deletedDocument.remove("patient_id");

                // Insert the modified document into the AvailableTimes collection
                availableTimesCollection.insertOne(deletedDocument);

                System.out.println("Document deleted, patient_id removed, and migrated successfully.");
            } else {
                System.out.println("Object with this objectId is not found");
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    // IDEA: Refactor into MongoDBSchema.java:

    // Convert the payload-string to a document that can be stored in the database
    private static Document convertStringToDocument(String payload, CollectionSchema classSchema) {
        Gson gson = new Gson();
        CollectionSchema schemaClass = gson.fromJson(payload, classSchema.getClass());
        return schemaClass.getDocument();
    }
}