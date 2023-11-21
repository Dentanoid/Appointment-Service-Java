package org.example4.TopicManagement;

public class TopicManager implements DataDomain {
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

    @Override
    public String getCollection() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCollection'");
    }

    @Override
    public String getOperation() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getOperation'");
    }
    
}
