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
        mqttMain.subscribe("my/test/topic/appointment"); // TODO: Refactor into 'setSubscriptions()' in MqttMain.java
    }

    public static void myTestMethod(String topic, String payload) {
        if (topic.contains("availabletime")) {
            createAvailableTime(topic);
        } else if (topic.contains("appointment")) {
            createAppointment(topic);
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
    private static void dentistCreateAvailableTime(Document payload) { //Needs to be changed when implemented correctly
        // TODO:
        // 1) Verify that the topic containts 'dentist'

        Document availableTimesDocument = makeAvailableTimeDocument();
        availableTimesCollection.insertOne(availableTimesDocument);

        mqttMain.publishMessage("test/publish/topic", availableTimesDocument.toJson());
    }

    // Patient registers on existing slot found in 'AvailableTimes' collection
    private static void patientCreateAppointment(Document payload) {
        // TODO:
        // 1) Verify that the topic containts 'patient'
        // 2) Delete corresponding appointment-data-instance from 'AvailableTimes' collection
        // 3) Create appointment in 'Appointments' collection

        Document appointmentDocument = makeAppointmentsDocument();
        appointmentsCollection.insertOne(appointmentDocument);

        mqttMain.publishMessage("test/publish/topic", appointmentDocument.toJson());
    }

    // Delete instance from 'AvailableTimes' collection
    private static void dentistDeleteAppointment(MongoDatabase appointmentDatabase, String appointmentId) {
        try {
            MongoCollection<Document> collection = appointmentDatabase.getCollection("Appointments");
            ObjectId appointmenObjectId = new ObjectId(appointmentId);

            Bson searchQuery = new Document("_id", appointmenObjectId);
            Document document = collection.findOneAndDelete(searchQuery);

            MongoCollection<Document> availableCollection = appointmentDatabase.getCollection("AvailableTimes");
            availableCollection.findOneAndDelete(searchQuery);

            mqttMain.publishMessage("grp20/notification/dentist/cancel", document.toJson());
            System.out.println("Appointment deleted successfully.");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static void patientDeleteAppointment() {
        // Access User Service - patient collection - Change appointment attribute to null
        // Migrate data from Appoinment to AvailableTimes
        // Notify dentist
    }

    // IDEA: Refactor into MongoDBSchema.java:

    private static Document makeAvailableTimeDocument() {
        return new Document("clinic_id", "70")
                .append("dentist_id",  "40")
                .append("start_time", "10:30")
                .append("end_time", "11:30");
    }

    private static Document makeAppointmentsDocument() {
        return new Document("clinic_id", "78")
            .append("dentist_id",  "6768")
            .append("patient_id",  "92")
            .append("start_time", "14:00")
            .append("end_time", "15:00");
    }
}