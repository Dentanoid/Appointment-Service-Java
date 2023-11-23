package org.example4.TopicManagement;

public interface Client {
    public void createAppointment(String payload);
    public void deleteAppointment(String payload);
    public void executeRequestedOperation(String topic, String payload);
}
