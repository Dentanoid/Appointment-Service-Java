package org.example4.TopicManagement;

public class TopicManager implements DataDomain {
    private String topic;

    public TopicManager(String topic) {
        this.topic = topic;

        Client client = getClient(topic);
    }

    public Client getClient(String test) {
        if (test == "T") {
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
