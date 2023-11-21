package org.example4.TopicManagement;

public class TopicManager implements DataDomain {
    private String topic;
    public Client client;

    public TopicManager(String topic) {
        this.topic = topic;
        client = getClient(topic);
    }

    public Client getClient(String topic) {
        if (topic.contains("dentist")) {
            return new Dentist();
        }
        return new Patient();
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
