package org.example4.TopicManagement;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example4.AppointmentService;
import org.example4.MqttMain;
import org.example4.Utils;
import org.example4.DatabaseManagement.DatabaseManager;
import org.example4.DatabaseManagement.PayloadParser;
import org.example4.DatabaseManagement.Schemas.Appointments;
import org.example4.DatabaseManagement.Schemas.AvailableTimes;

public class Dentist implements Client {
    private Document payloadDoc = null;

    public Dentist(String topic, String payload) {
        executeRequestedOperation(topic, payload);
    }

    // Dentist creates a timeslot in which patients can book appointments
    @Override
    public void createAppointment(String payload) {
        String objectId =  PayloadParser.getObjectId(payload, new AvailableTimes(), DatabaseManager.availableTimesCollection);

        // If the payload is unique, publish a time slot
        if (objectId == "-1") {
            // TODO: Check if dentist's timeslots overlaps from payload
            // PROBLEM: A dentist can have multiple appointments/available-times a day, but their intervals cannot overlap

            System.out.println("************* PAYLOAD: ****************");
            System.out.println(payload);
            System.out.println("*************************************");

            payloadDoc = PayloadParser.savePayloadDocument(payload, new AvailableTimes(), DatabaseManager.availableTimesCollection);  
        }
    }

     // This method recieves a payload that contains the objectId to delete and the other appointment attributes to be sent in the notification
    @Override
    public void deleteAppointment(String payload) {
        String appointmentObjectId =  PayloadParser.getObjectId(payload, new AvailableTimes(), DatabaseManager.appointmentsCollection); // Previous: new Appointments()
        String availableTimeObjectId =  PayloadParser.getObjectId(payload, new AvailableTimes(), DatabaseManager.availableTimesCollection);

        // The dentist has an appointment to cancel
        if (appointmentObjectId != "-1") {
            payloadDoc = PayloadParser.findDocumentById(appointmentObjectId, DatabaseManager.appointmentsCollection);
            DatabaseManager.appointmentsCollection.findOneAndDelete(payloadDoc);                        
        }

        // The dentist has an available time to cancel
        if (availableTimeObjectId != "-1") {
            payloadDoc = PayloadParser.findDocumentById(availableTimeObjectId, DatabaseManager.availableTimesCollection);
            DatabaseManager.availableTimesCollection.findOneAndDelete(payloadDoc);
        }

        if (payloadDoc != null) {
            System.out.println("Appointment deleted successfully.");
        }
    }

    @Override
    public void executeRequestedOperation(String topic, String payload) {
        String operation = Utils.getSubstringAtIndex(topic, 0, true);
        String publishTopic = "";

        System.out.println("1111111111111111111111111");

        if (operation.equals("create")) {
            createAppointment(payload);
            publishTopic = "pub/dentist/availabletimes/create";
        } else {
            deleteAppointment(payload);            
            publishTopic = "pub/dentist/delete";
        }

        if (payloadDoc != null) {
            MqttMain.subscriptionManagers.get(topic).publishMessage(publishTopic, payloadDoc.toJson());
        } else {
            System.out.println("Status 404 - Did not find a timeslot");
        }
    }
}
