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

public class ClassLifecycleListener implements ClassListener {
    private final SessionData data;
    private final MethodCounter counter = new MethodCounter();
    private final Map<String, Long> classes = new HashMap<>();
    private final Map<String, Integer> methodCounts = new HashMap<>();
    private final Map<String, String> projects = new HashMap<>();

    public ClassLifecycleListener(SessionData data) {
        this.data = data;
    }

    public synchronized void trackProject(File folder, String projectName) {
        String path = folder.getAbsolutePath();
        if (!projects.containsKey(path)) {
            projects.put(path, projectName);
            scan(folder, projectName, false);
        }
    }

    private void recordEvent(String action, String className, String projectName) {
        data.recordClass(action, className, projectName);
        data.recordProject("ProjectModified", projectName);
    }

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

    private String getFileName(String className) {
        int position = className.lastIndexOf(".");
        if (position >= 0) className = className.substring(position + 1);
        return className + ".java";
    }

    @Override
    public void classStateChanged(ClassEvent event) {}

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