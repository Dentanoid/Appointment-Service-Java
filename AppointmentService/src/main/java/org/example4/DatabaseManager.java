package org.example4;

import java.util.Iterator;

import org.bson.Document;
import org.example4.Schemas.Appointments;
import org.example4.Schemas.CollectionSchema;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DatabaseManager {
    public static MongoClient client;
    public static MongoDatabase appointmentDatabase;    
    public static MongoCollection<Document> availableTimesCollection;
    public static MongoCollection<Document> appointmentsCollection;

    public static void initializeDatabaseConnection() {
        client = MongoClients.create("mongodb+srv://DentistUser:1234@dentistsystemdb.7rnyky8.mongodb.net/?retryWrites=true&w=majority");
        appointmentDatabase = client.getDatabase("AppointmentService");
        availableTimesCollection = appointmentDatabase.getCollection("AvailableTimes");
        appointmentsCollection = appointmentDatabase.getCollection("Appointments");
    }

    // Convert the payload-string to a document that can be stored in the database
    public static Document convertPayloadToDocument(String payload, CollectionSchema classSchema) {
        Gson gson = new Gson();
        CollectionSchema schemaClass = gson.fromJson(payload, classSchema.getClass());
        return schemaClass.getDocument();
    }

    public static String getAttributeValue(String payload, String attributeName, CollectionSchema classSchema) {
        Gson gson = new Gson();
        CollectionSchema schemaObject = gson.fromJson(payload, classSchema.getClass());
        return schemaObject.getDocument().get(attributeName).toString();
    }

    // POST - Create new instance in a collection
    public static void saveDocumentInCollection(MongoCollection<Document> collection, Document doc) {
        collection.insertOne(doc);
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

    // A temporary method for the developers to delete everything to save time in development process
    public static void deleteAllCollectionInstances() {
        availableTimesCollection.deleteMany(new Document());
        appointmentsCollection.deleteMany(new Document());
    }
}
