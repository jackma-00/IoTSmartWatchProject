package it.unimore.iot.project.smartwatch.process;

import it.unimore.iot.project.smartwatch.conf.SensorsTypes;
import it.unimore.iot.project.smartwatch.conf.SmartWatchConf;
import it.unimore.iot.project.smartwatch.models.SensorDescriptor;
import it.unimore.iot.project.smartwatch.models.SmartWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmartWatchProcess {

    private final static Logger logger = LoggerFactory.getLogger(SmartWatchProcess.class);

    public static void main(String[] args) {

        logger.info("Smartwatch process started ...\n");

        try {

            // 1. initialize single sensors
            SensorDescriptor temperatureSensor = new SensorDescriptor(SensorsTypes.TEMPERATURE_SENSOR_TYPE,
                    SensorsTypes.TEMPERATURE_SENSOR_UNIT);
            SensorDescriptor heartRateSensor = new SensorDescriptor(SensorsTypes.HEART_RATE_SENSOR_TYPE,
                    SensorsTypes.HEART_RATE_SENSOR_UNIT);
            SensorDescriptor glucoseSensor = new SensorDescriptor(SensorsTypes.GLUCOSE_SENSOR_TYPE,
                    SensorsTypes.GLUCOSE_SENSOR_UNIT);
            SensorDescriptor saturationSensor = new SensorDescriptor(SensorsTypes.SATURATION_SENSOR_TYPE,
                    SensorsTypes.SATURATION_SENSOR_UNIT);

            // 2. initialize smartwatch as a sensor
            SmartWatch smartWatch = new SmartWatch(SmartWatchConf.SMARTWATCH_ID,
                    SmartWatchConf.PRODUCER,
                    SmartWatchConf.SOFTWARE_VERSION);
            smartWatch.addSensor(temperatureSensor);
            smartWatch.addSensor(heartRateSensor);
            smartWatch.addSensor(glucoseSensor);
            smartWatch.addSensor(saturationSensor);

            logger.info("Smartwatch initialization completed successfully!");

            // 3. initialize smartwatch as a MQTT client
            smartWatch.connectClient();

            logger.info("Connected!\n");

            // 4. publishing info data
            logger.info("Publishing smartwatch info ...");
            smartWatch.publishDeviceInfo();
            logger.info("Smartwatch info published correctly!\n");

            // 5. subscribing at the command topic, and wait until the measurement process is triggered
            logger.info("Waiting for command to start ...");
            smartWatch.waitToStart();
            logger.info("Session Started!\n");

            // 6. measure and publish data
            logger.info("Measuring data ...");
            smartWatch.senseAndPublishResults();
            logger.info("Smartwatch telemetry data published correctly!\n");

            // trigger the disconnection of the other client
            smartWatch.notifyFinish();
            logger.info("Finish sensing session!\n");

            // 7. disconnect and close
            smartWatch.disconnectClient();
            logger.info("Disconnected !");


        } catch (Exception e){
            logger.error("Something went wrong during the process\n");
            e.printStackTrace();
        }
    }

}
