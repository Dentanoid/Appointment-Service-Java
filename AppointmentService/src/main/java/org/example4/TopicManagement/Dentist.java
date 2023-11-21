package org.example4.TopicManagement;

import org.bson.Document;
import org.example4.AppointmentService;
import org.example4.DatabaseManager;
import org.example4.MqttMain;
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
    public void deleteAppointment() {

    }

    @Override
    public void executeRequestedOperation(String topic, String payload) {
        String operation = decodeRequestedOperation(topic);

        if (operation.equals("create")) {
            createAppointment(payload);
        } else {
            deleteAppointment();
        }

        // AppointmentService.mqttManager1.publishMessage("pub/dentist/availabletime/create", payloadDoc.toJson());
        MqttMain.subscriptionManagers.get(topic).publishMessage("pub/test/topic/123", payloadDoc.toJson());
    }

    // NOTE: Elaborate on this method once group has decided on structure of topic
    private String decodeRequestedOperation(String topic) {
        String[] splitString = topic.split("/"); // "sub/dentist/availabletime/create" 
        return splitString[splitString.length - 1];
    }
}
