//Student IDs: 400017923, 816044172, 816039066

//https://www.baeldung.com/java-start-thread
//https://www.baeldung.com/java-threadlocal
//https://www.baeldung.com/java-concurrent-map
//https://bluej.org/doc/extensionsAPI2/bluej/extensions2/BlueJ.html

package com.group22.reportnow.listener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.group22.reportnow.parser.MethodCounter;
import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.event.ClassEvent;
import bluej.extensions2.event.ClassListener;

/**
 * Tracks changes made to Java classes in BlueJ.
 * Records class creation, modification, deletion and changes in the number of methods.
 */
public class ClassLifecycleListener implements ClassListener {
    /** Stores the session data used to record class events */
    private final SessionData data;
    /** Counts the methods found in Java source files */
    private final MethodCounter counter = new MethodCounter();
    /** Stores the last modified times of tracked Java files */
    private final Map<String, Long> classes = new HashMap<>();
    /** Stores the number of methods previously found in each file */
    private final Map<String, Integer> methodCounts = new HashMap<>();
    /** Stores the paths and names of tracked projects */
    private final Map<String, String> projects = new HashMap<>();

    /**
     * Creates the class listener and connects it to the session data.
     * @param data the session data used to store class events
    */
    public ClassLifecycleListener(SessionData data) {
        this.data = data;
    }

    /**
     * Adds a project to the list of projects being tracked.
     * Then scans its Java files to record their initial state.
     * @param folder the folder containing the project
     * @param projectName the name of the project
    */
    public synchronized void trackProject(File folder, String projectName) {
        String path = folder.getAbsolutePath();
        if (!projects.containsKey(path)) {
            projects.put(path, projectName);
            scan(folder, projectName, false);
        }
    }

    /**
     * Records a class activity and displays the project as modified.
     * @param action the type of class activity
     * @param className the name of the class
     * @param projectName the name of the project
    */
    private void recordEvent(String action, String className, String projectName) {
        data.recordClass(action, className, projectName);
        data.recordProject("ProjectModified", projectName);
    }


    /**
     * Scans Java files to detect class edits and changes in the number of methods.
     * @param folder the folder being scanned
     * @param projectName the name of the project
     * @param recordChanges whether detected changes should be recorded
    */
    private void scan(File folder, String projectName, boolean recordChanges) {
        File[] files= folder.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scan(file, projectName, recordChanges);
            } else if (file.getName().endsWith(".java") && !file.getName().startsWith("__SHELL")) {
                String path = file.getAbsolutePath();
                long modified = file.lastModified();
                Long previous = classes.get(path);
                int methods = counter.countMethods(file);

                if (previous == null) {
                    classes.put(path, modified);
                    methodCounts.put(path, methods);
                    if (recordChanges) recordEvent("ClassCreated", file.getName(), projectName);
                } else if (previous != modified) {
                    classes.put(path, modified);
                    int oldCount = methodCounts.getOrDefault(path, methods);
                    if (methods > oldCount) {
                        recordEvent("MethodAdded", file.getName(), projectName);
                    } else if (methods < oldCount) {
                        recordEvent("MethodRemoved", file.getName(), projectName);
                    }
                    methodCounts.put(path, methods);
                    if (recordChanges && methods == oldCount) {
                        recordEvent("ClassModified", file.getName(), projectName);
                    }
                }
            }
        }
    }


    /**
     * Checks tracked Java files for changes or deletion.
     * Records class activity when changes are detected.
    */
    public synchronized void checkFiles() {
        for (String path : projects.keySet()) {
            File folder = new File(path);
            if (folder.exists()) scan(folder, projects.get(path), true);
        }

        for (String path : new ArrayList<>(classes.keySet())) {
            File file = new File(path);
            if (!file.exists()) {
                for (String projectPath : projects.keySet()) {
                    File projectFolder = new File(projectPath);
                    if (projectFolder.exists() && path.startsWith(projectPath + File.separator)) {
                        Long previous = classes.remove(path);
                        methodCounts.remove(path);
                        if (previous != null) {
                            recordEvent("ClassDeleted", file.getName(), projects.get(projectPath));
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Converts a class name into a Java filename.
     * @param className the name of the class
     * @return the filename ending in .java
    */
    private String getFileName(String className) {
        int position = className.lastIndexOf(".");
        if (position >= 0) className = className.substring(position + 1);
        return className + ".java";
    }


    /**
     * Receives class state change events from BlueJ.
     * No action is currently performed by this method.
     * @param event the class state change event
    */
    @Override
    public void classStateChanged(ClassEvent event) {}

    /**
     * Updates the tracked file information when a class is renamed.
     * @param event the class rename event from BlueJ
    */
    @Override
    public synchronized void classNameChanged(ClassEvent event) {
        try {
            File projectFolder = event.getPackage().getProject().getDir();
            String projectName = projects.get(projectFolder.getAbsolutePath());
            if (projectName == null) return;

            File folder = event.getPackage().getDir();
            File oldFile = new File(folder, getFileName(event.getOldName()));
            File newFile = new File(folder, getFileName(event.getBClass().getName()));

            if (classes.remove(oldFile.getAbsolutePath()) != null) {
                Integer count = methodCounts.remove(oldFile.getAbsolutePath());
                if (newFile.exists()) {
                    classes.put(newFile.getAbsolutePath(), newFile.lastModified());
                    if (count != null) methodCounts.put(newFile.getAbsolutePath(), count);
                }
                recordEvent("ClassModified", newFile.getName(), projectName);
            }
        } catch (Exception e) {
            System.err.println("Could not record class rename: " + e.getMessage());
        }
    }

    /**
     * Removes a deleted class from tracking and records the class deletion event.
     * @param event the class removal event from BlueJ
    */
    @Override
    public synchronized void classRemoved(ClassEvent event) {
        try {
            File projectFolder = event.getPackage().getProject().getDir();
            String projectName = projects.get(projectFolder.getAbsolutePath());
            if (projectName == null) return;

            File folder = event.getPackage().getDir();
            File file = new File(folder, getFileName(event.getBClass().getName()));

            if (classes.remove(file.getAbsolutePath()) != null) {
                methodCounts.remove(file.getAbsolutePath());
                recordEvent("ClassDeleted", file.getName(), projectName);
            }
        } catch (Exception e) {
            System.err.println("Could not record class deletion: " + e.getMessage());
        }
    }
}