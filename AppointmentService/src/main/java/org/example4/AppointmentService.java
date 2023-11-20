package org.example4;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;

public class AppointmentService {
    public static void main(String[] args) {
        MongoClient client = MongoClients.create("mongodb+srv://DentistUser:dentist123@dentistsystemdb.7rnyky8.mongodb.net/?retryWrites=true&w=majority");
        MongoDatabase appointmentDatabase = client.getDatabase("AppointmentService");
        MqttMain mqttMain = new MqttMain("tcp://broker.hivemq.com:1883");

        // mqttMain.subscribe("my/test/topic");
        

        // dentistDeleteAppointment(appointmentDatabase, "78", "92", "754");
        createAvailableTime(appointmentDatabase);
        // createAppointment(appointmentDatabase, mqttMain);
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
    private static void createAvailableTime(MongoDatabase appointmentDatabase) {
        // TODO:
        // 1) Verify that the topic containts 'dentist'

        MongoCollection<Document> availableTimesCollection = appointmentDatabase.getCollection("AvailableTimes");
        availableTimesCollection.insertOne(makeAvailableTimeDocument());
    }

    // Patient registers on existing slot found in 'AvailableTimes' collection
    private static void createAppointment(MongoDatabase appointmentDatabase, MqttMain mqttMain) {
        // TODO:
        // 1) Verify that the topic containts 'patient'
        // 2) Delete corresponding appointment-data-instance from 'AvailableTimes' collection
        // 3) Create appointment in 'Appointments' collection


        MongoCollection<Document> appointmentsCollection = appointmentDatabase.getCollection("Appointments");
        Document appointmentDocument = makeAppointmentsDocument();
        appointmentsCollection.insertOne(appointmentDocument);

        mqttMain.publishMessage("my/test/topic", appointmentDocument.toJson());
        // MqttPublishSample mqttPublishSample = new MqttPublishSample("my/test/topic", appointmentDocument.toJson());
    }

    // IDEA: Refactor into MongoDBSchema.java:

    private static Document makeAvailableTimeDocument() {
        return new Document("clinic_id", "70")
            .append("dentist_id",  "40")
            .append("start_time", "10:30")
            .append("end_time", "11:30");
    }

    private static Document makeAppointmentsDocument() {
        return new Document("appointment_id", "78")
            .append("dentist_id",  "754")
            .append("patient_id",  "92")
            .append("start_time", "14:00")
            .append("end_time", "15:00");
    }

    // Delete instance from 'AvailableTimes' collection
    private static void deleteOne(MongoCollection<Document> collection, String clinicId, String dentistId, String startTime) {
        // Perform search query to find document to delete
        // Document test13;
        // collection.findOneAndDelete(test13);
        // searchQueryFunction(collection, null)
        // collection.findOneAndDelete()
    }

    // Delete instance from 'AvailableTimes' collection
    private static void dentistDeleteAppointment(MongoDatabase appointmentDatabase, String clinicId, String patientiD, String dentistId) {

        
        /*
        ArrayList<Document> foundDocuments = searchQueryFunction(appointmentDatabase.getCollection("Appointments"),
                new String[][] {
                        {"appointment_id", clinicId},
                        {"dentist_id", dentistId}
                }
        );

        System.out.println(foundDocuments);
        */

        MongoCollection<Document> collection = appointmentDatabase.getCollection("Appointments");
        Bson searchQuery = new Document("appointment_id", clinicId)
            .append("dentist_id", dentistId);
        collection.findOneAndDelete(searchQuery);

        /*
        if (foundDocuments.size() > 0){
            // collection.findOneAndDelete((Bson)foundDocuments);
            collection.findOneAndDelete(searchQuery);
        }
        if (patientiD.length() > 0){
            // notify notification service for patient and user
        } else {
            // maybe notify dentist client?
        }
        */
    }

    private static ArrayList<Document> searchQueryFunction(MongoCollection<Document> collection, String[][] queryConditions) {
        BasicDBObject searchQuery = new BasicDBObject();
        searchQuery.putAll(transformToMap(queryConditions));

        MongoCursor<Document> cursor = collection.find(searchQuery).iterator();
        ArrayList<Document> foundDocuments = new ArrayList<Document>();

        while (cursor.hasNext()) {
            foundDocuments.add(cursor.next());
        }

        return foundDocuments;
    }

    // Transform the 2D array to a hash map to match the allowed datatype parameters that 'BasicDBObject' supports
    private static HashMap<String, String> transformToMap(String[][] queryConditions) {
        HashMap<String, String> map = new HashMap<String, String>(queryConditions.length);

        for (String[] mapping : queryConditions)
        {
            map.put(mapping[0], mapping[1]);
        }

        return map;
    }
}