package com.group22.reportnow.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores a summary of a ReportNow session.
 * Contains the student, project and activity statistics used when displaying previous sessions.
 */
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

    @JsonProperty("successful_executions")
    private int successfulExecutions;

    @JsonProperty("failed_executions")
    private int failedExecutions;

    @JsonProperty("active_time_ms")
    private long activeTimeMs;

    @JsonProperty("idle_time_ms")
    private long idleTimeMs;

    @JsonProperty("typing_cadence_ms")
    private long typingCadenceMs;

    @JsonProperty("edits_per_minute")
    private double editsPerMinute;

    /**
     * Creates an empty session summary.
     * This allows Jackson to load information from saved JSON reports.
    */
    public SessionSummary() {
    }

    /**
     *Creates a session summary using the recorded session statistics.
     * @param studentId the student's ID
     * @param projectName the name of the project
     * @param sessionId the unique ID of the session
     * @param sessionStartMs the session start time in milliseconds
     * @param sessionDurationMs the total session duration in milliseconds
     * @param totalCompilations the total number of compilation attempts
     * @param successfulCompilations the number of successful compilations
     * @param failedCompilations the number of failed compilations
     * @param totalExecutionAttempts the total number of execution attempts
     * @param successfulExecutions the number of successful executions
     * @param failedExecutions the number of failed executions
     * @param activeTimeMs the estimated active time in milliseconds
     * @param idleTimeMs the estimated idle time in milliseconds
     * @param typingCadenceMs the estimated time between detected edits
     * @param editsPerMinute the estimated number of detected edits per minute
    */
    public SessionSummary(String studentId, String projectName, String sessionId,
                          long sessionStartMs, long sessionDurationMs,
                          int totalCompilations, int successfulCompilations, int failedCompilations,
                          int totalExecutionAttempts, int successfulExecutions, int failedExecutions,
                          long activeTimeMs, long idleTimeMs,
                          long typingCadenceMs, double editsPerMinute) {
        this.studentId = studentId;
        this.projectName = projectName;
        this.sessionId = sessionId;
        this.sessionStartMs = sessionStartMs;
        this.sessionDurationMs = sessionDurationMs;
        this.totalCompilations = totalCompilations;
        this.successfulCompilations = successfulCompilations;
        this.failedCompilations = failedCompilations;
        this.totalExecutionAttempts = totalExecutionAttempts;
        this.successfulExecutions = successfulExecutions;
        this.failedExecutions = failedExecutions;
        this.activeTimeMs = activeTimeMs;
        this.idleTimeMs = idleTimeMs;
        this.typingCadenceMs = typingCadenceMs;
        this.editsPerMinute = editsPerMinute;
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

    public int getSuccessfulExecutions() {
        return successfulExecutions;
    }

    public int getFailedExecutions() {
        return failedExecutions;
    }

    public long getActiveTimeMs() {
        return activeTimeMs;
    }

    public long getIdleTimeMs() {
        return idleTimeMs;
    }

    public long getTypingCadenceMs() {
        return typingCadenceMs;
    }

    public double getEditsPerMinute() {
        return editsPerMinute;
    }
}