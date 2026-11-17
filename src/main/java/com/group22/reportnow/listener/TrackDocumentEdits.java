//Student IDs: 400017923
//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/editor/JavaEditor.html
//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/BClass.html
//https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Instant.html
package com.group22.reportnow.listener;

import java.time.Instant;

import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.editor.JavaEditor;

/**
 * Monitors changes in the length of text in a BlueJ editor.
 * Records an edit when the number of characters changes.
 */
public class TrackDocumentEdits {
    /** Stores the Java editor being monitored. */
    private final JavaEditor editor;
    /** Stores the session data used to record editing activity. */
    private final SessionData data;
    /** Stores the previous number of characters in the editor */
    private int lastLength;

    /**
     * Creates an edit tracker for a BlueJ Java editor and saves the initial text length for comparison.
     * @param editor the Java editor being monitored
     * @param data the session data used to record edits
    */
    public TrackDocumentEdits(JavaEditor editor, SessionData data) {
        this.editor = editor;
        this.data = data;
        lastLength = editor.getTextLength();
    }


    /**
     * Checks whether the length of the editor text has changed.
     * Records the current time when a change is detected.
    */
    public void checkForEdits(){
        int currentLength = editor.getTextLength();
        if(currentLength != lastLength){
            data.recordEdit(Instant.now());
            lastLength = currentLength;
        }
    }
}