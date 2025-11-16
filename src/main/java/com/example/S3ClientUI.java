package com.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;

public class S3ClientUI extends JFrame {

    private JComboBox<Profile> profileComboBox;
    private JTextField bucketNameField;
    private JTextField clientIdField;
    private JPasswordField clientSecretField;
    private JTextField regionField;
    private JTextField pathField;
    private JTextArea logArea;
    private JList<String> objectList;
    private DefaultListModel<String> listModel;
    private JButton deleteButton;
    private JButton getObjectButton;
    private JButton uploadButton;

    private S3Service s3Service;
    private ProfileManager profileManager;

    public S3ClientUI() {
        profileManager = new ProfileManager();

        setTitle("S3 Client");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Top Panel
        JPanel topPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        topPanel.add(new JLabel("Profile:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        profileComboBox = new JComboBox<>();
        topPanel.add(profileComboBox, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        JButton newProfileButton = new JButton("New Profile");
        topPanel.add(newProfileButton, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        topPanel.add(new JLabel("Bucket Name:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        bucketNameField = new JTextField(20);
        topPanel.add(bucketNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        topPanel.add(new JLabel("Client ID:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 2;
        clientIdField = new JTextField(20);
        topPanel.add(clientIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        topPanel.add(new JLabel("Client Secret:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 3;
        clientSecretField = new JPasswordField(20);
        topPanel.add(clientSecretField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        topPanel.add(new JLabel("Region:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        regionField = new JTextField(20);
        topPanel.add(regionField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        topPanel.add(new JLabel("Path or Prefix:"), gbc);

        gbc.gridx = 1;
        gbc.gridy = 5;
        pathField = new JTextField(20);
        topPanel.add(pathField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        JButton saveProfileButton = new JButton("Save Profile");
        topPanel.add(saveProfileButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 5;
        JButton listButton = new JButton("List");
        topPanel.add(listButton, gbc);

        // Main Panel with Tabs
        JTabbedPane tabbedPane = new JTabbedPane();

        // List Tab
        JPanel listPanel = new JPanel(new BorderLayout());
        listModel = new DefaultListModel<>();
        objectList = new JList<>(listModel);
        listPanel.add(new JScrollPane(objectList), BorderLayout.CENTER);

        // Add selection listener to enable/disable delete button based on selection
        objectList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Prevent multiple events during selection
                deleteButton.setEnabled(objectList.getSelectedValue() != null);
            }
        });

        JPanel buttonPanel = new JPanel();
        getObjectButton = new JButton("Get Object");
        uploadButton = new JButton("Upload");
        deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false); // Initially disabled until an object is selected
        buttonPanel.add(getObjectButton);
        buttonPanel.add(uploadButton);
        buttonPanel.add(deleteButton);
        listPanel.add(buttonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("List", listPanel);

        // Log Tab
        logArea = new JTextArea();
        logArea.setEditable(false);
        tabbedPane.addTab("Log", new JScrollPane(logArea));

        // Add panels to frame
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);

        // Load Profiles
        loadProfiles();

        // Auto-select first profile if available
        if (profileComboBox.getItemCount() > 0) {
            profileComboBox.setSelectedIndex(0);
            selectProfile(); // Populate the fields with the first profile's data
        }

        // Add Action Listeners
        profileComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectProfile();
            }
        });

        newProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                newProfile();
            }
        });

        saveProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveProfile();
            }
        });

        listButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                listObjects();
            }
        });

        getObjectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                getObject();
            }
        });

        uploadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                uploadObject();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteObject();
            }
        });
    }

    private void loadProfiles() {
        profileComboBox.removeAllItems();
        List<Profile> profiles = profileManager.getProfiles();
        for (Profile profile : profiles) {
            profileComboBox.addItem(profile);
        }
    }

    private void selectProfile() {
        Profile selectedProfile = (Profile) profileComboBox.getSelectedItem();
        if (selectedProfile != null) {
            bucketNameField.setText(selectedProfile.getBucketName());
            clientIdField.setText(selectedProfile.getClientId());
            clientSecretField.setText(selectedProfile.getClientSecret());
            regionField.setText(selectedProfile.getRegion());
            if (selectedProfile.getClientId() != null && !selectedProfile.getClientId().isEmpty() &&
                selectedProfile.getClientSecret() != null && !selectedProfile.getClientSecret().isEmpty()) {
                s3Service = new S3Service(selectedProfile.getClientId(), selectedProfile.getClientSecret(), selectedProfile.getRegion());
                log("Profile '" + selectedProfile.getProfileName() + "' loaded.");
            }
        }
    }

    private void newProfile() {
        String profileName = JOptionPane.showInputDialog(this, "Enter Profile Name:", "New Profile", JOptionPane.PLAIN_MESSAGE);
        if (profileName != null && !profileName.isEmpty()) {
            bucketNameField.setText("");
            clientIdField.setText("");
            clientSecretField.setText("");
            regionField.setText("us-east-1");
            Profile newProfile = new Profile(profileName, "", "", "", "us-east-1");
            profileComboBox.addItem(newProfile);
            profileComboBox.setSelectedItem(newProfile);
        }
    }

    private void saveProfile() {
        Profile selectedProfile = (Profile) profileComboBox.getSelectedItem();
        if (selectedProfile == null) {
            log("No profile selected to save.");
            return;
        }

        String profileName = selectedProfile.getProfileName();
        String bucketName = bucketNameField.getText();
        String clientId = clientIdField.getText();
        String clientSecret = new String(clientSecretField.getPassword());
        String region = regionField.getText().trim();

        selectedProfile.setBucketName(bucketName);
        selectedProfile.setClientId(clientId);
        selectedProfile.setClientSecret(clientSecret);
        selectedProfile.setRegion(region.isEmpty() ? "us-east-1" : region);

        profileManager.saveProfile(selectedProfile);
        log("Profile '" + profileName + "' saved.");
    }

    private void listObjects() {
        String bucketName = bucketNameField.getText();
        String path = pathField.getText();
        if (s3Service == null || bucketName == null || bucketName.isEmpty()) {
            log("Please select a profile and enter a bucket name.");
            return;
        }
        log("Listing objects in bucket '" + bucketName + "' at path '" + path + "'");
        listModel.clear();
        try {
            List<String> objects = s3Service.listObjects(bucketName, path);
            if (objects.isEmpty()) {
                listModel.addElement("No objects in the given path");
            } else {
                for (String object : objects) {
                    listModel.addElement(object);
                }
            }
        } catch (Exception e) {
            log("Error listing objects: " + e.getMessage());
        }
    }

    private void getObject() {
        String selectedObject = objectList.getSelectedValue();
        if (selectedObject != null) {
            String bucketName = bucketNameField.getText();
            log("Getting object '" + selectedObject + "' from bucket '" + bucketName + "'");
            try {
                String content = s3Service.getObject(bucketName, selectedObject);
                JTextArea textArea = new JTextArea(content);
                textArea.setEditable(false);
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(500, 400));
                JOptionPane.showMessageDialog(this, scrollPane, "Object Content", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception e) {
                log("Error getting object: " + e.getMessage());
            }
        } else {
            log("Please select an object to get.");
        }
    }

    private void uploadObject() {
        String bucketName = bucketNameField.getText();
        String path = pathField.getText();
        if (s3Service == null || bucketName == null || bucketName.isEmpty()) {
            log("Please select a profile and enter a bucket name.");
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            String key = path.isEmpty() ? fileChooser.getSelectedFile().getName() : path + "/" + fileChooser.getSelectedFile().getName();
            log("Uploading file '" + filePath + "' to bucket '" + bucketName + "' with key '" + key + "'");
            try {
                s3Service.putObject(bucketName, key, new java.io.File(filePath));
                listObjects();
            } catch (Exception e) {
                log("Error uploading file: " + e.getMessage());
            }
        }
    }

    private void deleteObject() {
        String selectedObject = objectList.getSelectedValue();
        if (selectedObject != null) {
            // Show confirmation dialog
            int result = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to delete the object '" + selectedObject + "'?\nThis action cannot be undone.",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (result == JOptionPane.YES_OPTION) {
                String bucketName = bucketNameField.getText();
                log("Deleting object '" + selectedObject + "' from bucket '" + bucketName + "'");
                try {
                    s3Service.deleteObject(bucketName, selectedObject);
                    listObjects(); // Refresh the list after deletion
                } catch (Exception e) {
                    log("Error deleting object: " + e.getMessage());
                }
            } else {
                log("Deletion of '" + selectedObject + "' was cancelled by the user.");
            }
        } else {
            log("Please select an object to delete.");
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new S3ClientUI().setVisible(true);
            }
        });
    }
}