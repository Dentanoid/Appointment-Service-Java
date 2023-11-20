package org.example4;

public class PatientClient extends Client {
    // private String topic;
    public PatientClient(String topic) {
        super(topic);
    }

    // bookAppointment()
    public void publishAppointment() {
        System.out.println("Patient: publishAvailableTimeSlots()");
    }

    // cancelAppointment()
    public void cancelAppointment() {
        System.out.println("Patient: cancelTimeSlot()");
    }
}
