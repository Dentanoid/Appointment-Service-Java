package org.example4.TopicManagement;

public class TopicManager {
    public Client client;

    public Client getClient(String topic, String payload) {
        if (topic.contains("dentist")) {
            return new Dentist(topic, payload);
        }
        return new Patient(topic, payload);
    }

    public void manageTopic(String topic, String payload) {
        client = getClient(topic, payload);
    }
}
