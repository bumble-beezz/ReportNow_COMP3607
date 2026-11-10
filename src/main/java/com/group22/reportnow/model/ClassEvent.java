package com.group22.reportnow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ClassEvent extends TelemetryEvent {

    @JsonProperty("action")
    private final String action;

    @JsonProperty("class_name")
    private final String className;

    @JsonProperty("project_name")
    private final String projectName;

    public ClassEvent(long timestampMs, String action, String className, String projectName) {
        super(timestampMs);
        this.action = action;
        this.className = className;
        this.projectName = projectName;
    }

    @Override
    public String getType() {
        return "CLASS";
    }

    public String getAction() {
        return action;
    }

    public String getClassName() {
        return className;
    }

    public String getProjectName() {
        return projectName;
    }
}