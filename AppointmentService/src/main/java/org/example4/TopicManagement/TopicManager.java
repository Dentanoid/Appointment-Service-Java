package org.example4.TopicManagement;

public class TopicManager implements DataDomain {
    public Client client;

    public TopicManager(String topic) {
        client = getClient(topic);
    }

    public Client getClient(String topic) {
        if (topic.contains("dentist")) {
            return new Dentist(topic);
        }
        return new Patient(topic);
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
