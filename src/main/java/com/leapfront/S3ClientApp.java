package com.leapfront;

import javax.swing.*;

public class S3ClientApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            S3ClientUI gui = new S3ClientUI();
            gui.setVisible(true);
        });
    }
}