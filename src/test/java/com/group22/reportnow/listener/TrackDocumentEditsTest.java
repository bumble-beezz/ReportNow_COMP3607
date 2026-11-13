package com.group22.reportnow.listener;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.group22.reportnow.storage.SessionData;
import bluej.extensions2.editor.JavaEditor;

public class TrackDocumentEditsTest {

    private JavaEditor editor;
    private SessionData data;
    private TrackDocumentEdits tracker;

    @BeforeEach
    void setUp() {
        editor = mock(JavaEditor.class);
        data = mock(SessionData.class);
        when(editor.getTextLength()).thenReturn(100);
        tracker= new TrackDocumentEdits(editor, data);
    }

    @Test
    void noEditRecordedWhenTextLengthUnchanged() {
        when(editor.getTextLength()).thenReturn(100);
        tracker.checkForEdits();
        verify(data, never()).recordEdit(any(Instant.class));
    }

    @Test
    void editRecordedWhenTextLengthChanges() {
        when(editor.getTextLength()).thenReturn(150);
        tracker.checkForEdits();
        verify(data,times(1)).recordEdit(any(Instant.class));
    }

    @Test
    void onlyChangedStatesAreCountedAsEdits() {
        when(editor.getTextLength()).thenReturn(150);
        tracker.checkForEdits();

        when(editor.getTextLength()).thenReturn(150);
        tracker.checkForEdits();

        when(editor.getTextLength()).thenReturn(200);
        tracker.checkForEdits();

        verify(data, times(2)).recordEdit(any(Instant.class));
    }

    @Test
    void constructorReadsInitialLength() {
        JavaEditor freshEditor = mock(JavaEditor.class);
        when(freshEditor.getTextLength()).thenReturn(42);
        new TrackDocumentEdits(freshEditor, data);
        verify(freshEditor, times(1)).getTextLength();
    }
}