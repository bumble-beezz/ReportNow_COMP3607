package com.group22.reportnow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores information about activities involving a BlueJ project.
 * This includes the type of activity and the project name.
 */
public class ProjectEvent extends TelemetryEvent {

    /** Stores the type of project activity that happened. */
    @JsonProperty("action")
    private final String action;

    /** Stores the name of the project involved. */
    @JsonProperty("project_name")
    private final String projectName;

    /**
     * Creates an event for a project activity.
     * @param timestampMs the time the event happened in milliseconds
     * @param action the type of project activity
     * @param projectName the name of the project
    */
    public ProjectEvent(long timestampMs, String action, String projectName) {
        super(timestampMs);
        this.action = action;
        this.projectName = projectName;
    }

    /**
     * Returns the type of telemetry event.
     * @return PROJECT to identify this as a project event
    */
    @Override
    public String getType() {
        return "PROJECT";
    }

    /**
     * Gets the type of project activity.
     * @return the recorded project action
    */
    public String getAction() {
        return action;
    }

    /**
     * Gets the name of the project involved in the event.
     * @return the project name
    */
    public String getProjectName() {
        return projectName;
    }
}