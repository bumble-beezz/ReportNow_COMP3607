package com.group22.reportnow.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class TelemetryEvent {

    @JsonProperty("timestamp_ms")
    private final long timestampMs;

    @JsonProperty("timestamp_iso")
    private final String timestampIso;

    protected TelemetryEvent(long timestampMs) {
        this.timestampMs = timestampMs;
        this.timestampIso = Instant.ofEpochMilli(timestampMs).toString();
    }

    public long getTimestampMs() {
        return timestampMs;
    }

    public String getTimestampIso() {
        return timestampIso;
    }

    @JsonProperty("event_type")
    public abstract String getType();
}