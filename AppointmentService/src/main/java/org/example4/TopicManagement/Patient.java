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
        String objectId =  DatabaseManager.getObjectId(payload, new AvailableTimes(), DatabaseManager.availableTimesCollection);

        // If an dentist 'available time' exists at requested time to book
        if (objectId != "-1") {
            // Delete dentist's slot from 'AvailableTimes' collection
            Document dentistAvailableTimeDoc = DatabaseManager.findDocumentById(objectId, DatabaseManager.availableTimesCollection);
            DatabaseManager.availableTimesCollection.findOneAndDelete(dentistAvailableTimeDoc);

            // Save payload-document in 'Appointment' collection
            payloadDoc = DatabaseManager.convertPayloadToDocument(payload, new Appointments());
            System.out.println("**************" + payloadDoc + "******************");
            DatabaseManager.appointmentsCollection.insertOne(payloadDoc);
        } else {
            System.out.println("No dentist has booked at that time!");
        }
    }

    // Delete instance from 'Appointments' and add it as 'AvailableTime'
    @Override
    public void deleteAppointment(String payload) {
        String objectId =  DatabaseManager.getObjectId(payload, new Appointments(), DatabaseManager.appointmentsCollection);

        // If payload-request is consistent with the actual value in database
        if (objectId != "-1") {
            payloadDoc = DatabaseManager.findDocumentById(objectId, DatabaseManager.appointmentsCollection);

            payloadDoc.remove("patient_id");

            DatabaseManager.availableTimesCollection.insertOne(payloadDoc);
            DatabaseManager.appointmentsCollection.findOneAndDelete(payloadDoc);
        } else {
            System.out.println("Document deleted, patient_id removed, and migrated successfully.");
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
            System.out.println("Status 404 - No dentist on the available time was found");
        }
    }
}
