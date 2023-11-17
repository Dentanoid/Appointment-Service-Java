package org.example4;

import java.util.Arrays;

public class MQTTHandler {
    public Client client;

    // Expected format of MQTT topic:
    // "{Client-Type}/{MQTT-Direction}/{Action}" --> Example: "dentist/publish/appointment"

    private final String topic1 = "dentist/publish/appointment"; // --> dentist_publish.json
    private final String topic2 = "dentist/cancel/appointment"; // --> dentist_cancel.json

    private final String topic3 = "patient/publish/appointment"; // --> patient_book.json
    private final String topic4 = "patient/cancel/appointment"; // --> patient_cancel.json

    public MQTTHandler() {
        instantiateClient(topic1); // Change topic here
    }

    // Returns corresponding client
    public String decodeTopic(String topic) {
        String[] topicSplit = topic.split("/");
        return topicSplit[0];
    }

    public void instantiateClient(String topic) {
        String clientType = decodeTopic(topic);

        if (clientType.equals("dentist")) {
            this.client = new DentistClient(topic);
        } else if (clientType.equals("patient")) {
            this.client = new PatientClient(topic);
        }
    }
}
