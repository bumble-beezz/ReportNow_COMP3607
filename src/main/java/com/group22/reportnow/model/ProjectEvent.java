package com.group22.reportnow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProjectEvent extends TelemetryEvent {

    @JsonProperty("action")
    private final String action;

    @JsonProperty("project_name")
    private final String projectName;

    public ProjectEvent(long timestampMs, String action, String projectName) {
        super(timestampMs);
        this.action = action;
        this.projectName = projectName;
    }

    @Override
    public String getType() {
        return "PROJECT";
    }

    public String getAction() {
        return action;
    }

    public String getProjectName() {
        return projectName;
    }
}