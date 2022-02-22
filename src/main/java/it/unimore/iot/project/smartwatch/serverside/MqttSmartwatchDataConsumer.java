package it.unimore.iot.project.smartwatch.serverside;

import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

public class MqttSmartwatchDataConsumer {

    public static void main(String[] args) {
        try {

            String clientId = UUID.randomUUID().toString();

            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(String.format("tcp://%s:%d", MqttConfigurationParametersSS.BROKER_ADDRESS,
                    MqttConfigurationParametersSS.BROKER_PORT),
                    clientId,
                    persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MqttConfigurationParametersSS.MQTT_USERNAME);
            options.setPassword((MqttConfigurationParametersSS.MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            mqttClient.connect(options);

            String infoTopic = String.format("%s/%s/test1/%s",
                    MqttConfigurationParametersSS.MQTT_BASIC_TOPIC,
                    MqttConfigurationParametersSS.SMARTWATCH_TOPIC,
                    MqttConfigurationParametersSS.SMARTWATCH_CONTROL_TOPIC);

            mqttClient.subscribe(infoTopic, (topic, mqttMessage) -> {
                byte[] payload = mqttMessage.getPayload();
                System.out.println("Message Received("+topic+") Message Received: " + new String(payload));
            });
        } catch (Exception e) {

            e.printStackTrace();

        }
    }

}
