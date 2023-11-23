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

public class Dentist implements Client {
    private Document payloadDoc = null;

    public Dentist(String topic, String payload) {
        executeRequestedOperation(topic, payload);
    }

    // Dentist creates a timeslot in which patients can book appointments
    @Override
    public void createAppointment(String payload) {
        String objectId =  DatabaseManager.getObjectId(payload, new AvailableTimes(), DatabaseManager.availableTimesCollection);

        // If the payload is unique, publish a time slot
        if (objectId == "-1") {
            // TODO: Check if dentist's timeslots overlaps from payload

            payloadDoc = DatabaseManager.convertPayloadToDocument(payload, new AvailableTimes());
            DatabaseManager.availableTimesCollection.insertOne(payloadDoc);
        }
    }

     // This method recieves a payload that contains the objectId to delete and the other appointment attributes to be sent in the notification
    @Override
    public void deleteAppointment(String payload) {
        String appointmentObjectId =  DatabaseManager.getObjectId(payload, new Appointments(), DatabaseManager.appointmentsCollection);
        String availableTimeObjectId =  DatabaseManager.getObjectId(payload, new AvailableTimes(), DatabaseManager.availableTimesCollection);


        // The dentist has an appointment to cancel
        if (appointmentObjectId != "-1") {
            payloadDoc = DatabaseManager.findDocumentById(appointmentObjectId, DatabaseManager.appointmentsCollection);
            DatabaseManager.appointmentsCollection.findOneAndDelete(payloadDoc);                        
        }

        // The dentist has an available time to cancel
        if (availableTimeObjectId != "-1") {
            payloadDoc = DatabaseManager.findDocumentById(availableTimeObjectId, DatabaseManager.availableTimesCollection);
            DatabaseManager.availableTimesCollection.findOneAndDelete(payloadDoc);
        }

        if (payloadDoc != null) {
            System.out.println("Appointment deleted successfully.");
        }

        /*
        try {
            String appointmentId = DatabaseManager.getAttributeValue(payload, "appointment_id", new Appointments());
            Bson searchQuery = new Document("appointment_id", appointmentId);

            DatabaseManager.appointmentsCollection.findOneAndDelete(searchQuery);
            DatabaseManager.availableTimesCollection.findOneAndDelete(searchQuery);

            System.out.println("Appointment deleted successfully.");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
        */
    }

    @Override
    public void executeRequestedOperation(String topic, String payload) {
        String operation = Utils.getSubstringAtIndex(topic, 0, true);
        String operationIdentifier = "create";

        if (operation.equals("create")) {
            createAppointment(payload);
        } else {
            deleteAppointment(payload);            
            operationIdentifier = "delete";
        }

        String publishTopic = "pub/dentist/notify/" + operationIdentifier;

        if (payloadDoc != null) {
            MqttMain.subscriptionManagers.get(topic).publishMessage(publishTopic, payloadDoc.toJson());
        } else {
            System.out.println("Status 404 - Did not find a timeslot");
        }
    }
}
