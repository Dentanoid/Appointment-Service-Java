package org.example4.TopicManagement;

import org.bson.Document;
import org.example4.DatabaseManager;
import org.example4.MqttManager;
import org.example4.Schemas.AvailableTimes;

public class Dentist implements Client {
    private String topic;
    private Document payloadDoc;

    public Dentist(String topic, String payload) {
        this.topic = topic;
        executeRequestedOperation(topic, payload);
    }

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

        MqttManager.getMqttManager().publishMessage("pub/dentist/availabletime/create", payloadDoc.toJson());
    }

    // NOTE: Elaborate on this method once group has decided on structure of topic
    private String decodeRequestedOperation(String topic) {
        String[] splitString = topic.split("/"); // "sub/dentist/availabletime/create" 
        return splitString[splitString.length - 1];
    }
}
