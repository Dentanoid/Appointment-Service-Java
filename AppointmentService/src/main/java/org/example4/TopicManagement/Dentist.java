package org.example4.TopicManagement;

import org.bson.Document;
import org.example4.DatabaseManager;
import org.example4.Schemas.AvailableTimes;

public class Dentist implements Client {
    private String topic;

    public Dentist(String topic) {
        this.topic = topic;
        executeRequestedOperation(topic);
    }

    @Override
    public void createAppointment() {
        // Document availableTimesDocument = DatabaseManager.convertPayloadToDocument(payload, new AvailableTimes());        
        // DatabaseManager.saveDocumentInCollection(DatabaseManager.availableTimesCollection, availableTimesDocument);        
    }

    @Override
    public void deleteAppointment() {

    }

    @Override
    public void executeRequestedOperation(String topic) {
        String operation = decodeRequestedOperation(topic);

        if (operation == "create") {
            createAppointment();
        } else {
            deleteAppointment();
        }

        // mqttMain.publishMessage(topic, availableTimesDocument.toJson());
    }

    // NOTE: Elaborate on this method once group has decided on structure of topic
    private String decodeRequestedOperation(String topic) {
        return topic;
    }
}
