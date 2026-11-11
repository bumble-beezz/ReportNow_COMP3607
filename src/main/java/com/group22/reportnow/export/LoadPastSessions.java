package com.group22.reportnow.export;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.group22.reportnow.model.SessionSummary;

public class LoadPastSessions {

    public List<SessionSummary> load(File baseDir, String currentSessionId) {
        List<SessionSummary> history = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        if (!baseDir.exists() || !baseDir.isDirectory()) {
            return history;
        }

        ObjectMapper mapper = new ObjectMapper();
        File[] files = baseDir.listFiles();
        if (files == null) {
            return history;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                history.addAll(load(file, currentSessionId));
                continue;
            }
            String name = file.getName();
            if (!name.startsWith("reportnow-") || !name.endsWith(".json")) {
                continue;
            }
            try {
                JsonNode root = mapper.readTree(file);
                JsonNode meta = root.get("session_metadata");
                if (meta == null) {
                    continue;
                }
                SessionSummary summary = mapper.treeToValue(meta, SessionSummary.class);
                String id = summary.getSessionId();
                if (id == null) {
                    continue;
                }
                if (id.equals(currentSessionId)) {
                    continue;
                }
                if (seen.contains(id)) {
                    continue;
                }
                history.add(summary);
                seen.add(id);
            } catch (Exception e) {
                System.err.println("Could not read " + name + ": " + e.getMessage());
            }
        }

        history.sort((a, b) -> Long.compare(b.getSessionStartMs(), a.getSessionStartMs()));
        return history;
    }
}