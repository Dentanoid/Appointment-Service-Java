package org.example4;

public abstract class Client {
    public Client(String topic) {
        identifyPublishAction(topic);
    }

    abstract void publishAppointment();
    abstract void cancelAppointment();

    public void identifyPublishAction(String topic) { // TODO: Refactor according to Open-Closed principle
        if (topic.contains("publish")) { // Not for developers: Discuss topic format so we can decode it accordingly here
            publishAppointment();
        } else if (topic.contains("cancel")) {
            cancelAppointment();
        }
    }
}
