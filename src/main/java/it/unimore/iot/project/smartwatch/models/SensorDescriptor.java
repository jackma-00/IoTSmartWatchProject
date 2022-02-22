package it.unimore.iot.project.smartwatch.models;

import java.util.Random;
import it.unimore.iot.project.smartwatch.conf.SensorsTypes;

public class SensorDescriptor {

    private String type;
    private String unit;
    private double value;
    private long timestamp;
    private final Random rnd;
    private int illnessClass; // it identifies the class of illness

    public SensorDescriptor() {
        this.rnd = new Random(System.currentTimeMillis());
    }

    public SensorDescriptor(String type, String unit) {
        this();
        this.type = type;
        this.unit = unit;
    }

    public String getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public double getValue() {
        generateIllnessClass();
        generateValue();
        return value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "SensorDescriptor{" +
                "type='" + type + '\'' +
                ", unit='" + unit + '\'' +
                ", value=" + value +
                ", timestamp=" + timestamp +
                '}';
    }

    private void generateValue() {
        this.timestamp = System.currentTimeMillis();
        switch (this.type) {
            case SensorsTypes.TEMPERATURE_SENSOR_TYPE -> generateEngineTemperature();
            case SensorsTypes.HEART_RATE_SENSOR_TYPE -> generateEngineHeartRate();
            case SensorsTypes.GLUCOSE_SENSOR_TYPE -> generateEngineGlucose();
            case SensorsTypes.SATURATION_SENSOR_TYPE -> generateEngineSaturation();
        }
    }

    private void generateIllnessClass() {
        this.illnessClass = rnd.nextInt(3);
    }

    private void generateEngineTemperature() {
        this.unit = SensorsTypes.TEMPERATURE_SENSOR_UNIT;
        switch (this.illnessClass) {
            case 0 -> this.value = 35.5 + this.rnd.nextDouble() * 1.5;
            case 1 -> this.value = 37 + this.rnd.nextDouble() * 1;
            case 2 -> this.value = 38 + this.rnd.nextDouble() * 3;
        }
    }

    private void generateEngineHeartRate() {
        this.unit = SensorsTypes.HEART_RATE_SENSOR_UNIT;
        switch (this.illnessClass) {
            case 0 -> this.value = 50 + this.rnd.nextDouble() * 10;
            case 1 -> this.value = 60 + this.rnd.nextDouble() * 40;
            case 2 -> this.value = 100 + this.rnd.nextDouble() * 300;
        }
    }

    private void generateEngineGlucose() {
        this.unit = SensorsTypes.GLUCOSE_SENSOR_UNIT;
        switch (this.illnessClass) {
            case 0 -> this.value = 7 + this.rnd.nextDouble() * 3;
            case 1 -> this.value = 10 + this.rnd.nextDouble() * 2.5;
            case 2 -> this.value = 12.5 + this.rnd.nextDouble() * 5;
        }
    }

    private void generateEngineSaturation() {
        this.unit = SensorsTypes.SATURATION_SENSOR_UNIT;
        switch (this.illnessClass) {
            case 0 -> this.value = 70 + this.rnd.nextDouble() * 20;
            case 1 -> this.value = 90 + this.rnd.nextDouble() * 5;
            case 2 -> this.value = 95 + this.rnd.nextDouble() * 1;
        }
    }
}