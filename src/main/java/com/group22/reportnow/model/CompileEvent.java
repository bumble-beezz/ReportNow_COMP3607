package com.group22.reportnow.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores information about a compilation attempt in BlueJ.
 * Records whether it succeeded, how long it took and any errors.
 */
public class CompileEvent extends TelemetryEvent {

    /** Stores the name of the project being compiled. */
    @JsonProperty("project_name")
    private final String projectName;

    /** Stores whether the compilation was successful. */
    @JsonProperty("success")
    private final boolean success;

    /** Stores the compilation duration in milliseconds. */
    @JsonProperty("duration_ms")
    private final long durationMs;

    /** Stores the errors and warnings from the compilation. */
    @JsonProperty("errors")
    private final List<CompileError> errors;

    /**
     * Creates an event containing the results of a compilation.
     * @param timestampMs the time the event happened in milliseconds
     * @param projectName the name of the project
     * @param success whether the compilation was successful
     * @param durationMs how long the compilation took in milliseconds
     * @param errors the errors and warnings found during compilation
    */
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

    /**
     * Returns the type of telemetry event.
     * @return COMPILATION to identify this as a compilation event
    */
    @Override
    public String getType() {
        return "COMPILATION";
    }

    /**
     * Gets the name of the project that was compiled.
     * @return the project name
    */
    public String getProjectName() {
        return projectName;
    }

    /**
     * Checks whether the compilation was successful.
     * @return true if successful, otherwise false
    */
    public boolean isSuccess() {
        return success;
    }

    /**
     * Gets the time taken to complete the compilation.
     * @return the compilation duration in milliseconds
    */
    public long getDurationMs() {
        return durationMs;
    }

    /**
     * Counts the errors and warnings stored during compilation.
     * @return a list of compilation errors and warnings
    */
    public List<CompileError> getErrors() {
        return errors;
    }

    /**
     * Counts the errors and warnings stored for this compilation.
     * @return the number of recorded errors and warnings
    */
    public int getErrorCount() {
        return errors.size();
    }
}