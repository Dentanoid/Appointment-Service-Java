package org.example4.TopicManagement;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example4.AppointmentService;
import org.example4.DatabaseManager;
import org.example4.MqttMain;
import org.example4.Utils;
import org.example4.Schemas.Appointments;
import org.example4.Schemas.AvailableTimes;
import org.example4.Schemas.CollectionSchema;

import com.fasterxml.jackson.databind.JsonNode;

public class Patient implements Client {
    private Document payloadDoc;

    public Patient(String topic, String payload) {
        executeRequestedOperation(topic, payload);
    }

    // Patient books existing timeslot
    @Override
    public void createAppointment(String payload) {

        // TODO Check if can find matching ObjectId before storing in DB
        payloadDoc = DatabaseManager.convertPayloadToDocument(payload, new Appointments());
        DatabaseManager.saveDocumentInCollection(DatabaseManager.appointmentsCollection, payloadDoc);

        String dentistId = DatabaseManager.getAttributeValue(payload, "dentist_id", new AvailableTimes());
        Bson searchQuery = new Document("dentist_id", dentistId);

        payloadDoc = DatabaseManager.availableTimesCollection.findOneAndDelete(searchQuery);
    }

    @Override
    public void deleteAppointment(String payload) {
        // String appointmentId = DatabaseManager.getAttributeValue(payload, "appointment_id", new Appointments()); // TODO: Create method getValue() in Client.java
        // Bson searchQuery = new Document("appointment_id", appointmentId); // TODO: Refactor into 'query' method and reuse across all methods in both client-classes

        String objectId =  DatabaseManager.getObjectId(payload, new Appointments(), DatabaseManager.appointmentsCollection);
        Document docTest = DatabaseManager.findDocumentById(objectId, DatabaseManager.appointmentsCollection);

        docTest.remove("patient_id");

        DatabaseManager.availableTimesCollection.insertOne(docTest);
        DatabaseManager.appointmentsCollection.findOneAndDelete(docTest);

        System.out.println("Document deleted, patient_id removed, and migrated successfully.");
    }

    @Override
    public void executeRequestedOperation(String topic, String payload) {
        String operation = Utils.getSubstringAtIndex(topic, 0, true);

        if (operation.equals("create")) {
            createAppointment(payload);
        } else {
            deleteAppointment(payload);
        }

        if (payloadDoc != null) {
            MqttMain.subscriptionManagers.get(topic).publishMessage("pub/patient/notify", payloadDoc.toJson());
        } else {
            System.out.println("Status 404 - No dentist on the available time was found");
        }
    }
}
