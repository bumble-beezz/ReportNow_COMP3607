package com.group22.reportnow.storage;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConsoleOutputCaptureTest {

    private static final PrintStream REAL_OUT = System.out;
    private static final PrintStream REAL_ERR = System.err;

    private ConsoleOutputCapture capture;

    @BeforeEach
    void setUp() {
        capture = new ConsoleOutputCapture();
    }

    @AfterEach
    void tearDown() {
        if (capture != null) capture.stop();
        System.setOut(REAL_OUT);
        System.setErr(REAL_ERR);
    }

    @Test
    void capturesSimpleOutput() {
        capture.start();
        System.out.println("hello world");
        assertTrue(capture.getCapturedOutput().contains("hello world"));
    }

    @Test
    void capturesErrorOutput() {
        capture.start();
        System.err.println("something broke");
        assertTrue(capture.getCapturedOutput().contains("something broke"));
    }

    @Test
    void filtersReportNowLines() {
        capture.start();
        System.out.println("[ReportNow] internal message");
        System.out.println("user output");
        String captured = capture.getCapturedOutput();
        assertFalse(captured.contains("[ReportNow]"));
        assertTrue(captured.contains("user output"));
    }

    @Test
    void filtersWarningLines() {
        capture.start();
        System.out.println("WARNING: dynamic agent loaded");
        System.out.println("actual output");
        String captured = capture.getCapturedOutput();
        assertFalse(captured.contains("WARNING:"));
        assertTrue(captured.contains("actual output"));
    }

    @Test
    void writesToFile(@TempDir Path tempDir) throws Exception {
        capture.start();
        System.out.println("first line");
        System.out.println("second line");

        File out = tempDir.resolve("captured.txt").toFile();
        capture.writeToFile(out);

        String content = Files.readString(out.toPath());
        assertTrue(content.contains("first line"));
        assertTrue(content.contains("second line"));
    }

    @Test
    void startIsIdempotent() {
        capture.start();
        capture.start();
        System.out.println("only once");
        long occurrences = capture.getCapturedOutput().lines().filter(l -> l.equals("only once")).count();
        assertEquals(1, occurrences);
    }

    @Test
    void stopWithoutStartDoesNotThrow() {
        ConsoleOutputCapture fresh = new ConsoleOutputCapture();
        assertDoesNotThrow(fresh::stop);
    }

    @Test
    void captureIsEmptyBeforeStart() {
        assertEquals("", capture.getCapturedOutput());
    }
}