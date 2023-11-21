package org.example4.TopicManagement;

public class Patient implements Client {
    private String topic;

    public Patient(String topic) {
        this.topic = topic;
        executeRequestedOperation(topic);
    }

    @Override
    public void createAppointment() {

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
    }
    
    private String decodeRequestedOperation(String topic) {
        return topic;
    }
}
