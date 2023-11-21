package org.example4.TopicManagement;

public interface Client {
    public void createAppointment();
    public void deleteAppointment();
    public void executeRequestedOperation(String operation);
}
