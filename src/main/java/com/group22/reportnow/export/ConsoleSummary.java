//https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html
package com.group22.reportnow.export;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

public class ConsoleSummary {
     public String summarize(File file) {
        if (!file.exists()) {
            return "No console output saved.";
        }
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (lines.isEmpty()) {
                return "Console output was empty.";
            }
            int limit = Math.min(lines.size(), 15);
            StringBuilder sb = new StringBuilder();
            sb.append("Total output lines: ").append(lines.size()).append("\n\n");

            for (int i = 0; i < limit; i++) {
                sb.append(lines.get(i)).append("\n");
            }
            if (lines.size() > limit) {
                sb.append("\n... (").append(lines.size() - limit).append(" more lines)");
            }
            return sb.toString();

        }catch(IOException e) {
            return "Could not read console output.";
        }
    }
}