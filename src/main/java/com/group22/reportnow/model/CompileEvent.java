package com.group22.reportnow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompileEvent extends TelemetryEvent {

    @JsonProperty("project_name")
    private final String projectName;

    @JsonProperty("success")
    private final boolean success;

    @JsonProperty("duration_ms")
    private final long durationMs;

    @JsonProperty("errors")
    private final List<CompileError> errors;

    public CompileEvent(long timestampMs, String projectName, boolean success,long durationMs, List<CompileError> errors) {
        super(timestampMs);
        this.projectName = projectName;
        this.success = success;
        this.durationMs = durationMs;
        if (errors == null) {
            this.errors = Collections.emptyList();
        } else {
            this.errors = Collections.unmodifiableList(new ArrayList<>(errors));
        }
    }

    @Override
    public String getType() {
        return "COMPILATION";
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

    public List<CompileError> getErrors() {
        return errors;
    }

    public int getErrorCount() {
        return errors.size();
    }
}