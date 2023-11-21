package org.example4.TopicManagement;

import org.bson.Document;
import org.example4.AppointmentService;
import org.example4.DatabaseManager;
import org.example4.Schemas.Appointments;

public class Patient implements Client {
    private String topic;
    private Document payloadDoc;

    public Patient(String topic, String payload) {
        this.topic = topic;
        executeRequestedOperation(topic, payload);
    }

    // Patient books existing timeslot
    @Override
    public void createAppointment(String payload) {
        payloadDoc = DatabaseManager.convertPayloadToDocument(payload, new Appointments());
        DatabaseManager.saveDocumentInCollection(DatabaseManager.appointmentsCollection, payloadDoc);        
    }

    @Override
    public void deleteAppointment() {

    }

    @Override
    public void executeRequestedOperation(String topic, String payload) {
        String operation = decodeRequestedOperation(topic);

        if (operation.equals("create")) {
            createAppointment(payload);
        } else {
            deleteAppointment();
        }

        // MqttManager.getMqttManager().publishMessage("pub/patient/appointments/create", payloadDoc.toJson());
        // AppointmentService.mqttManager.publishMessage("pub/patient/appointments/create", payloadDoc.toJson());
        // mqttManager.publishMessage("pub/patient/appointments/create", payloadDoc.toJson());
        AppointmentService.mqttManager1.publishMessage("pub/patient/appointments/create", payloadDoc.toJson());
    }
    
    private String decodeRequestedOperation(String topic) {
        String[] splitString = topic.split("/"); // "sub/patient/appointments/create"
        return splitString[splitString.length - 1];
    }
}
