package org.example4.TopicManagement;

public class Patient implements Client {

    @Override
    public void createAppointment() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'createAppointment'");
    }

    @Override
    public void deleteAppointment() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'deleteAppointment'");
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
