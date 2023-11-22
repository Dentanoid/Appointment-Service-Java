package org.example4.TopicManagement;

public class TopicManager {
    public Client client;

    public TopicManager(String topic, String payload) {
        client = getClient(topic, payload);
    }

    public Client getClient(String topic, String payload) {
        if (topic.contains("dentist")) {
            return new Dentist(topic, payload);
        }
        return new Patient(topic, payload);
    } 
}
