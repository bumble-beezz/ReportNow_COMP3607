package com.group22.reportnow.export;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.group22.reportnow.model.SessionSummary;

class LoadPastSessionsTest {
    @TempDir Path tempDir;

    @Test
    void loadsPastSessionsCorrectly() throws Exception {
        File f1 = tempDir.resolve("reportnow-123.json").toFile();
        SessionSummary summary = new SessionSummary("S", "P", "old-session-1", 1000L, 100L, 1, 1, 0, 0, 0, 0, 100L, 0L, 0L, 0.0);
        Map<String, Object> root =Map.of("session_metadata", summary);
        new ObjectMapper().writeValue(f1, root);

        LoadPastSessions loader = new LoadPastSessions();
        List<SessionSummary> result = loader.load(tempDir.toFile(), "current-session");
        
        assertEquals(1, result.size());
        assertEquals("old-session-1", result.get(0).getSessionId());
    }
}