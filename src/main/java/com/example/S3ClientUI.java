package com.example;

import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

public class S3ClientUI extends JFrame {

    private JComboBox<Profile> profileComboBox;
    private JTextField bucketNameField;
    private JTextField clientIdField;
    private JPasswordField clientSecretField;
    private JTextField regionField;
    private JTextField pathField;
    private JEditorPane logArea;
    private JScrollPane logScrollPane;
    private JList<String> objectList;
    private DefaultListModel<String> listModel;
    private JButton deleteButton;
    private JButton getObjectButton;
    private JButton uploadButton;
    // Tree view components
    private JTree bucketTree;
    private DefaultTreeModel treeModel;
    private JButton treeRefreshButton;
    private JButton treeGetObjectButton;
    private JButton treeDeleteButton;
    private JButton treeUploadButton;
    private JButton editProfileButton;
    private JButton saveProfileButton;

    private S3Service s3Service;
    private ProfileManager profileManager;

    public S3ClientUI() {
        profileManager = new ProfileManager();

        setTitle("Free S3 Client - MIT License");
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
        // Add Enter key listener to trigger List button action
        pathField.addActionListener(e -> listObjects());
        topPanel.add(pathField, gbc);

        gbc.gridx = 2;
        gbc.gridy = 1;
        saveProfileButton = new JButton("Save Profile");
        topPanel.add(saveProfileButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 2;
        JButton deleteProfileButton = new JButton("Delete Profile");
        topPanel.add(deleteProfileButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 3;
        JButton copyProfileButton = new JButton("Copy Profile");
        topPanel.add(copyProfileButton, gbc);

        gbc.gridx = 2;
        gbc.gridy = 4;
        editProfileButton = new JButton("Edit");
        topPanel.add(editProfileButton, gbc);

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
        // Add border to the list
        objectList.setBorder(BorderFactory.createEtchedBorder());
        listPanel.add(new JScrollPane(objectList), BorderLayout.CENTER);

        // Add selection listener to enable/disable get and delete buttons based on selection
        objectList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) { // Prevent multiple events during selection
                boolean hasSelection = objectList.getSelectedValue() != null;
                getObjectButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
            }
        });

        JPanel buttonPanel = new JPanel();
        getObjectButton = new JButton("Get Object");
        getObjectButton.setEnabled(false); // Initially disabled until an object is selected
        uploadButton = new JButton("Upload");
        deleteButton = new JButton("Delete");
        deleteButton.setEnabled(false); // Initially disabled until an object is selected
        buttonPanel.add(getObjectButton);
        buttonPanel.add(uploadButton);
        buttonPanel.add(deleteButton);
        listPanel.add(buttonPanel, BorderLayout.SOUTH);

        tabbedPane.addTab("List", listPanel);

        // Tree View Tab
        JPanel treePanel = new JPanel(new BorderLayout());
        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("Root"));
        treeModel.setAsksAllowsChildren(true);
        bucketTree = new JTree(treeModel);
        treePanel.add(new JScrollPane(bucketTree), BorderLayout.CENTER);

        // Create button panel for tree operations
        JPanel treeButtonPanel = new JPanel();
        treeRefreshButton = new JButton("Refresh Tree");
        treeGetObjectButton = new JButton("Download");
        treeDeleteButton = new JButton("Delete");
        treeUploadButton = new JButton("Upload to Path");

        treeGetObjectButton.setEnabled(false);
        treeDeleteButton.setEnabled(false);

        treeButtonPanel.add(treeRefreshButton);
        treeButtonPanel.add(treeGetObjectButton);
        treeButtonPanel.add(treeDeleteButton);
        treeButtonPanel.add(treeUploadButton);

        treePanel.add(treeButtonPanel, BorderLayout.SOUTH);

        // Add selection listener to enable/disable tree buttons based on selection
        bucketTree.addTreeSelectionListener(e -> {
            TreePath selectedPath = e.getPath();
            if (selectedPath != null) {
                DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
                if (selectedNode.getUserObject() instanceof String && !selectedNode.isLeaf()) {
                    // Directory node - enable upload only
                    treeGetObjectButton.setEnabled(false);
                    treeDeleteButton.setEnabled(false);
                    treeUploadButton.setEnabled(true);
                } else if (selectedNode.isLeaf() && selectedNode.getUserObject() instanceof String) {
                    // File node - enable download and delete
                    treeGetObjectButton.setEnabled(true);
                    treeDeleteButton.setEnabled(true);
                    treeUploadButton.setEnabled(true);
                } else {
                    // Root node or other - disable file operations
                    treeGetObjectButton.setEnabled(false);
                    treeDeleteButton.setEnabled(false);
                    treeUploadButton.setEnabled(false);
                }
            } else {
                treeGetObjectButton.setEnabled(false);
                treeDeleteButton.setEnabled(false);
                treeUploadButton.setEnabled(false);
            }
        });

        tabbedPane.addTab("Tree View", treePanel);

        // Log Tab
        logArea = new JEditorPane();
        logArea.setEditable(false);
        logArea.setContentType("text/html");
        logArea.setText("<html><body></body></html>");
        logScrollPane = new JScrollPane(logArea);
        tabbedPane.addTab("Log", logScrollPane);

        // Add tab change listener to auto-refresh tree when Tree View tab is selected and list when List tab is selected
        tabbedPane.addChangeListener(e -> {
            JTabbedPane sourceTabbedPane = (JTabbedPane) e.getSource();
            int selectedTabIndex = sourceTabbedPane.getSelectedIndex();
            String selectedTabTitle = sourceTabbedPane.getTitleAt(selectedTabIndex);

            if ("Tree View".equals(selectedTabTitle)) {
                // Tree View tab was selected, auto-refresh if we have a valid profile
                autoRefreshTreeView();
            } else if ("List".equals(selectedTabTitle)) {
                // List tab was selected, auto-refresh if we have a valid profile
                autoRefreshListView();
            }
        });

        // Add Action Listeners for tree buttons after the UI components are initialized
        treeRefreshButton.addActionListener(e -> refreshTreeView());
        treeGetObjectButton.addActionListener(e -> treeGetSelectedObject());
        treeDeleteButton.addActionListener(e -> treeDeleteSelectedObject());
        treeUploadButton.addActionListener(e -> treeUploadToObjectPath());

        // Add Action Listeners for profile buttons after the UI components are initialized
        editProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setProfileFieldsEditable(true);
                log("Profile fields are now editable. Make changes and click 'Save Profile' to save.");
            }
        });

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

        // Initially set fields as non-editable except path field
        setProfileFieldsEditable(false);

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

        deleteProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteProfile();
            }
        });

        copyProfileButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                copyProfile();
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
        // Set profile fields as non-editable by default after loading
        setProfileFieldsEditable(false);
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
            // Set profile fields as non-editable by default after creating a new profile
            setProfileFieldsEditable(false);
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
        // Set profile fields back to non-editable after saving
        setProfileFieldsEditable(false);
    }

    private void deleteProfile() {
        Profile selectedProfile = (Profile) profileComboBox.getSelectedItem();
        if (selectedProfile == null) {
            log("No profile selected to delete.");
            return;
        }

        // Show confirmation dialog
        int result = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the profile '" + selectedProfile.getProfileName() + "'?\nThis action cannot be undone.",
            "Confirm Profile Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );

        if (result == JOptionPane.YES_OPTION) {
            int selectedIndex = profileComboBox.getSelectedIndex();

            // Remove the profile from the combo box
            profileComboBox.removeItemAt(selectedIndex);

            // Remove the profile from the profile manager
            profileManager.deleteProfile(selectedProfile);

            log("Profile '" + selectedProfile.getProfileName() + "' deleted.");

            // Select the next available profile, or clear the fields if none remain
            int itemCount = profileComboBox.getItemCount();
            if (itemCount > 0) {
                // If we deleted the last item, select the new last item, otherwise select the same index
                int newSelectedIndex = selectedIndex >= itemCount ? itemCount - 1 : selectedIndex;
                profileComboBox.setSelectedIndex(newSelectedIndex);
                selectProfile();
            } else {
                // If no profiles remain, clear the fields
                bucketNameField.setText("");
                clientIdField.setText("");
                clientSecretField.setText("");
                regionField.setText("us-east-1");
                s3Service = null;
            }
        } else {
            log("Deletion of profile '" + selectedProfile.getProfileName() + "' was cancelled by the user.");
        }
    }

    private void copyProfile() {
        Profile selectedProfile = (Profile) profileComboBox.getSelectedItem();
        if (selectedProfile == null) {
            log("No profile selected to copy.");
            JOptionPane.showMessageDialog(this, "Please select a profile to copy first.",
                "No Profile Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String newProfileName = JOptionPane.showInputDialog(this,
            "Enter new profile name (copying from: " + selectedProfile.getProfileName() + "):",
            "Copy Profile",
            JOptionPane.PLAIN_MESSAGE);

        if (newProfileName != null && !newProfileName.isEmpty()) {
            // Check if a profile with the same name already exists
            boolean profileExists = false;
            for (int i = 0; i < profileComboBox.getItemCount(); i++) {
                Profile profile = (Profile) profileComboBox.getItemAt(i);
                if (profile.getProfileName().equals(newProfileName)) {
                    profileExists = true;
                    break;
                }
            }

            if (profileExists) {
                JOptionPane.showMessageDialog(this,
                    "A profile with the name '" + newProfileName + "' already exists.",
                    "Profile Name Exists",
                    JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Create new profile with same settings as the selected profile
            Profile newProfile = new Profile(
                newProfileName,
                selectedProfile.getBucketName(),
                selectedProfile.getClientId(),
                selectedProfile.getClientSecret(),
                selectedProfile.getRegion()  // Copy the region as well!
            );

            profileComboBox.addItem(newProfile);
            profileComboBox.setSelectedItem(newProfile);
            selectProfile(); // Update UI with the new profile's data
            log("Profile '" + selectedProfile.getProfileName() + "' copied to '" + newProfileName + "'");
            // Set profile fields as non-editable by default after copying
            setProfileFieldsEditable(false);
        }
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

            // Sort the objects alphabetically
            objects.sort(String::compareTo);

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
                // Get the object content from S3
                String content = s3Service.getObject(bucketName, selectedObject);

                // Extract file name from the selected object path
                String fileName = selectedObject.substring(selectedObject.lastIndexOf('/') + 1);
                if (fileName.isEmpty()) {
                    fileName = selectedObject; // If no path separator, use the whole string
                }

                // Create a file save dialog
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setDialogTitle("Save Object As");
                fileChooser.setSelectedFile(new java.io.File(fileName));

                int userSelection = fileChooser.showSaveDialog(this);

                if (userSelection == JFileChooser.APPROVE_OPTION) {
                    java.io.File fileToSave = fileChooser.getSelectedFile();

                    // Write the content to the selected file
                    try (java.io.FileWriter fileWriter = new java.io.FileWriter(fileToSave)) {
                        fileWriter.write(content);
                        log("Object '" + selectedObject + "' saved to '" + fileToSave.getAbsolutePath() + "'");
                    } catch (java.io.IOException e) {
                        log("Error writing file: " + e.getMessage());
                        JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(),
                            "Save Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (Exception e) {
                log("Error getting object: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error getting object from S3: " + e.getMessage(),
                    "S3 Error", JOptionPane.ERROR_MESSAGE);
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
        // Determine if this is an error message based on content
        boolean isError = message.toLowerCase().contains("error") ||
                         message.toLowerCase().contains("exception") ||
                         message.toLowerCase().contains("failed") ||
                         message.toLowerCase().contains("invalid");

        // Get current text
        String currentText = logArea.getText();

        // Prepare HTML for the new message
        String color = isError ? "red" : "black";
        String newMessage = "<font color=\"" + color + "\">" + escapeHtml(message) + "</font><br>";

        // Set the updated HTML content
        String updatedText = currentText.substring(0, currentText.lastIndexOf("</body>")) +
                            newMessage +
                            "</body></html>";
        logArea.setText(updatedText);

        // Scroll to bottom
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }

    private void autoRefreshTreeView() {
        // Check if we have a valid profile and S3 service
        if (s3Service == null) {
            log("S3 service not initialized. Please select a valid profile first.");
            return;
        }

        // Check if we have a bucket name
        String bucketName = bucketNameField.getText();
        if (bucketName == null || bucketName.isEmpty()) {
            log("No bucket name specified. Please enter a bucket name first.");
            return;
        }

        // Check if the tree is already showing this bucket to avoid unnecessary refreshes
        if (treeModel.getRoot() != null) {
            DefaultMutableTreeNode currentRoot = (DefaultMutableTreeNode) treeModel.getRoot();
            if (currentRoot.getUserObject() != null &&
                currentRoot.getUserObject().equals(bucketName)) {
                // Tree already shows the correct bucket, no need to refresh
                return;
            }
        }

        log("Auto-refreshing tree view for bucket: " + bucketName);

        // Use SwingUtilities.invokeLater to ensure the refresh happens on the event dispatch thread
        SwingUtilities.invokeLater(() -> {
            try {
                // Get all objects from the bucket
                List<String> objects = s3Service.listObjects(bucketName, "");

                // Create new tree model with bucket as root
                DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(bucketName);

                // Build hierarchical tree structure
                for (String objectKey : objects) {
                    String[] parts = objectKey.split("/");
                    DefaultMutableTreeNode currentNode = rootNode;

                    // Navigate/create the path structure
                    for (int i = 0; i < parts.length - 1; i++) {
                        String part = parts[i];
                        DefaultMutableTreeNode childNode = findOrCreateChild(currentNode, part);
                        currentNode = childNode;
                    }

                    // Add the final file as a leaf node
                    if (parts.length > 0) {
                        String fileName = parts[parts.length - 1];
                        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileName);
                        currentNode.add(fileNode);
                    }
                }

                treeModel.setRoot(rootNode);
                treeModel.reload(); // Refresh the tree display

                // Expand all nodes for better visibility
                expandAllNodes(bucketTree, new TreePath(rootNode));

                log("Tree view auto-refreshed with " + objects.size() + " objects.");
            } catch (Exception e) {
                log("Error auto-refreshing tree view: " + e.getMessage());
                // Clear the tree to show empty state on error
                SwingUtilities.invokeLater(() -> {
                    DefaultMutableTreeNode emptyRoot = new DefaultMutableTreeNode("Error loading tree");
                    treeModel.setRoot(emptyRoot);
                    treeModel.reload();
                });
            }
        });
    }

    private void autoRefreshListView() {
        // Check if we have a valid profile and S3 service
        if (s3Service == null) {
            log("S3 service not initialized. Please select a valid profile first.");
            return;
        }

        // Check if we have a bucket name
        String bucketName = bucketNameField.getText();
        if (bucketName == null || bucketName.isEmpty()) {
            log("No bucket name specified. Please enter a bucket name first.");
            return;
        }

        log("Auto-refreshing list view for bucket: " + bucketName);

        // Use SwingUtilities.invokeLater to ensure the refresh happens on the event dispatch thread
        SwingUtilities.invokeLater(() -> {
            try {
                String path = pathField.getText(); // Use the current path/prefix from the field
                List<String> objects = s3Service.listObjects(bucketName, path);

                // Sort the objects alphabetically
                objects.sort(String::compareTo);

                // Clear the list model and add new objects
                listModel.clear();
                if (objects.isEmpty()) {
                    listModel.addElement("No objects in the given path");
                } else {
                    for (String object : objects) {
                        listModel.addElement(object);
                    }
                }

                log("List view auto-refreshed with " + objects.size() + " objects.");
            } catch (Exception e) {
                log("Error auto-refreshing list view: " + e.getMessage());
            }
        });
    }

    private void refreshTreeView() {
        String bucketName = bucketNameField.getText();
        if (bucketName == null || bucketName.isEmpty()) {
            log("Please enter a bucket name to refresh tree view.");
            return;
        }

        if (s3Service == null) {
            log("S3 service not initialized. Please select a valid profile.");
            return;
        }

        log("Refreshing tree view for bucket: " + bucketName);
        try {
            // Get all objects from the bucket
            List<String> objects = s3Service.listObjects(bucketName, "");

            // Create new tree model with bucket as root
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(bucketName);

            // Build hierarchical tree structure
            for (String objectKey : objects) {
                String[] parts = objectKey.split("/");
                DefaultMutableTreeNode currentNode = rootNode;

                // Navigate/create the path structure
                for (int i = 0; i < parts.length - 1; i++) {
                    String part = parts[i];
                    DefaultMutableTreeNode childNode = findOrCreateChild(currentNode, part);
                    currentNode = childNode;
                }

                // Add the final file as a leaf node
                if (parts.length > 0) {
                    String fileName = parts[parts.length - 1];
                    DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileName);
                    currentNode.add(fileNode);
                }
            }

            treeModel.setRoot(rootNode);
            treeModel.reload(); // Refresh the tree display

            // Expand all nodes for better visibility
            expandAllNodes(bucketTree, new TreePath(rootNode));

            log("Tree view refreshed with " + objects.size() + " objects.");
        } catch (Exception e) {
            log("Error refreshing tree view: " + e.getMessage());
            // Clear the tree to show empty state on error
            DefaultMutableTreeNode emptyRoot = new DefaultMutableTreeNode("Error loading tree");
            treeModel.setRoot(emptyRoot);
            treeModel.reload();
            JOptionPane.showMessageDialog(this, "Error refreshing tree view: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void setProfileFieldsEditable(boolean editable) {
        // Set all profile-related fields to the specified editable state
        bucketNameField.setEditable(editable);
        clientIdField.setEditable(editable);
        clientSecretField.setEditable(editable);
        regionField.setEditable(editable);

        // Path field remains always editable
        pathField.setEditable(true);

        // Update button states
        if (editable) {
            editProfileButton.setEnabled(false); // Disable edit button when in edit mode
            saveProfileButton.setEnabled(true);  // Enable save button when in edit mode
        } else {
            editProfileButton.setEnabled(true);  // Enable edit button when not in edit mode
            saveProfileButton.setEnabled(false); // Disable save button when not in edit mode
        }
    }

    private DefaultMutableTreeNode findOrCreateChild(DefaultMutableTreeNode parent, String childName) {
        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (child.getUserObject().equals(childName)) {
                return child;
            }
        }
        // If not found, create a new directory node
        DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(childName);
        parent.add(newChild);
        return newChild;
    }

    private void expandAllNodes(JTree tree, TreePath parent) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (int i = 0; i < node.getChildCount(); i++) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                expandAllNodes(tree, parent.pathByAddingChild(child));
            }
        }
        tree.expandPath(parent);
    }

    private void treeGetSelectedObject() {
        TreePath selectedPath = bucketTree.getSelectionPath();
        if (selectedPath != null) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            if (selectedNode.isLeaf() && selectedNode.getUserObject() instanceof String) {
                String fileName = (String) selectedNode.getUserObject();
                // Get the full path by traversing up the tree
                String fullPath = getFullObjectPath(selectedNode);

                String bucketName = bucketNameField.getText();
                log("Getting object '" + fullPath + "' from bucket '" + bucketName + "'");

                try {
                    // Get the object content from S3
                    String content = s3Service.getObject(bucketName, fullPath);

                    // Create a file save dialog
                    JFileChooser fileChooser = new JFileChooser();
                    fileChooser.setDialogTitle("Save Object As");
                    fileChooser.setSelectedFile(new java.io.File(fileName));

                    int userSelection = fileChooser.showSaveDialog(this);

                    if (userSelection == JFileChooser.APPROVE_OPTION) {
                        java.io.File fileToSave = fileChooser.getSelectedFile();

                        // Write the content to the selected file
                        try (java.io.FileWriter fileWriter = new java.io.FileWriter(fileToSave)) {
                            fileWriter.write(content);
                            log("Object '" + fullPath + "' saved to '" + fileToSave.getAbsolutePath() + "'");
                        } catch (java.io.IOException e) {
                            log("Error writing file: " + e.getMessage());
                            JOptionPane.showMessageDialog(this, "Error saving file: " + e.getMessage(),
                                "Save Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception e) {
                    log("Error getting object: " + e.getMessage());
                    JOptionPane.showMessageDialog(this, "Error getting object from S3: " + e.getMessage(),
                        "S3 Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                log("Please select a file (leaf node) to download.");
            }
        } else {
            log("Please select an object from the tree to download.");
        }
    }

    private String getFullObjectPath(DefaultMutableTreeNode node) {
        java.util.List<String> pathParts = new java.util.ArrayList<>();

        // Traverse up the tree to build the path
        DefaultMutableTreeNode current = node;
        while (current != null && current.getUserObject() instanceof String) {
            pathParts.add(0, (String) current.getUserObject());
            current = (DefaultMutableTreeNode) current.getParent();
        }

        // Skip the root node (bucket name) when building the path for S3
        StringBuilder path = new StringBuilder();
        for (int i = 1; i < pathParts.size(); i++) { // Start from 1 to skip bucket name
            if (i > 1) path.append("/");
            path.append(pathParts.get(i));
        }

        return path.toString();
    }

    private void treeDeleteSelectedObject() {
        TreePath selectedPath = bucketTree.getSelectionPath();
        if (selectedPath != null) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            if (selectedNode.isLeaf() && selectedNode.getUserObject() instanceof String) {
                String fileName = (String) selectedNode.getUserObject();
                String fullPath = getFullObjectPath(selectedNode);

                // Show confirmation dialog
                int result = JOptionPane.showConfirmDialog(
                    this,
                    "Are you sure you want to delete the object '" + fullPath + "'?\nThis action cannot be undone.",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                );

                if (result == JOptionPane.YES_OPTION) {
                    String bucketName = bucketNameField.getText();
                    log("Deleting object '" + fullPath + "' from bucket '" + bucketName + "'");
                    try {
                        s3Service.deleteObject(bucketName, fullPath);
                        // Refresh the tree view after deletion
                        refreshTreeView();
                    } catch (Exception e) {
                        log("Error deleting object: " + e.getMessage());
                        JOptionPane.showMessageDialog(this, "Error deleting object: " + e.getMessage(),
                            "S3 Error", JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    log("Deletion of '" + fullPath + "' was cancelled by the user.");
                }
            } else {
                log("Please select a file (leaf node) to delete.");
            }
        } else {
            log("Please select an object from the tree to delete.");
        }
    }

    private void treeUploadToObjectPath() {
        TreePath selectedPath = bucketTree.getSelectionPath();

        String targetPath = "";
        if (selectedPath != null) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) selectedPath.getLastPathComponent();
            if (selectedNode.getUserObject() instanceof String) {
                if (selectedNode.isLeaf()) {
                    // If a file is selected, use its parent directory
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
                    if (parent != null && parent != treeModel.getRoot()) {
                        targetPath = getFullObjectPath(parent) + "/";
                    }
                } else {
                    // If a directory is selected, use that path
                    targetPath = getFullObjectPath(selectedNode) + "/";
                }
            }
        }

        String bucketName = bucketNameField.getText();
        if (s3Service == null || bucketName == null || bucketName.isEmpty()) {
            log("Please select a profile and enter a bucket name.");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            String filePath = fileChooser.getSelectedFile().getAbsolutePath();
            String fileName = fileChooser.getSelectedFile().getName();
            String key = targetPath + fileName;
            log("Uploading file '" + filePath + "' to bucket '" + bucketName + "' with key '" + key + "'");
            try {
                s3Service.putObject(bucketName, key, new java.io.File(filePath));
                // Refresh the tree view after upload
                refreshTreeView();
            } catch (Exception e) {
                log("Error uploading file: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error uploading file: " + e.getMessage(),
                    "S3 Error", JOptionPane.ERROR_MESSAGE);
            }
        }
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