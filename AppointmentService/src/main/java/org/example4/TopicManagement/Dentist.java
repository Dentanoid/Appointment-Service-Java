package org.example4.TopicManagement;

import org.bson.Document;
import org.example4.AppointmentService;
import org.example4.DatabaseManager;
import org.example4.Schemas.AvailableTimes;

public class Dentist implements Client {
    private String topic;
    private Document payloadDoc;

    public Dentist(String topic, String payload) {
        this.topic = topic;
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

        // REFACTORING TODO: Find a way to refactor so that the pub-topic depends on the operations (if-statements above)
        // MqttManager.getMqttManager().publishMessage("pub/dentist/availabletime/create", payloadDoc.toJson());
        // AppointmentService.mqttManager.publishMessage("pub/dentist/availabletime/create", payloadDoc.toJson());
        // mqttManager.publishMessage("pub/dentist/availabletime/create", payloadDoc.toJson());
        AppointmentService.mqttManager1.publishMessage("pub/dentist/availabletime/create", payloadDoc.toJson());
    }

    // NOTE: Elaborate on this method once group has decided on structure of topic
    private String decodeRequestedOperation(String topic) {
        String[] splitString = topic.split("/"); // "sub/dentist/availabletime/create" 
        return splitString[splitString.length - 1];
    }
}
