package com.group22.reportnow.listener;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.group22.reportnow.model.CompileEvent;
import com.group22.reportnow.storage.SessionData;

public class RecordCompileTest {
    private SessionData data;
    private RecordCompile recorder;
    private File realFile;

    @BeforeEach
    void setUp() {
        data = new SessionData();
        recorder = new RecordCompile(data);
        realFile = new File("Foo.java");
    }

    private bluej.extensions2.event.CompileEvent mockEvent(String message, int line) {
        bluej.extensions2.event.CompileEvent e = mock(bluej.extensions2.event.CompileEvent.class);
        when(e.getFiles()).thenReturn(new File[]{realFile});
        when(e.getErrorMessage()).thenReturn(message);
        when(e.getErrorLineNumber()).thenReturn(line);
        return e;
    }

    @Test
    void successfulCompileIsRecorded() {
        recorder.compileStarted(mock(bluej.extensions2.event.CompileEvent.class));
        recorder.compileSucceeded(mock(bluej.extensions2.event.CompileEvent.class));

        assertEquals(1, data.getCompileEvents().size());
        CompileEvent ce = data.getCompileEvents().get(0);
        assertTrue(ce.isSuccess());
        assertTrue(ce.getErrors().isEmpty());
    }

    @Test
    void failedCompileWithOneError() {
        recorder.compileStarted(mock(bluej.extensions2.event.CompileEvent.class));
        recorder.compileError(mockEvent("cannot find symbol", 15));
        recorder.compileFailed(mock(bluej.extensions2.event.CompileEvent.class));

        CompileEvent ce = data.getCompileEvents().get(0);
        assertFalse(ce.isSuccess());
        assertEquals(1, ce.getErrors().size());
        assertEquals("Foo.java", ce.getErrors().get(0).getFileName());
        assertEquals(15, ce.getErrors().get(0).getLineNumber());
        assertEquals("ERROR", ce.getErrors().get(0).getErrorCode());
        assertEquals("cannot find symbol", ce.getErrors().get(0).getMessage());
    }

    @Test
    void multipleErrorsAreAllRecorded() {
        recorder.compileStarted(mock(bluej.extensions2.event.CompileEvent.class));
        recorder.compileError(mockEvent("err one", 1));
        recorder.compileError(mockEvent("err two", 2));
        recorder.compileError(mockEvent("err three", 3));
        recorder.compileFailed(mock(bluej.extensions2.event.CompileEvent.class));

        CompileEvent ce = data.getCompileEvents().get(0);
        assertEquals(3, ce.getErrors().size());
    }

    @Test
    void parsedErrorLinesAreCaptured() {
        recorder.compileStarted(mock(bluej.extensions2.event.CompileEvent.class));
        recorder.compileError(mockEvent("Foo.java:42: error: real problem", 42));
        recorder.compileFailed(mock(bluej.extensions2.event.CompileEvent.class));

        CompileEvent ce = data.getCompileEvents().get(0);
        assertEquals("real problem", ce.getErrors().get(0).getMessage());
        assertEquals(42, ce.getErrors().get(0).getLineNumber());
    }

    @Test
    void warningsAreRecordedAsWarningSeverity() {
        recorder.compileStarted(mock(bluej.extensions2.event.CompileEvent.class));
        recorder.compileWarning(mockEvent("unused variable", 10));
        recorder.compileFailed(mock(bluej.extensions2.event.CompileEvent.class));

        CompileEvent ce = data.getCompileEvents().get(0);
        assertEquals("WARNING", ce.getErrors().get(0).getErrorCode());
    }

    @Test
    void errorsAreClearedBetweenRuns() {
        recorder.compileStarted(mock(bluej.extensions2.event.CompileEvent.class));
        recorder.compileError(mockEvent("boom", 1));
        recorder.compileFailed(mock(bluej.extensions2.event.CompileEvent.class));

        recorder.compileStarted(mock(bluej.extensions2.event.CompileEvent.class));
        recorder.compileSucceeded(mock(bluej.extensions2.event.CompileEvent.class));

        assertEquals(1, data.getCompileEvents().get(0).getErrors().size());
        assertEquals(0, data.getCompileEvents().get(1).getErrors().size());
    }

    @Test
    void handlesEmptyFileArrayGracefully() {
        bluej.extensions2.event.CompileEvent e =
                mock(bluej.extensions2.event.CompileEvent.class);
        when(e.getFiles()).thenReturn(new File[0]);
        when(e.getErrorMessage()).thenReturn("generic error");
        when(e.getErrorLineNumber()).thenReturn(0);

        recorder.compileStarted(mock(bluej.extensions2.event.CompileEvent.class));
        recorder.compileError(e);
        recorder.compileFailed(mock(bluej.extensions2.event.CompileEvent.class));

        CompileEvent ce = data.getCompileEvents().get(0);
        assertEquals(1, ce.getErrors().size());
        assertEquals("unknown", ce.getErrors().get(0).getFileName());
    }
}