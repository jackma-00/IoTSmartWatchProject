package it.unimore.iot.project.smartwatch.process;

import it.unimore.iot.project.smartwatch.conf.SensorsTypes;
import it.unimore.iot.project.smartwatch.conf.SmartWatchConf;
import it.unimore.iot.project.smartwatch.models.SensorDescriptor;
import it.unimore.iot.project.smartwatch.models.SmartWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SmartWatchProcessGUI extends JFrame implements ActionListener {

    private final static Logger logger = LoggerFactory.getLogger(SmartWatchProcess.class);

    // GUI components
    private final JButton onButton; // serves the client connection
    private final JButton startButton; // serves the waiting for the command START from the subscriber
    private final JButton measureButton; // measures and sends data to the subscriber as well as notifying finish
    private final JButton offButton; // serves the client disconnection
    private final JTextArea display; // basic interface

    // Smartwatch (the core object that allow the sensing and communication)
    private final SmartWatch smartWatch;

    public SmartWatchProcessGUI() throws HeadlessException {

        // Buttons init -------------------------------------------------------------------------------------------------

        // left plane where to insert the buttons
        JPanel leftPanel = new JPanel(new GridLayout(4,1));
        leftPanel.setBorder (new TitledBorder(new EtchedBorder()));

        // create left plane components
        onButton = new JButton("on");
        onButton.setBackground(Color.DARK_GRAY);
        onButton.setForeground(Color.green);
        onButton.addActionListener(this);

        startButton = new JButton("start");
        startButton.setBackground(Color.DARK_GRAY);
        startButton.setForeground(Color.green);
        startButton.addActionListener(this);

        measureButton = new JButton("send");
        measureButton.setBackground(Color.DARK_GRAY);
        measureButton.setForeground(Color.green);
        measureButton.addActionListener(this);

        offButton = new JButton("off");
        offButton.setBackground(Color.DARK_GRAY);
        offButton.setForeground(Color.green);
        offButton.addActionListener(this);

        // add button into the left panel
        leftPanel.add(onButton);
        leftPanel.add(startButton);
        leftPanel.add(measureButton);
        leftPanel.add(offButton);

        // Display init -----------------------------------------------------------------------------------------------

        JPanel middlePanel = new JPanel();
        middlePanel.setBorder (new TitledBorder(new EtchedBorder()));

        // create the middle panel components
        display = new JTextArea (16, 58);
        display.setBackground(Color.gray);
        display.setEditable (false); // set textArea non-editable
        JScrollPane scroll = new JScrollPane(display);
        scroll.setVerticalScrollBarPolicy (ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        //Add Textarea in to middle panel);
        middlePanel.add(scroll);

        // Frame init --------------------------------------------------------------------------------------------------

        JPanel globalPanel = new JPanel(new BorderLayout());
        globalPanel.setBorder (new TitledBorder(new EtchedBorder()));
        globalPanel.add(leftPanel, BorderLayout.WEST);
        globalPanel.add(middlePanel, BorderLayout.EAST);

        JFrame frame = new JFrame();
        frame.add(globalPanel);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible (true);
        frame.setTitle("Smartwatch");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // Smartwatch init ---------------------------------------------------------------------------------------------

        // initialize single sensors attached to smartwatch
        SensorDescriptor temperatureSensor = new SensorDescriptor(SensorsTypes.TEMPERATURE_SENSOR_TYPE,
                SensorsTypes.TEMPERATURE_SENSOR_UNIT);
        SensorDescriptor heartRateSensor = new SensorDescriptor(SensorsTypes.HEART_RATE_SENSOR_TYPE,
                SensorsTypes.HEART_RATE_SENSOR_UNIT);
        SensorDescriptor glucoseSensor = new SensorDescriptor(SensorsTypes.GLUCOSE_SENSOR_TYPE,
                SensorsTypes.GLUCOSE_SENSOR_UNIT);
        SensorDescriptor saturationSensor = new SensorDescriptor(SensorsTypes.SATURATION_SENSOR_TYPE,
                SensorsTypes.SATURATION_SENSOR_UNIT);

        // initialize smartwatch as a sensing component
        smartWatch = new SmartWatch(SmartWatchConf.SMARTWATCH_ID,
                SmartWatchConf.PRODUCER,
                SmartWatchConf.SOFTWARE_VERSION);
        smartWatch.addSensor(temperatureSensor);
        smartWatch.addSensor(heartRateSensor);
        smartWatch.addSensor(glucoseSensor);
        smartWatch.addSensor(saturationSensor);

        logger.info("Smartwatch initialization completed successfully!");

    }

    // events pressing the buttons --------------------------------------------------------------------------------------

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == onButton) {

            // turn on the device and initialize it as a MQTT client

            // Display int
            display.setBackground(Color.cyan);
            display.setText(smartWatch.toString());

            // client connection
            smartWatch.connectClient();

            logger.info("Connected!\n");

            // publishing info data as a default
            logger.info("Publishing smartwatch info ...");
            smartWatch.publishDeviceInfo();
            logger.info("Smartwatch info published correctly!\n");

        }
        if (e.getSource() == startButton) {

            try {

                // Display int
                display.setText(null);
                display.setText("Waiting ...");

                // trigger the blocking process in order to wait for a command START
                logger.info("Waiting for command to start ...");
                smartWatch.waitToStart();
                logger.info("Session Started!\n");

            } catch (Exception exception) {
                logger.error("Something went wrong waiting for signal start!\n");
                exception.printStackTrace();
            }

        }
        if (e.getSource() == measureButton) {

            try {

                // measure and publish data
                logger.info("Measuring data ...");
                smartWatch.senseAndPublishResults();
                logger.info("Smartwatch telemetry data published correctly!\n");

                // view the results of the current measurement on the display
                display.setText(null);
                display.setText(smartWatch.getTelemetrySenMLPack());

                // trigger the disconnection of the other client (subscriber)
                smartWatch.notifyFinish();
                logger.info("Finish sensing session!\n");

            } catch (Exception exception) {
                logger.error("Something went wrong measuring data!\n");
                exception.printStackTrace();
            }

        }
        if (e.getSource() == offButton) {

            // disconnect client and turn off the device
            display.setText(null);
            display.setBackground(Color.gray);

            smartWatch.disconnectClient();
            logger.info("Disconnected !");

        }
    }

    public static void main(String[] args) {
            SwingUtilities.invokeLater(SmartWatchProcessGUI::new);
    }
}
