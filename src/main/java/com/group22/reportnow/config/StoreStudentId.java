package com.group22.reportnow.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class StoreStudentId {
    private Properties props = new Properties();
    private File file = new File(System.getProperty("user.home") + "/.reportnow/config.properties");

    public StoreStudentId() {
        load();
    }

    private void load() {
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                System.out.println("Could not load student ID");
            }
        }
    }

    private void save() {
        file.getParentFile().mkdirs();

        try (FileOutputStream out = new FileOutputStream(file)) {
            props.store(out, "ReportNow");
        } catch (IOException e) {
            System.out.println("Could not save student ID");
        }
    }

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

