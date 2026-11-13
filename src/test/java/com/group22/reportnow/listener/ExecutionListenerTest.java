package com.group22.reportnow.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import com.group22.reportnow.storage.SessionData;
import bluej.extensions2.event.InvocationFinishedEvent;

public class ExecutionListenerTest {

    private SessionData data;
    private ExecutionListener recorder;

    @BeforeEach
    void setUp() {
        data = new SessionData();
        recorder = new ExecutionListener(data);
    }

    private InvocationFinishedEvent event(InvocationFinishedEvent.EventType type) {
        InvocationFinishedEvent e= mock(InvocationFinishedEvent.class);
        when(e.getEventType()).thenReturn(type);
        return e;
    }

    @Test
    void normalExitIsRecordedAsSuccess() {
        recorder.invocationFinished(event(InvocationFinishedEvent.EventType.NORMAL_EXIT));

        assertEquals(1, data.getRunEvents().size());
        assertTrue(data.getRunEvents().get(0).isSuccess());
        assertNull(data.getRunEvents().get(0).getMessage());
        assertEquals(0L, data.getRunEvents().get(0).getDurationMs());
    }

    @Test
    void exceptionExitIsRecordedAsFailure() {
        recorder.invocationFinished(event(InvocationFinishedEvent.EventType.EXCEPTION_EXIT));

        assertEquals(1, data.getRunEvents().size());
        assertFalse(data.getRunEvents().get(0).isSuccess());
        assertEquals("Runtime exception", data.getRunEvents().get(0).getMessage());
    }

    @Test
    void terminatedExitIsRecordedAsFailure() {
        recorder.invocationFinished(event(InvocationFinishedEvent.EventType.TERMINATED_EXIT));

        assertEquals(1, data.getRunEvents().size());
        assertFalse(data.getRunEvents().get(0).isSuccess());
        assertEquals("TERMINATED_EXIT", data.getRunEvents().get(0).getMessage());
    }

    @Test
    void unknownExitIsRecordedAsFailure() {
        recorder.invocationFinished(event(InvocationFinishedEvent.EventType.UNKNOWN_EXIT));

        assertEquals(1, data.getRunEvents().size());
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