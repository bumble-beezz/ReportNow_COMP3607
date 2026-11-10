package com.group22.reportnow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionSummary {

    @JsonProperty("student_id")
    private String studentId;

    @JsonProperty("project_name")
    private String projectName;

    @JsonProperty("session_id")
    private String sessionId;

    @JsonProperty("session_start_ms")
    private long sessionStartMs;

    @JsonProperty("session_duration_ms")
    private long sessionDurationMs;

    @JsonProperty("total_compilations")
    private int totalCompilations;

    @JsonProperty("successful_compilations")
    private int successfulCompilations;

    @JsonProperty("failed_compilations")
    private int failedCompilations;

    @JsonProperty("total_execution_attempts")
    private int totalExecutionAttempts;

    @JsonProperty("active_time_ms")
    private long activeTimeMs;

    @JsonProperty("idle_time_ms")
    private long idleTimeMs;

    public SessionSummary() {
    }

    public SessionSummary(String studentId, String projectName, String sessionId, long sessionStartMs, long sessionDurationMs, int totalCompilations, int successfulCompilations,int failedCompilations, int totalExecutionAttempts, long activeTimeMs, long idleTimeMs) {
        this.studentId = studentId;
        this.projectName = projectName;
        this.sessionId = sessionId;
        this.sessionStartMs = sessionStartMs;
        this.sessionDurationMs = sessionDurationMs;
        this.totalCompilations = totalCompilations;
        this.successfulCompilations = successfulCompilations;
        this.failedCompilations = failedCompilations;
        this.totalExecutionAttempts = totalExecutionAttempts;
        this.activeTimeMs = activeTimeMs;
        this.idleTimeMs = idleTimeMs;
    }

    public String getProjectName() {
        if (projectName == null || projectName.isBlank()) {
            return "Unknown Project";
        }
        return projectName;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public long getSessionStartMs() {
        return sessionStartMs;
    }

    public long getSessionDurationMs() {
        return sessionDurationMs;
    }

    public int getTotalCompilations() {
        return totalCompilations;
    }

    public int getSuccessfulCompilations() {
        return successfulCompilations;
    }

    public int getFailedCompilations() {
        return failedCompilations;
    }

    public int getTotalExecutionAttempts() {
        return totalExecutionAttempts;
    }

    public long getActiveTimeMs() {
        return activeTimeMs;
    }

    public long getIdleTimeMs() {
        return idleTimeMs;
    }
}