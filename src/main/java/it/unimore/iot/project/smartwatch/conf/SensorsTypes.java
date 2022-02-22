package it.unimore.iot.project.smartwatch.conf;

public class SensorsTypes {
    // type of sensors supported

    public static final String TEMPERATURE_SENSOR_TYPE = "temperature_sensor";
    public static final String TEMPERATURE_SENSOR_UNIT = "Cel";

    public static final String GLUCOSE_SENSOR_TYPE = "glucose_sensor";
    public static final String GLUCOSE_SENSOR_UNIT = "mg/l";

    public static final String HEART_RATE_SENSOR_TYPE = "heart_rate_sensor";
    public static final String HEART_RATE_SENSOR_UNIT = "beat/min";

    public static final String SATURATION_SENSOR_TYPE = "saturation_sensor";
    public static final String SATURATION_SENSOR_UNIT = "%";

    public static final int SENSOR_SAMPLE = 5; // 15
    public static final int SENSOR_FREQUENCY = 1000; // 2000 (millis)
}