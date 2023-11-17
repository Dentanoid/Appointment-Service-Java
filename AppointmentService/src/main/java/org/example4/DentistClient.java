package org.example4;

public class DentistClient extends Client {
    public DentistClient(String topic) {
        super(topic);
    }

    // publishAvailableTimeSlots()
    public void publishAppointment() {
        System.out.println("Dentist: publishAvailableTimeSlots()");
        
        String specificFilePath = "AppointmentService\\src\\main\\resources\\Publish\\dentist_publish.json"; // Works in Visual Studio
        // String specificFilePath2 = "src/main/resources/Publish/dentist_publish.json"; // Works in IntelliJ

        Object dentistClientRequest = Utils.deserialize(specificFilePath); // TODO: Store this object in database
        System.out.println(dentistClientRequest);
    }

    // cancelTimeSlot()
    public void cancelAppointment() {
        System.out.println("Dentist: cancelTimeSlot()");
    }
}
