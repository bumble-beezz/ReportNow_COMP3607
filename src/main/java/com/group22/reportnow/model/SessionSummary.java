//Student IDs: 816044172, 400017923


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

    /**
     * Gets the name of the project
     * @return the project name or "Unknown Project" if it is empty
     */
    public String getProjectName() {
        if (projectName == null || projectName.isBlank()) {
            return "Unknown Project";
        }
        return projectName;
    }

    /**
     * Get the student's ID
     * @return the student ID
     */
    public String getStudentId() {
        return studentId;
    }

    /**
     * Gets the unique ID for this session
     * @return the session ID
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the time the session started
     * @return the session start time in milliseconds
     */
    public long getSessionStartMs() {
        return sessionStartMs;
    }

    /**
     * Gets the total duration of the session
     * @return the session duration in milliseconds
     */
    public long getSessionDurationMs() {
        return sessionDurationMs;
    }

    /**
     * Gets the total number of compilation attempts
     * @return the total compilations
     */
    public int getTotalCompilations() {
        return totalCompilations;
    }

    /**
     * Gets the sucessful compilations
     * @return the successful compilations
     */
    public int getSuccessfulCompilations() {
        return successfulCompilations;
    }

    /**
     * Gets the number of failed compilations
     * @return the failed compilations
     */
    public int getFailedCompilations() {
        return failedCompilations;
    }

    /**
     * Gets the number of execution attempts
     * @return the total execution attempts
     */
    public int getTotalExecutionAttempts() {
        return totalExecutionAttempts;
    }

    /**
     * Gets the number of successful executions
     * @return the successful executions
     */
    public int getSuccessfulExecutions() {
        return successfulExecutions;
    }

    /**
     * Gets the number of failed executions
     * @return the failed executions
     */
    public int getFailedExecutions() {
        return failedExecutions;
    }

    /**
     * Gets the calculated active time
     * @return the active time in milliseconds
     */
    public long getActiveTimeMs() {
        return activeTimeMs;
    }

    /**
     * Gets the calculated idle time
     * @return the idle time in milliseconds
     */
    public long getIdleTimeMs() {
        return idleTimeMs;
    }

    /**
     * Gets the calculated typing cadence
     * @return the typing cadence in milliseconds
     */
    public long getTypingCadenceMs() {
        return typingCadenceMs;
    }

    /**
     * Gets the calculated edits per minute
     * @return the edits per minute
     */
    public double getEditsPerMinute() {
        return editsPerMinute;
    }
}