package com.group22.reportnow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RunEvent extends TelemetryEvent {

    @JsonProperty("project_name")
    private final String projectName;

    @JsonProperty("success")
    private final boolean success;

    @JsonProperty("duration_ms")
    private final long durationMs;

    @JsonProperty("message")
    private final String message;

    public RunEvent(long timestampMs, String projectName, boolean success, long durationMs, String message) {
        super(timestampMs);
        this.projectName = projectName;
        this.success = success;
        this.durationMs = durationMs;
        this.message = message;
    }

    @Override
    public String getType() {
        return "EXECUTION";
    }

    public String getProjectName() {
        return projectName;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getDurationMs() {
        return durationMs;
    }

    public String getMessage() {
        return message;
    }
}