package it.unimore.iot.project.smartwatch.models;

public class TelemetryMessageDescriptor {

    private String deviceId;
    private String type;
    private long timestamp;
    private String unit;
    private double value;

    public TelemetryMessageDescriptor() {
    }

    public TelemetryMessageDescriptor(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "TelemetryMessageDescriptor{" +
                "deviceId='" + deviceId + '\'' +
                ", type='" + type + '\'' +
                ", timestamp=" + timestamp +
                ", unit='" + unit + '\'' +
                ", value=" + value +
                '}';
    }
}
