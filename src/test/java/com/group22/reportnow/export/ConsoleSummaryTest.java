// Student IDs: 816044172

package com.group22.reportnow.export;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ConsoleSummaryTest {
    @TempDir Path tempDir;
    private final ConsoleSummary summary = new ConsoleSummary();

    @Test
    void summarizesFileCorrectly() throws Exception {
        File f = tempDir.resolve("console.txt").toFile();
        StringBuilder content = new StringBuilder();
        for (int i = 1; i <= 20; i++) {
            content.append("line ").append(i).append("\n");
        }
        Files.writeString(f.toPath(), content.toString());
        String result= summary.summarize(f);
        assertTrue(result.contains("Total output lines: 20"));
        assertTrue(result.contains("line 15"));
        assertTrue(result.contains("... (5 more lines)"));
    }

    @Test
    void handlesMissingFile() {
        assertEquals("No console output saved.", summary.summarize(new File("missing.txt")));
    }
}