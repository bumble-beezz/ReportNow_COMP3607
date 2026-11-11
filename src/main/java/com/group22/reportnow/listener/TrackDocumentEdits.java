//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/editor/JavaEditor.html
//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/BClass.html
//https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/time/Instant.html
package com.group22.reportnow.listener;

import java.time.Instant;

import com.group22.reportnow.storage.SessionData;
import bluej.extensions2.editor.JavaEditor;

public class TrackDocumentEdits {
    private final JavaEditor editor;
    private final SessionData data;
    private int lastLength;

    public TrackDocumentEdits(JavaEditor editor, SessionData data) {
        this.editor = editor;
        this.data = data;
        lastLength = editor.getTextLength();
    }

    public void checkForEdits(){
        int currentLength = editor.getTextLength();
        if(currentLength != lastLength){
            data.recordEdit(Instant.now());
            lastLength = currentLength;
        }
    }
}