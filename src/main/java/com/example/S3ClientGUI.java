package com.example;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class S3ClientGUI extends JFrame {

    private S3Service s3Service;
    private Properties prop = new Properties();
    private final String configFilePath = "config.properties";

    private JTextField bucketNameField = new JTextField(20);
    private JTextField keyField = new JTextField(20);
    private JTextField clientIdField = new JTextField(20);
    private JPasswordField clientSecretField = new JPasswordField(20);
    private JTextArea outputArea = new JTextArea();

    public S3ClientGUI() {
        setTitle("S3 Client");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create components
        JButton saveButton = new JButton("Save");

        JButton createBucketButton = new JButton("Create Bucket");
        JButton listBucketsButton = new JButton("List Buckets");
        JButton putObjectButton = new JButton("Upload Object");
        JButton getObjectButton = new JButton("Get Object");
        JButton deleteObjectButton = new JButton("Delete Object");
        JButton deleteBucketButton = new JButton("Delete Bucket");

        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);

        // Layout components
        JPanel configPanel = new JPanel(new GridLayout(5, 2));
        configPanel.add(new JLabel("Bucket Name:"));
        configPanel.add(bucketNameField);
        configPanel.add(new JLabel("Object Key:"));
        configPanel.add(keyField);
        configPanel.add(new JLabel("Client ID:"));
        configPanel.add(clientIdField);
        configPanel.add(new JLabel("Client Secret:"));
        configPanel.add(clientSecretField);
        configPanel.add(new JLabel());
        configPanel.add(saveButton);

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createBucketButton);
        buttonPanel.add(listBucketsButton);
        buttonPanel.add(putObjectButton);
        buttonPanel.add(getObjectButton);
        buttonPanel.add(deleteObjectButton);
        buttonPanel.add(deleteBucketButton);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(configPanel, BorderLayout.NORTH);
        topPanel.add(buttonPanel, BorderLayout.CENTER);

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(topPanel, BorderLayout.NORTH);
        contentPane.add(scrollPane, BorderLayout.CENTER);

        // Load config and initialize S3Service
        loadConfig();
        s3Service = new S3Service(clientIdField.getText(), new String(clientSecretField.getPassword()));

        // Add action listeners
        saveButton.addActionListener(e -> saveConfig());
        createBucketButton.addActionListener(e -> s3Service.createBucket(bucketNameField.getText()));
        listBucketsButton.addActionListener(e -> s3Service.listBuckets());
        putObjectButton.addActionListener(e -> s3Service.putObject(bucketNameField.getText(), keyField.getText(), "Hello, S3!"));
        getObjectButton.addActionListener(e -> outputArea.append(s3Service.getObject(bucketNameField.getText(), keyField.getText()) + "\n"));
        deleteObjectButton.addActionListener(e -> s3Service.deleteObject(bucketNameField.getText(), keyField.getText()));
        deleteBucketButton.addActionListener(e -> s3Service.deleteBucket(bucketNameField.getText()));

        // Add appender to logger
        Logger root = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        TextAreaAppender appender = new TextAreaAppender(outputArea);
        appender.start();
        root.addAppender(appender);
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(configFilePath)) {
            if (input == null) {
                outputArea.append("Sorry, unable to find " + configFilePath + "\n");
                return;
            }
            prop.load(input);
            bucketNameField.setText(prop.getProperty("bucketName"));
            keyField.setText(prop.getProperty("key"));
            clientIdField.setText(prop.getProperty("clientId"));
            clientSecretField.setText(prop.getProperty("clientSecret"));
        } catch (IOException ex) {
            outputArea.append("Failed to load config file.\n");
        }
    }

    private void saveConfig() {
        String home = System.getProperty("user.home");
        try (FileOutputStream output = new FileOutputStream(home + "/" + configFilePath)) {
            prop.setProperty("bucketName", bucketNameField.getText());
            prop.setProperty("key", keyField.getText());
            prop.setProperty("clientId", clientIdField.getText());
            prop.setProperty("clientSecret", new String(clientSecretField.getPassword()));
            prop.store(output, null);
            outputArea.append("Config saved to " + home + "/" + configFilePath + "\n");
            s3Service = new S3Service(clientIdField.getText(), new String(clientSecretField.getPassword()));
        } catch (IOException ex) {
            outputArea.append("Failed to save config file.\n");
        }
    }

    public static class TextAreaAppender extends AppenderBase<ILoggingEvent> {
        private JTextArea textArea;

        public TextAreaAppender(JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        protected void append(ILoggingEvent eventObject) {
            SwingUtilities.invokeLater(() -> {
                textArea.append(eventObject.getFormattedMessage() + "\n");
            });
        }
    }
}
