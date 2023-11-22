package org.example4.TopicManagement;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example4.AppointmentService;
import org.example4.DatabaseManager;
import org.example4.MqttMain;
import org.example4.Utils;
import org.example4.Schemas.Appointments;

import com.fasterxml.jackson.databind.JsonNode;

public class Patient implements Client {
    private Document payloadDoc;

    public Patient(String topic, String payload) {
        executeRequestedOperation(topic, payload);
    }

    // Patient books existing timeslot
    @Override
    public void createAppointment(String payload) {
        payloadDoc = DatabaseManager.convertPayloadToDocument(payload, new Appointments());
        DatabaseManager.saveDocumentInCollection(DatabaseManager.appointmentsCollection, payloadDoc);
        
        // TODO:
        // 1) Do a search query in 'AvailableTimes' collection to find the DB-instance with the corresponding dentist_id
        // 2) Delete the instance that was found
    }

    @Override
    public void deleteAppointment(String payload) {
        String appointmentId = DatabaseManager.getAttributeValue(payload, "appointment_id", new Appointments());
        Bson searchQuery = new Document("appointment_id", appointmentId);

        try {
            Document foundDocument = DatabaseManager.appointmentsCollection.find(searchQuery).first();

            if (foundDocument != null) {
                payloadDoc = DatabaseManager.appointmentsCollection.findOneAndDelete(searchQuery);

                // Remove the additional attributes that distinguishes an 'Appointment' collection instance from 'AvailableTimes'
                payloadDoc.remove("patient_id");
                payloadDoc.remove("appointment_id");

                // Insert the modified document into the AvailableTimes collection
                DatabaseManager.availableTimesCollection.insertOne(payloadDoc);

                System.out.println("Document deleted, patient_id removed, and migrated successfully.");
            } else {
                System.out.println("Object with this objectId is not found");
            }
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
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
            System.out.println("Status 404 - Did not find an appointment to delete");
        }
    }
}
