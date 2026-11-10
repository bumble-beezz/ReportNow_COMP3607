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

public class ProjectLifecycleListener implements PackageListener {
    private final SessionData data;
    private final Consumer<String> onClose;
    private final ClassLifecycleListener classListener;
    private final Map<String, String> projects = new HashMap<>();
    private final Set<String> openProjects = new HashSet<>();
    private Timer timer;

    public ProjectLifecycleListener(SessionData data, Consumer<String> onClose) {
        this.data = data;
        this.onClose = onClose;
        this.classListener = new ClassLifecycleListener(data);
    }

    public ClassLifecycleListener getClassListener() {
        return classListener;
    }

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

    public void recordProjectModified(String projectName) { 
        data.recordProject("ProjectModified", projectName);
    }

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

    public synchronized void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

}
