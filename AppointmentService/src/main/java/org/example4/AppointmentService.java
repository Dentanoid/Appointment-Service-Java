package org.example4;

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
    private static void dentistCreateAvailableTime(MongoDatabase appointmentDatabase) {
        // TODO:
        // 1) Verify that the topic containts 'dentist'

        MongoCollection<Document> availableTimesCollection = appointmentDatabase.getCollection("AvailableTimes");
        availableTimesCollection.insertOne(makeAvailableTimeDocument());
    }

    // Patient registers on existing slot found in 'AvailableTimes' collection
    private static void patientCreateAppointment(MongoDatabase appointmentDatabase) {
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

    // Delete instance from 'AvailableTimes' collection
    private static void dentistDeleteAppointment(MongoDatabase appointmentDatabase, String clinicId, String patientiD, String dentistId, String startTime) {

        ArrayList<Document> foundDocuments = searchQueryFunction(appointmentDatabase.getCollection("AvailableTimes"),
                new String[][] {
                        {"clinic_id", clinicId},
                        {"dentist_id", dentistId}
                }
        );

        MongoCollection<Document> collection = appointmentDatabase.getCollection("Appointments");
        Bson searchQuery = new Document("appointment_id", clinicId)
                .append("dentist_id", dentistId);
        collection.findOneAndDelete(searchQuery);
    }

    private static void patientDeleteAppointment(MongoDatabase appointmentDatabase, String objectId) {
        // Access User Service - patient collection - Change appointment attribute to null
        // Migrate data from Appoinment to AvailableTimes
        // Notify dentist
        MongoCollection<Document> collection = appointmentDatabase.getCollection("Appointments");
        MongoCollection<Document> publishCollection = appointmentDatabase.getCollection("AvailableTimes");
        ObjectId appointmentId;

        try {
            appointmentId = new ObjectId(objectId);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid ObjectId format");
            return;
        }

        Bson searchQuery = new Document("_id", appointmentId);

        try {
            Document foundDocument = collection.find(searchQuery).first();

            if (foundDocument != null) {
                // Delete from the Appointments collection and get the document
                Document deletedDocument = collection.findOneAndDelete(searchQuery);
                mqttMain.publishMessage("grp20/notification/patient/cancel", deletedDocument.toJson());

                // Remove the "patient_id" field from the document
                deletedDocument.remove("patient_id");

                // Insert the modified document into the AvailableTimes collection
                publishCollection.insertOne(deletedDocument);

                System.out.println("Document deleted, patient_id removed, and migrated successfully.");
            } else {
                System.out.println("Object with this objectId is not found");
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    // IDEA: Refactor into MongoDBSchema.java:

    private static Document makeAvailableTimeDocument() {
        return new Document("clinic_id", "70")
                .append("dentist_id",  "40")
                .append("start_time", "10:30")
                .append("end_time", "11:30");
    }

    private static Document makeAppointmentsDocument() {
        return new Document("clinic_id", "123456")
                .append("dentist_id",  "75")
                .append("patient_id",  "91")
                .append("start_time", "14:00")
                .append("end_time", "15:00");
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