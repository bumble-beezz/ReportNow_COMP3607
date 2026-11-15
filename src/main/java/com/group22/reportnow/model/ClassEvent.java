//Student IDs: 816044172, 400017923


package com.group22.reportnow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores information about changes made to a Java class.
 * This includes the type of change, class name and project name.
 */
public class ClassEvent extends TelemetryEvent {

    /** Stores the type of class activity that happened. */
    @JsonProperty("action")
    private final String action;

    /** Stores the name of the class involved. */
    @JsonProperty("class_name")
    private final String className;

    /** Stores the name of the project containing the class. */
    @JsonProperty("project_name")
    private final String projectName;

    /**
     * Creates an event for a class activity.
     * @param timestampMs the time the event happened in milliseconds
     * @param action the type of class activity
     * @param className the name of the class
     *  @param projectName the name of the project
    */
    public ClassEvent(long timestampMs, String action, String className, String projectName) {
        super(timestampMs);
        this.action = action;
        this.className = className;
        this.projectName = projectName;
    }

    /**
     * Returns the type of telemetry event.
     * @return CLASS to identify this as a class event
    */
    @Override
    public String getType() {
        return "CLASS";
    }

    /**
     * Gets the type of class activity.
     * @return the recorded class action
    */
    public String getAction() {
        return action;
    }

    /**
     * Gets the name of the class involved in the event.
     * @return the class name
    */
    public String getClassName() {
        return className;
    }

    /**
     * Gets the project name linked to the class event.
     * @return the project name
    */
    public String getProjectName() {
        return projectName;
    }
}