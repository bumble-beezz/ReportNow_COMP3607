package com.group22.reportnow.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

/**
 * StoreStudentId
 * Handles the student Id prompt used in ReportNow when a user opens BlueJ
 */

public class StoreStudentId {
    /** Stores the student Id in the configuration properties */
    private Properties props = new Properties();
    /** Stores the location of the configuration file */
    private File file = new File(System.getProperty("user.home") + "/.reportnow/config.properties");

    /** Loads the saved student ID when the class is created */
    public StoreStudentId() {
        load();
    }

    /** Reads the saved properties from the configuration file */
    private void load() {
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                System.out.println("Could not load student ID");
            }
        }
    }

    /** Saves the student ID to the configuration file */
    private void save() {
        file.getParentFile().mkdirs();

        try (FileOutputStream out = new FileOutputStream(file)) {
            props.store(out, "ReportNow");
        } catch (IOException e) {
            System.out.println("Could not save student ID");
        }
    }

    /**
     * Gets the saved student ID or asks the student to enter one
     * @return the student ID, or UNKNOWN_STUDENT if no ID is entered
    */
    public String getStudentId() {
        String id = props.getProperty("student_id");

        if (id == null || id.isBlank()) {
            id = askForId();

            if (id == null || id.isBlank()) {
                return "UNKNOWN_STUDENT";
            }
            id = id.trim();
            props.setProperty("student_id", id);
            save();
        }
        return id;
    }

    /**
     * Shows a pop-up window asking the student for their ID.
     * @return the entered ID, or null if the window is cancelled
    */
    private String askForId() {
        URL url = getClass().getResource("/reportnow_icon.png");
        ImageIcon icon = null;

        if (url != null) {
            icon = new ImageIcon(url);
        }
        return (String) JOptionPane.showInputDialog(null,
            "Welcome to ReportNow!\nPlease enter your Student ID:",
            "ReportNow Setup", JOptionPane.PLAIN_MESSAGE, icon, null, null);
    }
}

