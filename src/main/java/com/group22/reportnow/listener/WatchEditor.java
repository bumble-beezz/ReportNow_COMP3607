package com.group22.reportnow.listener;

import java.util.IdentityHashMap;
import java.util.Map;

import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.BClass;
import bluej.extensions2.BPackage;
import bluej.extensions2.BlueJ;
import bluej.extensions2.BProject;
import bluej.extensions2.PackageNotFoundException;
import bluej.extensions2.ProjectNotOpenException;
import bluej.extensions2.editor.JavaEditor;
import bluej.extensions2.event.PackageEvent;
import bluej.extensions2.event.PackageListener;

public class WatchEditor implements PackageListener {
    private final BlueJ bluej;
    private final SessionData data;
    private final Map<JavaEditor, WatchDocument> attached = new IdentityHashMap<>();

    public WatchEditor(BlueJ bluej, SessionData data) {
        this.bluej = bluej;
        this.data = data;
    }

    @Override
    public void packageOpened(PackageEvent event) {
        scanOpenEditors();
    }

    @Override
    public void packageClosing(PackageEvent event) {
    }

    public void scanOpenEditors() {
        for (BProject project : bluej.getOpenProjects()) {
            try {
                for (BPackage pkg : project.getPackages()) {
                    for (BClass cls : pkg.getClasses()) {
                        JavaEditor editor = cls.getJavaEditorIfOpen();
                        if (editor != null && !attached.containsKey(editor)) {
                            attached.put(editor, new WatchDocument(editor, data));
                        }
                    }
                }
            } catch (ProjectNotOpenException | PackageNotFoundException e) {
                System.err.println("Could not scan editors: " + e.getMessage());
            }
        }
    }

    public void detachAll() {
        for (WatchDocument watcher : attached.values()) {
            watcher.detach();
        }
        attached.clear();
    }
}