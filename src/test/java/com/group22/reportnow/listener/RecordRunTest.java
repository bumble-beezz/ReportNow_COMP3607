package com.group22.reportnow.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.event.InvocationFinishedEvent;

public class RecordRunTest {
    private SessionData data;
    private RecordRun recorder;

    @BeforeEach
    void setUp() {
        data = new SessionData();
        recorder = new RecordRun(data);
    }

    private InvocationFinishedEvent event(InvocationFinishedEvent.EventType type) {
        InvocationFinishedEvent e = mock(InvocationFinishedEvent.class);
        when(e.getEventType()).thenReturn(type);
        return e;
    }

    @Test
    void normalExitIsSuccess() {
        recorder.invocationFinished(event(InvocationFinishedEvent.EventType.NORMAL_EXIT));

        assertEquals(1, data.getRunEvents().size());
        assertTrue(data.getRunEvents().get(0).isSuccess());
        assertEquals(null, data.getRunEvents().get(0).getMessage());
    }

    @Test
    void exceptionExitIsFailure() {
        recorder.invocationFinished(event(InvocationFinishedEvent.EventType.EXCEPTION_EXIT));

        assertEquals(1, data.getRunEvents().size());
        assertFalse(data.getRunEvents().get(0).isSuccess());
        assertEquals("Runtime exception", data.getRunEvents().get(0).getMessage());
    }

    @Test
    void terminatedExitIsFailure() {
        recorder.invocationFinished(event(InvocationFinishedEvent.EventType.TERMINATED_EXIT));

        assertFalse(data.getRunEvents().get(0).isSuccess());
        assertEquals("TERMINATED_EXIT", data.getRunEvents().get(0).getMessage());
    }

    @Test
    void unknownExitIsFailure() {
        recorder.invocationFinished(event(InvocationFinishedEvent.EventType.UNKNOWN_EXIT));

        assertFalse(data.getRunEvents().get(0).isSuccess());
        assertEquals("UNKNOWN_EXIT", data.getRunEvents().get(0).getMessage());
    }

    @Test
    void multipleRunsAccumulate() {
        recorder.invocationFinished(event(InvocationFinishedEvent.EventType.NORMAL_EXIT));
        recorder.invocationFinished(event(InvocationFinishedEvent.EventType.EXCEPTION_EXIT));

        assertEquals(2, data.getRunEvents().size());
        assertEquals(1, data.getSuccessfulRunCount());
        assertEquals(1, data.getFailedRunCount());
    }
}