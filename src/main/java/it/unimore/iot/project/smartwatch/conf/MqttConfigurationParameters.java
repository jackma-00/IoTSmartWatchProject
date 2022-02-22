package it.unimore.iot.project.smartwatch.conf;

public class MqttConfigurationParameters {
    public static String BROKER_ADDRESS = "155.185.228.20";
    public static int BROKER_PORT = 7883;
    public static final String MQTT_USERNAME = "ineffective user"; // make sure to change username with a valid one
    public static final String MQTT_PASSWORD = " ineffective password"; // make sure to change password with a valid one
    public static final String MQTT_BASIC_TOPIC = String.format ("/iot/user/%s", MQTT_USERNAME);
    public static final String SMARTWATCH_TOPIC = "smartwatch";
    public static final String SMARTWATCH_TELEMETRY_TOPIC = "telemetry";
    public static final String SMARTWATCH_INFO_TOPIC = "info";
    public static final String SMARTWATCH_CONTROL_TOPIC = "control1";
}