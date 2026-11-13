//https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/nio/file/Files.html
package com.group22.reportnow.export;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class ConsoleSummary {
     public String summarize(File file) {
        if (!file.exists()) {
            return "No console output saved.";
        }
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            String summary = "Total output lines: " + lines.size();
            for (int i = 0; i < lines.size() && i < 3; i++) {
                summary += "\n" + lines.get(i);
            }
            return summary;

        }catch(IOException e) {
            return "Could not read console output.";
        }
    }
}