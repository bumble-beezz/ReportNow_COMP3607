//https://bluej.org/doc/extensionsAPI2/bluej/extensions2/BlueJ.html
//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/BClass.html
//https://www.baeldung.com/java-threadlocal
//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/event/PackageListener.html
//https://www.baeldung.com/java-concurrent-map
package com.group22.reportnow.listener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.BProject;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;

/**
 * Tracks activities involving BlueJ projects.
 * Records when projects are created, opened, modified or deleted.
 */
public class ProjectLifecycleListener implements PackageListener {
    /** Stores the session data used to record project events. */
    private final SessionData data;
    /** Stores the action to perform when a project is closed. */
    private final Consumer<String> onClose;
    /** Tracks class activities within the opened projects. */
    private final ClassLifecycleListener classListener;
    /** Stores the paths and names of tracked projects. */
    private final Map<String, String> projects = new HashMap<>();
    /** Keeps track of projects that are currently open. */
    private final Set<String> openProjects = new HashSet<>();
    /** Runs regular checks for project and class changes. */
    private Timer timer;


    /**
     * Sets up the project lifecycle listener.
     * @param data the session data used to store events
     * @param onClose the action to perform when a project closes
    */
    public ProjectLifecycleListener(SessionData data, Consumer<String> onClose) {
        this.data = data;
        this.onClose = onClose;
        this.classListener = new ClassLifecycleListener(data);
    }

    /**
     * Returns the class lifecycle listener used for tracking classes.
     * @return the class lifecycle listener
    */
    public ClassLifecycleListener getClassListener() {
        return classListener;
    }


    /**
     * Records when a BlueJ project package is opened and starts tracking the classes in the project.
     *  @param event the package opening event from BlueJ
    */
    @Override
    public synchronized void packageOpened(PackageEvent event) {
        try {
            BProject project = event.getPackage().getProject();
            String name = project.getName();
            File folder = project.getDir();
            String path = folder.getAbsolutePath();

            if (!projects.containsKey(path)) {
                projects.put(path, name);

                if (isNewProject(folder)) {
                    data.recordProject("ProjectCreated", name);
                }
            }
            if (openProjects.add(path)) {
                data.recordProject("ProjectOpened", name);
            }
            classListener.trackProject(folder, name);

        }catch(Exception e) {
            System.out.println("Could not record project open: " + e.getMessage());
        }
    }


    /**
     * Handles a package closing event and updates the list of open projects.
     * @param event the package closing event from BlueJ
    */
    @Override
    public synchronized void packageClosing(PackageEvent event) {
        try {
            BProject project = event.getPackage().getProject();
            String name = project.getName();
            openProjects.remove(project.getDir().getAbsolutePath());

            if (onClose != null) {
                onClose.accept(name);
            }

        }catch(Exception e) {
            System.out.println("Could not record project close: " + e.getMessage());
        }
    }

    /**
     * Checks whether a project folder was recently created.
     * @param folder the project folder being checked
     * @return true if the folder was created recently, otherwise false
    */
    private boolean isNewProject(File folder) {
        try {
            BasicFileAttributes info = Files.readAttributes(folder.toPath(), BasicFileAttributes.class);
            long created = info.creationTime().toMillis();
            long now = System.currentTimeMillis();

            return created <= now && now - created < 10000L;

        }catch(IOException e) {
            return false;
        }
    }

    /**
     * Records that a project has been modified.
     * @param projectName the name of the modified project
    */
    public void recordProjectModified(String projectName) { 
        data.recordProject("ProjectModified", projectName);
    }

    /**
     * Checks whether tracked project folders still exist.
     * Records a deletion event when a folder is missing.
    */
    public synchronized void checkProjects() {
        for (String path : new ArrayList<>(projects.keySet())) {
            File folder = new File(path);

            if (!folder.exists()) {
                String name = projects.remove(path);
                openProjects.remove(path);

                if (name != null) {
                    data.recordProject("ProjectDeleted", name);
                }
            }
        }
    }

    /**
     * Starts a timer that checks for project and class changes every second.
    */
    public synchronized void startTimer() {
        if (timer != null) {
            return;
        }
        
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                try {
                    checkProjects();
                    classListener.checkFiles();
                }catch(Exception e) {
                    System.out.println("Error checking projects: " + e.getMessage());
                }
            }
        };

        timer = new Timer("Lifecycle Timer", true);
        timer.scheduleAtFixedRate(task, 1000L, 1000L);
    }

    /**
     * Stops the timer used for checking project and class changes.
    */
    public synchronized void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
