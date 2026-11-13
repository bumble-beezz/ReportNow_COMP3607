package com.group22.reportnow.storage;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.group22.reportnow.model.CompileError;

public class SessionDataTest {

    private SessionData data;

    @BeforeEach
    void setUp() {
        data = new SessionData();
    }

    @Test
    void startsEmptyWithUuid() {
        assertNotNull(data.getSessionId());
        assertFalse(data.getSessionId().isBlank());
        assertEquals(0, data.getCompileEvents().size());
        assertEquals(0, data.getRunEvents().size());
    }

    @Test
    void recordsCompileEvents() {
        data.recordCompile("P", true, 100, new ArrayList<>());
        data.recordCompile("P", false, 200, List.of(new CompileError("A.java", 1, "ERROR", "boom")));
        assertEquals(2, data.getCompileEvents().size());
        assertEquals(1, data.getSuccessfulCompileCount());
        assertEquals(1, data.getFailedCompileCount());
    }

    @Test
    void recordsRunEvents() {
        data.recordRun("P", true, 50, null);
        data.recordRun("P", false, 60, "NPE");
        assertEquals(2, data.getRunEvents().size());
        assertEquals(1, data.getSuccessfulRunCount());
        assertEquals(1, data.getFailedRunCount());
    }

    @Test
    void activeTimeSumsDurations() {
        data.recordCompile("P", true, 100, new ArrayList<>());
        data.recordRun("P", true, 50, null);
        assertEquals(150, data.getActiveTimeMs());
    }

    @Test
    void idleTimeIsClampedToZero() {
        data.recordCompile("P", true, 10_000, new ArrayList<>());
        data.endSession();
        assertEquals(0, data.getIdleTimeMs());
    }

    @Test
    void editTimestampsDriveActiveTime() {
        long base = System.currentTimeMillis();
        data.recordEdit(Instant.ofEpochMilli(base));
        data.recordEdit(Instant.ofEpochMilli(base + 1_000));
        data.recordEdit(Instant.ofEpochMilli(base + 3_000));
        assertEquals(3_000, data.getActiveTimeFromEditsMs());
    }

    @Test
    void currentProjectNameFollowsOpenedProject() {
        data.recordProject("ProjectOpened", "Alpha");
        assertEquals("Alpha", data.getCurrentProjectName());
        data.recordProject("ProjectOpened", "Beta");
        assertEquals("Beta", data.getCurrentProjectName());
    }

    @Test
    void defaultProjectNameWhenNoProjectEvents() {
        assertEquals("UnknownProject", data.getCurrentProjectName());
    }

    @Test
    void typingCadenceIsZeroWithNoEdits() {
        assertEquals(0L, data.getTypingCadence());
    }

    @Test
    void typingCadenceIsZeroWithOneEdit() {
        data.recordEdit(Instant.now());
        assertEquals(0L, data.getTypingCadence());
    }

    @Test
    void typingCadenceAveragesFastGaps() {
        long base = System.currentTimeMillis();
        data.recordEdit(Instant.ofEpochMilli(base));
        data.recordEdit(Instant.ofEpochMilli(base + 1_000));
        data.recordEdit(Instant.ofEpochMilli(base + 3_000));
        assertEquals(1_500L, data.getTypingCadence());
    }

    @Test
    void typingCadenceExcludesSlowGaps() {
        long base = System.currentTimeMillis();
        data.recordEdit(Instant.ofEpochMilli(base));
        data.recordEdit(Instant.ofEpochMilli(base + 200_000));
        data.recordEdit(Instant.ofEpochMilli(base + 201_000)); 
        assertEquals(1_000L, data.getTypingCadence());
    }

    @Test
    void editsPerMinuteIsZeroWithNoEdits() {
        data.endSession();
        assertEquals(0.0, data.getEditsPerMinute(), 0.001);
    }

    @Test
    void editsPerMinuteIsPositiveWithEditsAndDuration() throws InterruptedException {
        data.recordEdit(Instant.now());
        Thread.sleep(40);
        data.endSession();
        assertTrue(data.getEditsPerMinute() > 0);
    }

    @Test
    void editsPerMinuteIsZeroWhenSessionHasNoDuration() {
        SessionData fresh = new SessionData();
        fresh.endSession();
        assertEquals(0.0, fresh.getEditsPerMinute(), 0.001);
    }
}