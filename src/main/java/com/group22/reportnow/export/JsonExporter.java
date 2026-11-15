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
import com.group22.reportnow.model.CompileError;
import com.group22.reportnow.model.CompileEvent;
import com.group22.reportnow.model.RunEvent;
import com.group22.reportnow.model.SessionSummary;
import com.group22.reportnow.storage.SessionData;

/**
 * Creates JSON reports using the telemetry data collected by ReportNow
 * It uses Jackson's ObjectMapper to save the information.
 */
public class JsonExporter implements ExportStrategy {
    /** Stores the student ID used in the report */
    private final String studentId;
    /** Stores the name of the project being reported */
    private final String projectName;
    /** Converts the collected information into JSON format */
    private final ObjectMapper mapper;

    /**
     * Creates a JSON exporter for the given student and project.
     * @param studentId the student's ID
     * @param projectName the name of the BlueJ project
    */
    public JsonExporter(String studentId, String projectName) {
        this.studentId = studentId;
        this.projectName = projectName;
        this.mapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }


    /**
     * Creates a JSON report containing the recorded session information
     * @param data the data collected during the session
     * @param file the file where the JSON report will be saved
     * @throws ExportException if the report cannot be generated
    */
    @Override
    public void export(SessionData data, File file) throws ExportException {
        try {
            Map<String, Object> root = new LinkedHashMap<>();
            root.put("session_metadata", buildMetadata(data));
            root.put("active_vs_idle_time", buildActiveIdle(data));
            root.put("compilation_error_frequencies", buildErrorFrequencies(data));
            root.put("compilation_summary", buildCompilationSummary(data));
            root.put("class_progression", buildClassProgression(data));
            root.put("execution_summary", buildExecutionSummary(data));

            File consoleFile = new File(file.getAbsoluteFile().getParentFile(), "console-output.txt");
            root.put("console_output_summary", new ConsoleSummary().summarize(consoleFile));

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


    /**
     * Organizes the session details and activity totals
     * @param data the recorded session data
     * @return a map containing the session information
    */
    private Map<String, Object> buildMetadata(SessionData data) {
        int totalCompiles = 0;
        int successCompiles = 0;
        int totalRuns = 0;
        int successRuns = 0;

        for (CompileEvent ce : data.getCompileEvents()) {
            if (!ce.getProjectName().equals(projectName)) {
                continue;
            }
            totalCompiles++;
            if (ce.isSuccess()) {
                successCompiles++;
            }
        }
        for (RunEvent re : data.getRunEvents()) {
            if (!re.getProjectName().equals(projectName)) {
                continue;
            }
            totalRuns++;
            if (re.isSuccess()) {
                successRuns++;
            } 
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("student_id", studentId);
        m.put("project_name", projectName);
        m.put("session_id", data.getSessionId());
        m.put("session_start_ms", data.getStartMs());
        m.put("session_duration_ms", data.getSessionDurationMs());
        m.put("active_time_ms", data.getActiveTimeMs());
        m.put("idle_time_ms", data.getIdleTimeMs());
        m.put("typing_cadence_ms", data.getTypingCadence());
        m.put("edits_per_minute", data.getEditsPerMinute());
        m.put("total_compilations", totalCompiles);
        m.put("successful_compilations", successCompiles);
        m.put("failed_compilations", totalCompiles - successCompiles);
        m.put("total_execution_attempts", totalRuns);
        m.put("successful_executions", successRuns);
        m.put("failed_executions", totalRuns - successRuns);
        return m;
    }

    /**
     * Calculates the active and idle time percentages for the report
     * @param data the recorded session data
     * @return a map containing active and idle time information
    */
    private Map<String, Object> buildActiveIdle(SessionData data) {
        long active = data.getActiveTimeMs();
        long idle = data.getIdleTimeMs();
        long total = active + idle;

        double activePercent;
        if (total == 0) {
            activePercent = 0.0;
        } else {
            activePercent = active * 100.0 / total;
        }
        double idlePercent;
        if (total == 0) {
            idlePercent = 0.0;
        } else {
            idlePercent = idle * 100.0 / total;
        }

        Map<String, Object> m = new LinkedHashMap<>();
        m.put("active_ms", active);
        m.put("idle_ms", idle);
        m.put("active_percent", activePercent);
        m.put("idle_percent", idlePercent);
        return m;
    }


    /**
     * Counts how many times each compilation error occurred.
     * @param data the recorded session data
     * @return a list of errors and their frequencies
    */
    private List<Map<String, Object>> buildErrorFrequencies(SessionData data) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (CompileEvent ce : data.getCompileEvents()) {
            if (!ce.getProjectName().equals(projectName)) continue;
            for (CompileError error : ce.getErrors()) {
                String key = error.getFileName() + " — " + error.getMessage();
                counts.put(key, counts.getOrDefault(key, 0) + 1);
            }
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Integer> e : counts.entrySet()) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("error", e.getKey());
            row.put("frequency", e.getValue());
            result.add(row);
        }
        return result;
    }


    /**
     * Organizes the compilation results for the JSON report.
     * @param data the recorded session data
     * @return a list containing compilation details
    */
    private List<Map<String, Object>> buildCompilationSummary(SessionData data) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (CompileEvent ce : data.getCompileEvents()) {
            if (!ce.getProjectName().equals(projectName)) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("timestamp", ce.getTimestampIso());
            
            String resultText;
            if (ce.isSuccess()) {
                resultText = "SUCCESS";
            } else {
                resultText = "FAILED";
            }
            row.put("result", resultText);
            
            row.put("duration_ms", ce.getDurationMs());
            row.put("error_count", ce.getErrorCount());
            String first = "-";
            if (!ce.getErrors().isEmpty()) {
                CompileError firstError = ce.getErrors().get(0);
                first = firstError.getFileName() + ":" + firstError.getLineNumber();
            }
            row.put("first_error", first);
            result.add(row);
        }
        return result;
    }

    /**
     * Organizes the recorded class activities for the JSON report.
     * @param data the recorded session data
     * @return a list containing class activity information
    */
    private List<Map<String, Object>> buildClassProgression(SessionData data) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (ClassEvent ce : data.getClassEvents()) {
            if (!ce.getProjectName().equals(projectName)) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("timestamp", ce.getTimestampIso());
            String action = ce.getAction();
            if ("ClassModified".equals(action)) {
                action = "Method/Class Edit";
            } else if ("ClassCreated".equals(action)) {
                action = "Class Created";
            } else if ("MethodAdded".equals(action)) {
                action = "Method Added";
            } else if ("MethodRemoved".equals(action)) {
                action = "Method Removed";
            }
            row.put("action", action);
            row.put("class_name", ce.getClassName());
            result.add(row);
        }
        return result;
    }

    /**
     * Organizes the program execution results for the JSON report.
     * @param data the recorded session data
     * @return a list containing execution details
    */
    private List<Map<String, Object>> buildExecutionSummary(SessionData data) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (RunEvent re : data.getRunEvents()) {
            if (!re.getProjectName().equals(projectName)) continue;
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("timestamp", re.getTimestampIso());
            
            String resultText;
            if (re.isSuccess()) {
                resultText = "OK";
            } else {
                resultText = "FAILED";
            }
            row.put("result", resultText);
            
            row.put("duration_ms", re.getDurationMs());
            
            String messageText;
            if (re.getMessage() == null) {
                messageText = "-";
            } else {
                messageText = re.getMessage();
            }
            row.put("message", messageText);
            
            result.add(row);
        }
        return result;
    }
}