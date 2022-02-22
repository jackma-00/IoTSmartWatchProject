package it.unimore.iot.project.smartwatch.serverside;

public class InfoMessageDescriptorSS {

    private String deviceId;
    private String producer;
    private String softwareVersion;

    public InfoMessageDescriptorSS() {
    }

    public InfoMessageDescriptorSS(String deviceId, String producer, String softwareVersion) {
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

    @Override
    public String toString() {
        return "InfoMessageDescriptorSS{" +
                "deviceId='" + deviceId + '\'' +
                ", producer='" + producer + '\'' +
                ", softwareVersion='" + softwareVersion + '\'' +
                '}';
    }
}