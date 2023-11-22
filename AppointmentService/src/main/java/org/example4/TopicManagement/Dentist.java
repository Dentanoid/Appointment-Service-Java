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
    private Document payloadDoc;

    public Dentist(String topic, String payload) {
        executeRequestedOperation(topic, payload);
    }

    // Dentist creates a timeslot in which patients can book appointments
    @Override
    public void createAppointment(String payload) {
        payloadDoc = DatabaseManager.convertPayloadToDocument(payload, new AvailableTimes());
        DatabaseManager.saveDocumentInCollection(DatabaseManager.availableTimesCollection, payloadDoc);
    }

    @Override
    public void deleteAppointment(String payload) {
        // This method recieves a payload that contains the objectId to delete and the other appointment attributes to be sent in the notification

        try {
            String appointmentId = DatabaseManager.getAttributeValue(payload, "appointment_id", new Appointments());
            Bson searchQuery = new Document("appointment_id", appointmentId);

            DatabaseManager.appointmentsCollection.findOneAndDelete(searchQuery);
            DatabaseManager.availableTimesCollection.findOneAndDelete(searchQuery);

            System.out.println("Appointment deleted successfully.");
        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
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
            System.out.println("Status 404 - Did not find a timeslot to delete");
        }
    }
}
