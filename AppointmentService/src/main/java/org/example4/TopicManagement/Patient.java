package org.example4.TopicManagement;

import org.bson.Document;
import org.example4.AppointmentService;
import org.example4.DatabaseManager;
import org.example4.MqttMain;
import org.example4.Utils;
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
        
        // TODO:
        // 1) Do a search query in 'AvailableTimes' collection to find the DB-instance with the corresponding dentist_id
        // 2) Delete the instance that was found
    }

    @Override
    public void deleteAppointment(String payload) {

    }

    @Override
    public void executeRequestedOperation(String topic, String payload) {
        String operation = Utils.getSubstringAtIndex(topic, 0, true);

        if (operation.equals("create")) {
            createAppointment(payload);
        } else {
            deleteAppointment(payload);
        }

        MqttMain.subscriptionManagers.get(topic).publishMessage("pub/patient/notify", payloadDoc.toJson());
    }
}
