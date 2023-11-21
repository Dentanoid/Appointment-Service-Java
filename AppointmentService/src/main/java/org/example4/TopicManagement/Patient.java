package org.example4.TopicManagement;

import org.bson.Document;
import org.example4.AppointmentService;
import org.example4.DatabaseManager;
import org.example4.MqttMain;
import org.example4.Schemas.Appointments;

public class Patient implements Client {
    private Document payloadDoc;

    public Patient(String topic, String payload) {
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

        // Todo: Create smooth extension code for publish-messages
        MqttMain.subscriptionManagers.get(topic).publishMessage("pub/test/topic/123", payloadDoc.toJson());
    }
    
    private String decodeRequestedOperation(String topic) {
        String[] splitString = topic.split("/"); // "sub/patient/appointments/create"
        return splitString[splitString.length - 1];
    }
}
