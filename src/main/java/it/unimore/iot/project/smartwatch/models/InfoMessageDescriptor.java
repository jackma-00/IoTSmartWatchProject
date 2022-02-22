package it.unimore.iot.project.smartwatch.models;

import java.util.ArrayList;
import java.util.List;

public class InfoMessageDescriptor {

    private String deviceId;
    private String producer;
    private String softwareVersion;
    private final List<String> sensors;

    public InfoMessageDescriptor() {
        this.sensors = new ArrayList<String>();
    }

    public InfoMessageDescriptor(String deviceId, String producer, String softwareVersion) {
        this();
        this.deviceId = deviceId;
        this.producer = producer;
        this.softwareVersion = softwareVersion;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getProducer() {
        return producer;
    }

    public void setProducer(String producer) {
        this.producer = producer;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public List<String> getSensors() {
        return sensors;
    }

    public void addSensor(String sensor) {
        if (this.sensors != null && sensor != null){
            this.sensors.add(sensor);
        }
        else{
            System.err.println("Error adding new sensor");
        }
    }
}