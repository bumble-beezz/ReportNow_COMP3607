package com.group22.reportnow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores information about a program execution in BlueJ.
 * Records whether it succeeded and any message about the result.
 */
public class RunEvent extends TelemetryEvent {

    /** Stores the name of the project being run. */
    @JsonProperty("project_name")
    private final String projectName;

    /** Stores whether the execution finished successfully. */
    @JsonProperty("success")
    private final boolean success;

    /** Stores the recorded execution duration in milliseconds. */
    @JsonProperty("duration_ms")
    private final long durationMs;

    /** Stores a message about the execution result or exception. */
    @JsonProperty("message")
    private final String message;

    /**
     * Creates an event containing the result of a program execution.
     * @param timestampMs the time the event happened in milliseconds
     * @param projectName the name of the project
     * @param success whether the execution finished successfully
     * @param durationMs the recorded duration in milliseconds
     * @param message a message about the result or exception
    */
    public RunEvent(long timestampMs, String projectName, boolean success, long durationMs, String message) {
        super(timestampMs);
        this.projectName = projectName;
        this.success = success;
        this.durationMs = durationMs;
        this.message = message;
    }

    /**
     * Returns the type of telemetry event.
     * @return EXECUTION to identify this as an execution event
    */
    @Override
    public String getType() {
        return "EXECUTION";
    }

    /**
     * Gets the name of the project where the execution happened.
     * @return the project name
    */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Checks whether the execution finished successfully.
     * @return true if successful, otherwise false
    */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the recorded execution duration.
     * @return the duration in milliseconds
    */
    public long getDurationMs() {
        return durationMs;
    }

    /**
     * Gets the message recorded for the execution.
     * @return the execution message, or null if none was recorded
    */
    public String getMessage() {
        return message;
    }
}