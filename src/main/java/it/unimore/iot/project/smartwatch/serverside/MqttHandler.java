package it.unimore.iot.project.smartwatch.serverside;

import com.google.gson.Gson;
import it.unimore.iot.project.smartwatch.utils.SenMLPack;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MqttHandler {

    // the use of global variables allows the visibility of them in both the main thread and the listeners' threads,
    // since the moment they will be initialized into the listeners.
    public static InfoMessageDescriptorSS infoPayload = new InfoMessageDescriptorSS();
    public static List<SenMLPack> telemetryPayload = new ArrayList<>();
    public static boolean finish; // variable to state the end of the subscribing

    // utils
    private final Gson gson;
    private final static Logger logger = LoggerFactory.getLogger(MqttHandler.class);

    // MQTT related variables
    private IMqttClient client;
    private MqttConnectOptions options;

    // Variables to map the mqtt's topics
    private final String controlTopic;
    private final String telemetryTopic;
    private final String infoTopic;

    public MqttHandler() {
        // utils initialization
        this.gson = new Gson();

        // topic initialization using the configuration files
        this.infoTopic = String.format("%s/%s/%s/%s",
                MqttConfigurationParametersSS.MQTT_BASIC_TOPIC,
                MqttConfigurationParametersSS.SMARTWATCH_TOPIC,
                MqttConfigurationParametersSS.SMARTWATCH_ID,
                MqttConfigurationParametersSS.SMARTWATCH_INFO_TOPIC);

        this.telemetryTopic = String.format("%s/%s/%s/%s",
                MqttConfigurationParametersSS.MQTT_BASIC_TOPIC,
                MqttConfigurationParametersSS.SMARTWATCH_TOPIC,
                MqttConfigurationParametersSS.SMARTWATCH_ID,
                MqttConfigurationParametersSS.SMARTWATCH_TELEMETRY_TOPIC);

        this.controlTopic = String.format("%s/%s/%s/%s",
                MqttConfigurationParametersSS.MQTT_BASIC_TOPIC,
                MqttConfigurationParametersSS.SMARTWATCH_TOPIC,
                MqttConfigurationParametersSS.SMARTWATCH_ID,
                MqttConfigurationParametersSS.SMARTWATCH_CONTROL_TOPIC);
    }

    // getter for the data retrieved from the broker.
    public InfoMessageDescriptorSS getInfoPayload() {
        return infoPayload;
    }

    public List<SenMLPack> getTelemetryPayload() {
        return telemetryPayload;
    }

    // initialization as a MQTT client
    private void initMQTTClient() {

        try {

            String clientId = UUID.randomUUID().toString(); // random string to identify the connection id
            // MQTT client attributes
            MqttClientPersistence persistence = new MemoryPersistence();
            this.client = new MqttClient(String.format("tcp://%s:%d", MqttConfigurationParametersSS.BROKER_ADDRESS,
                    MqttConfigurationParametersSS.BROKER_PORT), clientId, persistence);

            // setting up the options
            this.options = new MqttConnectOptions();
            this.options.setUserName(MqttConfigurationParametersSS.MQTT_USERNAME);
            this.options.setPassword((MqttConfigurationParametersSS.MQTT_PASSWORD).toCharArray());
            this.options.setAutomaticReconnect(true);
            this.options.setCleanSession(true);
            this.options.setConnectionTimeout(10);

        } catch (Exception e) {
            logger.error("Something went wrong initializing the MQTT client\n");
            e.printStackTrace();
        }
    }

    // connection to Broker
    public void connectClient() {
        try {
            initMQTTClient(); // call to the stand above method
            this.client.connect(options);
        } catch (Exception e) {
            logger.error("Something went wrong connecting to the MQTT broker\n");
            e.printStackTrace();
        }
    }

    // subscribing to the info topic for receiving the smartwatch related data
    public void subscribeInfo() {

        try {

            if (this.client.isConnected()) {

                // the subscribe method uses a listener dispatched to another parallel thread
                this.client.subscribe(this.infoTopic, (topic, mqttMessage) -> {

                    byte[] payload = mqttMessage.getPayload();
                    infoPayload = gson.fromJson(new String(payload), InfoMessageDescriptorSS.class); // saving data on the global variable
                    //logger.info("Message Received("+topic+") Message Received: " + infoPayload);

                });

            } else {
                logger.error("Error: MQTT client is not connected!");
            }

        } catch (Exception e) {
            logger.error("Something went wrong subscribing to smartwatch information\n");
            e.printStackTrace();
        }

    }

    // subscription to telemetry topic
    public void subscribeTelemetry() {
        try {

            if (this.client.isConnected()) {

                // the subscribe method uses a listener dispatched to another parallel thread
                this.client.subscribe(this.telemetryTopic, (topic, mqttMessage) -> {
                    byte[] payload = mqttMessage.getPayload();
                    telemetryPayload.add(gson.fromJson(new String(payload), SenMLPack.class)); // saving data on the global variable
                    //logger.info("Message Received("+topic+") Message Received: " + new String(payload));

                });

            } else {
                logger.error("Error: MQTT client is not connected!");
            }

        } catch (Exception e) {
            logger.error("Something went wrong subscribing to smartwatch telemetry\n");
            e.printStackTrace();
        }

    }

    // send command START
    public void sendCommandStart() {

        try {

            if (this.client.isConnected()) {

                // variables to synchronize the communication
                String jsonPayload = this.gson.toJson(true);

                MqttMessage msg = new MqttMessage(jsonPayload.getBytes());
                msg.setQos(2);
                msg.setRetained(false); // need to be NOT retained
                this.client.publish(this.controlTopic, msg); // method publish() to publish the message to the given topic
                //logger.debug("Publishing to Topic: {} Data: {}", this.controlTopic, jsonPayload);

            } else {
                logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
            }

        } catch (Exception e) {
            logger.error("Something went wrong publishing START command!\n");
            e.printStackTrace();
        }
    }

    // wait for the end of the sensing session
    public void waitToFinish() {

        try {

            if (this.client.isConnected()) {

                // the subscribe method uses a listener dispatched to another parallel thread
                this.client.subscribe(this.controlTopic, (topic, mqttMessage) -> {

                    byte[] payload = mqttMessage.getPayload();
                    finish = gson.fromJson(new String(payload), Boolean.class);
                    //logger.info("Message Received("+topic+") Message Received: " + this.finish);

                });

                // block the main thread until the command FINISH is received and the global variable finish becomes consistent
                boolean finishFlag = false;
                while (!finishFlag) { // run until not finished
                    Thread.sleep(1000); // it allows data synchronization between listener's thread and main thread
                    finishFlag = finish;
                }

                finish = false; // for precaution we set the global variable to false in order to avoid a non-block on the next communications

            } else {
                logger.error("Error: MQTT client is not connected!");
            }

        } catch (Exception e) {
            logger.error("Something went wrong subscribing to finish command\n");
            e.printStackTrace();
        }

    }

    // disconnect client MQTT
    public void disconnectClient() {
        try {
            this.client.disconnect();
            this.client.close();
        } catch (Exception e) {
            logger.error("Disconnection failed!\n");
            e.printStackTrace();
        }
    }

}
