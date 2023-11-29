package org.example4.TopicManagement;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example4.AppointmentService;
import org.example4.MqttMain;
import org.example4.Utils;
import org.example4.DatabaseManagement.DatabaseManager;
import org.example4.DatabaseManagement.PayloadParser;
import org.example4.DatabaseManagement.Schemas.Appointments;
import org.example4.DatabaseManagement.Schemas.AvailableTimes;
import org.example4.DatabaseManagement.Schemas.CollectionSchema;

import com.fasterxml.jackson.databind.JsonNode;

public class Patient implements Client {
    private Document payloadDoc;

    public Patient(String topic, String payload) {
        executeRequestedOperation(topic, payload);
    }

    // Patient books existing timeslot
    @Override
    public void createAppointment(String payload) {
        String objectId =  PayloadParser.getObjectId(payload, new AvailableTimes(), DatabaseManager.availableTimesCollection);

        // If an dentist 'available time' exists at requested time to book
        if (objectId != "-1") {
            // Delete dentist's slot from 'AvailableTimes' collection
            Document dentistAvailableTimeDoc = PayloadParser.findDocumentById(objectId, DatabaseManager.availableTimesCollection);
            DatabaseManager.availableTimesCollection.findOneAndDelete(dentistAvailableTimeDoc);

            payloadDoc = PayloadParser.savePayloadDocument(payload, new Appointments(), DatabaseManager.appointmentsCollection);
        } else {

            // NOTE: The above if-statement is unnecessary if only available appointsments are displayed on FrontEnd
            // TEMPORARY:
            payloadDoc = PayloadParser.savePayloadDocument(payload, new Appointments(), DatabaseManager.appointmentsCollection);
        }
    }

    // Delete instance from 'Appointments' and add it as 'AvailableTime'
    @Override
    public void deleteAppointment(String payload) {
        String objectId =  PayloadParser.getObjectId(payload, new Appointments(), DatabaseManager.appointmentsCollection);

        // If payload-request is consistent with the actual value in database
        if (objectId != "-1") {
            payloadDoc = PayloadParser.findDocumentById(objectId, DatabaseManager.appointmentsCollection);
            payloadDoc.remove("patient_id");

            // TODO: Create method in 'DatabaseManager.java' migrateData(collectionA, collectionB)
            DatabaseManager.availableTimesCollection.insertOne(payloadDoc);
            DatabaseManager.appointmentsCollection.findOneAndDelete(payloadDoc);
        } else {
            System.out.println("Error: requested item does not exist in DB");
        }        
    }

    @Override
    public void executeRequestedOperation(String topic, String payload) {
        String operation = Utils.getSubstringAtIndex(topic, 0, true);
        String publishTopic = "";

        if (operation.equals("create")) {
            createAppointment(payload);
            publishTopic = "pub/patient/appointments/create";
        } else {
            deleteAppointment(payload);
            publishTopic = "pub/patient/appointments/delete";
        }

        if (payloadDoc != null) {
            MqttMain.subscriptionManagers.get(topic).publishMessage(publishTopic, payloadDoc.toJson());
        } else {
            System.out.println("Status 404 - No dentist on the available time was found");
        }
    }
}
