package it.unimore.iot.project.smartwatch.models;

import com.google.gson.Gson;
import it.unimore.iot.project.smartwatch.conf.MqttConfigurationParameters;
import it.unimore.iot.project.smartwatch.conf.SensorsTypes;
import it.unimore.iot.project.smartwatch.utils.SenMLPack;
import it.unimore.iot.project.smartwatch.utils.SenMLRecord;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class SmartWatch {

    //global variable
    public static boolean start; // variables to synchronize the communication

    // utils
    private final Gson gson;
    private final static Logger logger = LoggerFactory.getLogger(SmartWatch.class);

    private IMqttClient client;
    private MqttConnectOptions options;
    private String controlTopic;

    // attributes of the smartwatch
    private String deviceId;
    private String producer;
    private String softwareVersion;
    private final List<SensorDescriptor> sensors;

    // massages descriptors
    private InfoMessageDescriptor infoMessage;
    private TelemetryMessageDescriptor telemetryMessage;
    private SenMLPack telemetrySenMLPack;

    // current data measured
    private final List<SenMLPack> data;

    public SmartWatch() {

        // utils initialization
        this.sensors = new ArrayList<>();
        this.gson = new Gson();
        this.data = new ArrayList<>();

    }

    public SmartWatch(String deviceId, String producer, String softwareVersion) {
        this();
        this.deviceId = deviceId;
        this.producer = producer;
        this.softwareVersion = softwareVersion;
    }

    @Override
    public String toString() {
        return "Smartwatch info:\n" +
                "deviceId='" + deviceId + '\n' +
                "producer='" + producer + '\n' +
                "softwareVersion='" + softwareVersion + '\n';
    }

    // adding single sensor
    public void addSensor(SensorDescriptor sensor) {
        if (this.sensors != null && sensor != null){
            this.sensors.add(sensor);
        }
        else{
            logger.error("Error adding new sensor");
        }
    }

    // initialization as a MQTT client
    private void initMQTTClient() {

        try {

            // MQTT client attributes
            MqttClientPersistence persistence = new MemoryPersistence();
            this.client = new MqttClient(String.format("tcp://%s:%d", MqttConfigurationParameters.BROKER_ADDRESS,
                    MqttConfigurationParameters.BROKER_PORT), this.deviceId, persistence);

            this.options = new MqttConnectOptions();
            this.options.setUserName(MqttConfigurationParameters.MQTT_USERNAME);
            this.options.setPassword(MqttConfigurationParameters.MQTT_PASSWORD.toCharArray());
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
            initMQTTClient();
            this.client.connect(options);
        } catch (Exception e) {
            logger.error("Something went wrong connecting to the MQTT broker\n");
            e.printStackTrace();
        }
    }

    // publish device info
    public void publishDeviceInfo() {

        try {

            String infoTopic = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SMARTWATCH_TOPIC,
                    this.deviceId,
                    MqttConfigurationParameters.SMARTWATCH_INFO_TOPIC);

            if (this.client.isConnected()) {

                buildInfoMessage();
                String jsonPayload = this.gson.toJson(this.infoMessage);

                MqttMessage msg = new MqttMessage(jsonPayload.getBytes());
                msg.setQos(2);
                msg.setRetained(true);
                this.client.publish(infoTopic, msg);

                logger.debug("Publishing to Topic: {} Data: {}", infoTopic, jsonPayload);

            } else {
                logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
            }

        } catch (Exception e) {
            logger.error("Something went wrong publishing smartwatch information\n");
            e.printStackTrace();
        }
    }

    // build info message
    private void buildInfoMessage() {
        try {
            this.infoMessage = new InfoMessageDescriptor(this.deviceId, this.producer, this.softwareVersion);
            for(SensorDescriptor sensor : this.sensors) {
                this.infoMessage.addSensor(sensor.getType());
            }
        } catch (Exception e) {
            logger.error("Something went wrong building info message\n");
            e.printStackTrace();
        }
    }

    // measure data
    public void senseAndPublishResults(){

        try {

            for(SensorDescriptor sensor : this.sensors) {
                switch (sensor.getType()) {
                    case SensorsTypes.TEMPERATURE_SENSOR_TYPE, SensorsTypes.GLUCOSE_SENSOR_TYPE -> {
                        this.telemetrySenMLPack = new SenMLPack();
                        buildTelemetryMessage(sensor);
                        getJsonSenMLResponse();
                        //publishJsonTelemetryData();
                        publishSenMLTelemetryData();
                    }
                    case SensorsTypes.HEART_RATE_SENSOR_TYPE, SensorsTypes.SATURATION_SENSOR_TYPE -> {
                        this.telemetrySenMLPack = new SenMLPack();
                        for (int i = 0; i < SensorsTypes.SENSOR_SAMPLE; i++) {
                            buildTelemetryMessage(sensor);
                            getJsonSenMLResponse();
                            //publishJsonTelemetryData();
                            Thread.sleep(SensorsTypes.SENSOR_FREQUENCY);
                        }
                        publishSenMLTelemetryData();
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Something went measuring and/or publishing data\n");
            e.printStackTrace();
        }
    }


    // publish SenMl telemetry data
    private void publishSenMLTelemetryData() {

        try {

            String telemetryTopic = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SMARTWATCH_TOPIC,
                    this.deviceId,
                    MqttConfigurationParameters.SMARTWATCH_TELEMETRY_TOPIC);

            if (this.client.isConnected()) {

                String senMLPayload = this.gson.toJson(this.telemetrySenMLPack);
                this.data.add(this.telemetrySenMLPack);

                MqttMessage msg = new MqttMessage(senMLPayload.getBytes());
                msg.setQos(2);
                msg.setRetained(false);
                this.client.publish(telemetryTopic, msg);

                logger.debug("Publishing SenML data to Topic: {} Data: {}", telemetryTopic, senMLPayload);

            } else {
                logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
            }

        } catch (Exception e) {
            logger.error("Something went wrong publishing smartwatch telemetry\n");
            e.printStackTrace();
        }
    }


    // build telemetry message
    private void buildTelemetryMessage(SensorDescriptor sensor) {
        try {
            this.telemetryMessage = new TelemetryMessageDescriptor(this.deviceId);
            this.telemetryMessage.setType(sensor.getType());
            this.telemetryMessage.setTimestamp(sensor.getTimestamp());
            this.telemetryMessage.setUnit(sensor.getUnit());
            this.telemetryMessage.setValue(sensor.getValue());
        } catch (Exception e) {
            logger.error("Something went wrong building telemetry message\n");
            e.printStackTrace();
        }
    }

    // build SenML Pack
    private void getJsonSenMLResponse(){
        try {

            SenMLRecord senMLRecord = new SenMLRecord();

            if(telemetrySenMLPack.isEmpty()) {
                senMLRecord.setBn(this.telemetryMessage.getDeviceId());
                senMLRecord.setN(this.telemetryMessage.getType());
                senMLRecord.setBt(this.telemetryMessage.getTimestamp());
                senMLRecord.setBu(this.telemetryMessage.getUnit());
            } else {
                senMLRecord.setN(this.telemetryMessage.getType());
                senMLRecord.setT(this.telemetryMessage.getTimestamp());
            }
            senMLRecord.setV(this.telemetryMessage.getValue());

            this.telemetrySenMLPack.add(senMLRecord);

        } catch (Exception e) {
            logger.error("Something went wrong building SenML telemetry message\n");
            e.printStackTrace();
        }
    }

    // subscribing to command topic to wait for the trigger signal
    public void waitToStart() {

        try {

            this.controlTopic = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SMARTWATCH_TOPIC,
                    this.deviceId,
                    MqttConfigurationParameters.SMARTWATCH_CONTROL_TOPIC);

            if (this.client.isConnected()) {

                this.client.subscribe(this.controlTopic, (topic, mqttMessage) -> {

                    //mqttMessage.setQos(2);
                    byte[] payload = mqttMessage.getPayload();
                    start = gson.fromJson(new String(payload), Boolean.class);
                    //logger.debug("Message Received("+topic+") Message Received: " + start);

                });

                // Block the process until the command START is received and the variable this.start becomes consistent
                boolean startFlag = false;
                while (!startFlag) { // run until not started
                    Thread.sleep(1000); // it allows data synchronization between listener's thread and main thread
                    startFlag = start;
                }

                start = false;

                // allowed to unsubscribe from control topic
                this.client.unsubscribe(this.controlTopic);

            } else {
                logger.error("Error: MQTT client is not connected!");
            }

        } catch (Exception e) {
            logger.error("Something went wrong subscribing to smartwatch information\n");
            e.printStackTrace();
        }
    }

    // notify the finish of the measurement publishing on control topic
    public void notifyFinish() {
        try {

            if (this.client.isConnected()) {

                String jsonPayload = this.gson.toJson(true);

                MqttMessage msg = new MqttMessage(jsonPayload.getBytes());
                msg.setQos(2);
                msg.setRetained(false);
                this.client.publish(this.controlTopic, msg);

            } else {
                logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected !");
            }

        } catch (Exception e) {
        logger.error("Something went wrong publishing FINISH command!\n");
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

    // get data of the current measurement
    public String getTelemetrySenMLPack() {
        StringBuilder stringToPrint = new StringBuilder();
        stringToPrint.append("------------------------------------------------------------------- Current measurement\n");
        for(SenMLPack pack : data) {
            for(SenMLRecord record : pack) {
                if (pack.indexOf(record) == 0) {
                    stringToPrint.append(record.getN()).append(" ")
                            .append(record.getBu()).append("\n")
                            .append(record.getV()).append(", ");
                } else {
                    stringToPrint.append(record.getV()).append(", ");
                }
            }
            stringToPrint.append("\n\n");
        }
        this.data.clear();
        return stringToPrint.toString();
    }
}
