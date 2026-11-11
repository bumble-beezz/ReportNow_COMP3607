package com.group22.reportnow.export;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group22.reportnow.model.CompileError;
import com.group22.reportnow.model.SessionSummary;
import com.group22.reportnow.storage.SessionData;

class JsonExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void writesMetadataAndEvents() throws Exception {
        SessionData data = new SessionData();
        data.recordCompile("P", true, 100, new ArrayList<>());
        data.recordCompile("P", false, 200,
                List.of(new CompileError("Foo.java", 7, "ERROR", "boom")));
        data.recordRun("P", true, 40, null);
        data.endSession();

        File out = tempDir.resolve("out.json").toFile();
        new JsonExporter("STUDENT", "P").export(data, out);

        assertTrue(out.exists());
        assertTrue(out.length() > 0);

        JsonNode root = new ObjectMapper().readTree(out);
        JsonNode meta = root.get("session_metadata");
        assertNotNull(meta);
        assertEquals("STUDENT", meta.get("student_id").asText());
        assertEquals("P", meta.get("project_name").asText());
        assertEquals(data.getSessionId(), meta.get("session_id").asText());
        assertEquals(2, meta.get("total_compilations").asInt());
        assertEquals(1, meta.get("successful_compilations").asInt());
        assertEquals(1, meta.get("failed_compilations").asInt());
        assertEquals(1, meta.get("total_execution_attempts").asInt());

        // Fields added by the newer SessionData API
        assertTrue(meta.has("typing_cadence_ms"));
        assertTrue(meta.has("edits_per_minute"));
        assertEquals(0L, meta.get("typing_cadence_ms").asLong());

        JsonNode events = root.get("events");
        assertNotNull(events);
        assertEquals(3, events.size());
    }

    @Test
    void errorDetailsAreSerialised() throws Exception {
        SessionData data = new SessionData();
        data.recordCompile("P", false, 10,
                List.of(new CompileError("A.java", 5, "ERROR", "msg")));

        File out = tempDir.resolve("errors.json").toFile();
        new JsonExporter("S", "P").export(data, out);

        JsonNode compile = new ObjectMapper().readTree(out).get("events").get(0);
        assertEquals("COMPILATION", compile.get("event_type").asText());
        assertTrue(!compile.get("success").asBoolean());
        JsonNode err = compile.get("errors").get(0);
        assertEquals("A.java", err.get("file_name").asText());
        assertEquals(5, err.get("line_number").asInt());
        assertEquals("ERROR", err.get("error_code").asText());
    }

    @Test
    void runEventMessageIsSerialised() throws Exception {
        SessionData data = new SessionData();
        data.recordRun("P", false, 150, "NullPointerException");

        File out = tempDir.resolve("run.json").toFile();
        new JsonExporter("S", "P").export(data, out);

        JsonNode run = new ObjectMapper().readTree(out).get("events").get(0);
        assertEquals("EXECUTION", run.get("event_type").asText());
        assertTrue(!run.get("success").asBoolean());
        assertEquals("NullPointerException", run.get("message").asText());
    }

    @Test
    void typingCadenceIsReflectedInMetadata() throws Exception {
        SessionData data = new SessionData();
        long base = System.currentTimeMillis();
        data.recordEdit(Instant.ofEpochMilli(base));
        data.recordEdit(Instant.ofEpochMilli(base + 1_000));
        data.recordEdit(Instant.ofEpochMilli(base + 3_000));
        data.endSession();

        File out = tempDir.resolve("cadence.json").toFile();
        new JsonExporter("S", "P").export(data, out);

        JsonNode meta = new ObjectMapper().readTree(out).get("session_metadata");
        assertEquals(1_500L, meta.get("typing_cadence_ms").asLong());
        assertTrue(meta.get("edits_per_minute").isNumber());
    }

    @Test
    void pastSessionsAreIncludedWhenPresent() throws Exception {
        SessionData data = new SessionData();
        data.endSession();
        List<SessionSummary> past = new ArrayList<>();
        past.add(new SessionSummary("STUDENT", "P", "old-session",
                1000L, 500L, 1, 1, 0, 1, 300L, 200L));
        past.add(new SessionSummary("STUDENT", "OtherProject", "other-session",
                2000L, 500L, 1, 1, 0, 1, 300L, 200L));
        data.setPastSessions(past);

        File out = tempDir.resolve("past.json").toFile();
        new JsonExporter("STUDENT", "P").export(data, out);

        JsonNode root = new ObjectMapper().readTree(out);
        JsonNode sessions = root.get("past_sessions");
        assertNotNull(sessions);
        assertEquals(1, sessions.size());
        assertEquals("old-session", sessions.get(0).get("session_id").asText());
    }

    @Test
    void emptySessionStillWritesFile() throws Exception {
        SessionData data = new SessionData();
        data.endSession();
        File out = tempDir.resolve("empty.json").toFile();
        new JsonExporter("S", "P").export(data, out);
        assertTrue(out.length() > 0);
    }
}