package it.unimore.iot.project.smartwatch.serverside;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MqttHandlerProcess {

    private final static Logger logger = LoggerFactory.getLogger(MqttHandlerProcess.class);

    public static void main(String[] args) {

        logger.info("MQTT Handler process started ...\n");

        try {

            // 1. instance a new mqtt handler
            MqttHandler mqttHandler = new MqttHandler();
            logger.info("Mqtt Handler initialized successfully!");

            // 2. connect the client
            mqttHandler.connectClient();
            logger.info("Connected!\n");

            // 3. send command start to smartwatch
            mqttHandler.sendCommandStart();
            logger.info("Session started!\n");

            // 4. subscribe to info topic
            logger.info("Receiving data ...\n");


            // subscription to info and telemetry topic
            mqttHandler.subscribeInfo();
            mqttHandler.subscribeTelemetry();

            // give it time to receive all data
            mqttHandler.waitToFinish();

            // client disconnection
            mqttHandler.disconnectClient();
            logger.info("Data received successfully and client disconnected!\n");

            // finally, we have the data!
            logger.info("Info data: " + mqttHandler.getInfoPayload());
            logger.info("Telemetry data: " + mqttHandler.getTelemetryPayload());
            // Note that now we can relay them to other functions for our applications purposes

        } catch (Exception e) {
            logger.error("Something went wrong during the process\n");
            e.printStackTrace();
        }


    }
}
