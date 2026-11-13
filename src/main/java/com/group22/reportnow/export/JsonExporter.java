//https://www.baeldung.com/jackson-object-mapper-tutorial
package com.group22.reportnow.export;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.group22.reportnow.model.ClassEvent;
import com.group22.reportnow.model.CompileEvent;
import com.group22.reportnow.model.ProjectEvent;
import com.group22.reportnow.model.RunEvent;
import com.group22.reportnow.model.SessionSummary;
import com.group22.reportnow.model.TelemetryEvent;
import com.group22.reportnow.storage.SessionData;

public class JsonExporter implements ExportStrategy {
    private final String studentId;
    private final String projectName;
    private final ObjectMapper mapper;

    public JsonExporter(String studentId, String projectName) {
        this.studentId = studentId;
        this.projectName = projectName;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    @Override
    public void export(SessionData data, File file) throws ExportException {
        try {
            List<TelemetryEvent> filtered = filterEvents(data);

            Map<String, Object> root = new LinkedHashMap<>();
            root.put("session_metadata", buildMetadata(data, filtered));
            root.put("events", filtered);

            File consoleFile = new File(file.getAbsoluteFile().getParentFile(), "console-output.txt");
            String consoleSummary = new ConsoleSummary().summarize(consoleFile);
            root.put("console_output_summary", consoleSummary);

            List<SessionSummary> past = new ArrayList<>();
            for (SessionSummary s : data.getPastSessions()) {
                if (s.getProjectName().equals(projectName)) {
                    past.add(s);
                }
            }
            if (!past.isEmpty()) {
                root.put("past_sessions", past);
            }

            mapper.writeValue(file, root);
        } catch (Exception e) {
            throw new ExportException("JSON export failed", e);
        }
    }

    private List<TelemetryEvent> filterEvents(SessionData data) {
        List<TelemetryEvent> filtered = new ArrayList<>();
        for (TelemetryEvent e : data.getEvents()) {
            if (e instanceof ProjectEvent) {
                ProjectEvent pe = (ProjectEvent) e;
                if (pe.getProjectName().equals(projectName)) {
                    filtered.add(e);
                }
            } else if (e instanceof ClassEvent) {
                ClassEvent ce = (ClassEvent) e;
                if (ce.getProjectName().equals(projectName)) {
                    filtered.add(e);
                }
            } else if (e instanceof CompileEvent) {
                CompileEvent ce = (CompileEvent) e;
                if (ce.getProjectName().equals(projectName)) {
                    filtered.add(e);
                }
            } else if (e instanceof RunEvent) {
                RunEvent re = (RunEvent) e;
                if (re.getProjectName().equals(projectName)) {
                    filtered.add(e);
                }
            }
        }
        return filtered;
    }

    private Map<String, Object> buildMetadata(SessionData data, List<TelemetryEvent> events) {
        int totalCompiles = 0;
        int successCompiles = 0;
        int totalRuns = 0;
        int successRuns = 0;

        for (TelemetryEvent e : events) {
            if (e instanceof CompileEvent) {
                CompileEvent ce = (CompileEvent) e;
                totalCompiles++;
                if (ce.isSuccess()) {
                    successCompiles++;
                }
            } else if (e instanceof RunEvent) {
                RunEvent re = (RunEvent) e;
                totalRuns++;
                if (re.isSuccess()) {
                    successRuns++;
                }
            }
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("student_id", studentId);
        m.put("project_name", projectName);
        m.put("session_id", data.getSessionId());
        m.put("session_start_ms", data.getStartMs());
        m.put("session_duration_ms", data.getSessionDurationMs());
        m.put("total_compilations", totalCompiles);
        m.put("successful_compilations", successCompiles);
        m.put("failed_compilations", totalCompiles - successCompiles);
        m.put("total_execution_attempts", totalRuns);
        m.put("successful_executions", successRuns);
        m.put("failed_executions", totalRuns - successRuns);
        m.put("active_time_ms", data.getActiveTimeMs());
        m.put("idle_time_ms", data.getIdleTimeMs());
        m.put("typing_cadence_ms", data.getTypingCadence());
        m.put("edits_per_minute", data.getEditsPerMinute());
        return m;
    }
}