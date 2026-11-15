//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/editor/JavaEditor.html
//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/BClass.html

package com.group22.reportnow.listener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

/**
 * Finds open Java editors in BlueJ and monitors them for changes and uses TrackDocumentEdits to record editing activity.
 */
public class TrackEditor implements PackageListener {
    /** Provides access to the BlueJ environment*/
    private final BlueJ bluej;
    /** Stores the session data used to record edits. */
    private final SessionData data;
    /** Stores the edit trackers for open Java files. */
    private final Map<String, TrackDocumentEdits> attached = new HashMap<>();

    /**
     * Sets up the editor tracker using BlueJ and the session data.
     * @param bluej the BlueJ extension environment
     * @param data the session data used to record edits
    */
    public TrackEditor(BlueJ bluej, SessionData data) {
        this.bluej = bluej;
        this.data = data;
    }

    /**
     * Checks for open Java editors when a package is opened.
     * @param event the package opening event
    */
    @Override
    public void packageOpened(PackageEvent event) {
        scanOpenEditors();
    }

    /**
     * Receives package closing events from BlueJ.
     * No action is currently performed by this method.
     * @param event the package closing event
    */
    @Override
    public void packageClosing(PackageEvent event) {
    }

    /**
     * Finds open Java editors and attaches edit trackers.
     * Checks the attached editors for changes and removes trackers for files that are no longer open.
    */
    public void scanOpenEditors() {
        Set<String> currentlyOpen = new HashSet<>();

        for (BProject project : bluej.getOpenProjects()) {
            try {
                for (BPackage pkg : project.getPackages()) {
                    for (BClass cls : pkg.getClasses()) {
                        JavaEditor editor = cls.getJavaEditorIfOpen();

                        if (editor != null && cls.getJavaFile() != null) {
                            String path = cls.getJavaFile().getAbsolutePath();
                            currentlyOpen.add(path);

                            if (!attached.containsKey(path)) {
                                attached.put(path, new TrackDocumentEdits(editor, data));
                            }
                        }
                    }
                }
            } catch (ProjectNotOpenException | PackageNotFoundException e) {
                System.out.println("Could not scan editors: " + e.getMessage());
            }
        }

        attached.keySet().removeIf(path -> !currentlyOpen.contains(path));

        for (TrackDocumentEdits watcher : attached.values()) {
            watcher.checkForEdits();
        }
    }

    /** Removes all attached editor trackers.*/
    public void detachAll() {
        attached.clear();
    }
}
