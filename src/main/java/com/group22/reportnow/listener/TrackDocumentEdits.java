//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/editor/JavaEditor.html
package com.group22.reportnow.listener;

import java.time.Instant;

import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.editor.DocumentListener;
import bluej.extensions2.editor.JavaEditor;

public class TrackDocumentEdits implements DocumentListener {
    private final JavaEditor editor;
    private final SessionData data;

    public TrackDocumentEdits(JavaEditor editor, SessionData data) {
        this.editor = editor;
        this.data = data;
        editor.addDocumentListener(this);
    }

    @Override
    public void textReplaced(int offset, String oldText, String newText,int newCaretPos, int newSelectionLength) {
        data.recordEdit(Instant.now());
    }

    public void detach() {
        editor.removeDocumentListener(this);
    }
}