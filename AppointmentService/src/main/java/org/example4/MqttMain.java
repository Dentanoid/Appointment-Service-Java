package org.example4;

import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient;

import static com.hivemq.client.mqtt.MqttGlobalPublishFilter.ALL;
import static java.nio.charset.StandardCharsets.UTF_8;

import java.nio.CharBuffer;

public class MqttMain {
    public static void main(String[] args) throws Exception {

        final String host = "5c221c75b8354617b30aad7a2186be07.s1.eu.hivemq.cloud"; // "5c221c75b8354617b30aad7a2186be07.s1.eu.hivemq.cloud" (works locally)  |   "broker.hivemq.com" (will be implemented with group --> Access localhost broker statistics)
        final String username = "MyCluster1"; // "MyCluster1"
        final String password = "Cluster123"; // "Cluster123"

        // create an MQTT client
        final Mqtt5BlockingClient client = MqttClient.builder()
                .useMqttVersion5()
                .serverHost(host)
                .serverPort(8883)
                .sslWithDefaultConfig()
                .buildBlocking();

        // connect to HiveMQ Cloud with TLS and username/pw
        client.connectWith()
                .simpleAuth()
                .username(username)
                .password(UTF_8.encode(password))
                .applySimpleAuth()
                .send();

        System.out.println("Connected successfully");

        // subscribe to the topic "my/test/topic"
        client.subscribeWith()
                .topicFilter("my/test/topic")
                .send();

        // set a callback that is called when a message is received (using the async API style)
        client.toAsync().publishes(ALL, publish -> {
            System.out.println("Received message: " +
                publish.getTopic() + " -> " +
                UTF_8.decode(publish.getPayload().get()));
            
            CharBuffer myJson = UTF_8.decode(publish.getPayload().get());
            System.out.println(myJson);
            
            // disconnect the client after a message was received
            // client.disconnect();
        });

        // publish a message to the topic "my/test/topic"
        client.publishWith()
                .topic("my/test/topic2")
                .payload(UTF_8.encode("Publish from IDE!"))
                .send();
    }
}
