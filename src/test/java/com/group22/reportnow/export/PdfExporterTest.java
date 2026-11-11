package com.group22.reportnow.export;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.group22.reportnow.model.CompileError;
import com.group22.reportnow.model.SessionSummary;
import com.group22.reportnow.storage.SessionData;

class PdfExporterTest {

    @TempDir
    Path tempDir;

    @Test
    void producesValidPdf() throws Exception {
        SessionData data = new SessionData();
        data.recordCompile("P", true, 100, new ArrayList<>());
        data.recordCompile("P", false, 200,
                List.of(new CompileError("Foo.java", 7, "ERROR", "boom")));
        data.recordRun("P", true, 50, null);
        data.endSession();

        File out = tempDir.resolve("report.pdf").toFile();
        new PdfExporter("S", "P").export(data, out);

        byte[] bytes = Files.readAllBytes(out.toPath());
        assertTrue(bytes.length > 4);
        assertEquals('%', bytes[0]);
        assertEquals('P', bytes[1]);
        assertEquals('D', bytes[2]);
        assertEquals('F', bytes[3]);
    }

    @Test
    void emptySessionStillWorks() {
        SessionData data = new SessionData();
        data.endSession();
        File out = tempDir.resolve("empty.pdf").toFile();
        assertDoesNotThrow(() -> new PdfExporter("S", "P").export(data, out));
        assertTrue(out.length() > 0);
    }

    @Test
    void withPastSessionsStillWorks() {
        SessionData data = new SessionData();
        data.recordCompile("P", true, 100, new ArrayList<>());
        data.endSession();
        List<SessionSummary> past = new ArrayList<>();
        past.add(new SessionSummary("S", "P", "old",
                1000L, 500L, 2, 1, 1, 0, 300L, 200L));
        data.setPastSessions(past);

        File out = tempDir.resolve("past.pdf").toFile();
        assertDoesNotThrow(() -> new PdfExporter("S", "P").export(data, out));
        assertTrue(out.length() > 0);
    }
}