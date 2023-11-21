package org.example4;

import java.util.ArrayList;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttManager {
    private static MqttManager mqttManager;
    private static String broker = "tcp://broker.hivemq.com:1883";

    int qos = 0;

    public static void initializeSubscriptions() {
        mqttManager = new MqttManager();
        mqttManager.subscribe("sub/dentist/availabletime/create");    
        // mqttManagr.subscribe("sub/availabletime/create"); // Add a new subscription
    }

    public static MqttManager getMqttManager() {
        return mqttManager;
    }

    public void publishMessage(String topic, String content) {
    // String clientId     = "JavaSampleClientId";
    String clientId = MqttClient.generateClientId();
    MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            System.out.println("Connecting to broker: "+broker);
            sampleClient.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            sampleClient.publish(topic, message);
            System.out.println("Message published");

            // sampleClient.disconnect();
            // System.out.println("Disconnected");
            // System.exit(0);
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        String username = "Test";
        String password = "Test123";
        // String clientid = "subscribe_client";
        String clientid = MqttClient.generateClientId();

        try {
           MqttClient client = new MqttClient(broker, clientid, new MemoryPersistence());
           // connect options
           MqttConnectOptions options = new MqttConnectOptions();
           options.setUserName(username);
           options.setPassword(password.toCharArray());
           options.setConnectionTimeout(60);
            options.setKeepAliveInterval(60);
           // setup callback
           client.setCallback(new MqttCallback() {

               public void connectionLost(Throwable cause) {
                   System.out.println("connectionLost: " + cause.getMessage());
              }

               public void messageArrived(String topic, MqttMessage message) throws MqttException {
                System.out.println("topic: " + topic);
                System.out.println("Qos: " + message.getQos());
                System.out.println("message content: " + new String(message.getPayload()));

                AppointmentService.manageRecievedPayload(topic, new String(message.getPayload()));
              }

               public void deliveryComplete(IMqttDeliveryToken token) {
                   System.out.println("deliveryComplete---------" + token.isComplete());
              }

          });
           client.connect(options);
           client.subscribe(topic, qos);           
      } catch (Exception e) {
           e.printStackTrace();
      }
    }
}
