package org.example4;

import org.bson.Document;
import org.example4.Schemas.CollectionSchema;

import com.google.gson.Gson;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DatabaseManager { // TODO: Create singleton
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

    public static void saveDocumentInCollection(MongoCollection<Document> collection, Document doc) {
        collection.insertOne(doc);
    }
}
