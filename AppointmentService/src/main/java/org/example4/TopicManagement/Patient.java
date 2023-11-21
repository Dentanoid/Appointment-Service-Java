package org.example4.TopicManagement;

public class Patient implements Client {
    private String topic;

    public Patient(String topic, String payload) {
        this.topic = topic;
        executeRequestedOperation(topic, payload);
    }

    @Override
    public void createAppointment(String payload) {

    }

    @Override
    public void deleteAppointment() {

    }

    @Override
    public void executeRequestedOperation(String topic, String payload) {
        String operation = decodeRequestedOperation(topic);

        if (operation == "create") {
            createAppointment(payload);
        } else {
            deleteAppointment();
        }
    }
    
    private String decodeRequestedOperation(String topic) {
        return topic;
    }
}
