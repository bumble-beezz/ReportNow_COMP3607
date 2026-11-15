package com.group22.reportnow.model;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Acts as the parent class for ReportNow telemetry events.
 * Stores the time an event happened and provides a common method for identifying the event type.
 */
public abstract class TelemetryEvent {
    /** Stores the event timestamp in milliseconds.*/
    @JsonProperty("timestamp_ms")
    private final long timestampMs;

    /** Stores the event timestamp as an ISO date and time string.*/
    @JsonProperty("timestamp_iso")
    private final String timestampIso;

    /**
     * Creates a telemetry event and saves its timestamp in milliseconds and ISO format.
     * @param timestampMs the time the event happened in milliseconds
    */
    protected TelemetryEvent(long timestampMs) {
        this.timestampMs = timestampMs;
        this.timestampIso = Instant.ofEpochMilli(timestampMs).toString();
    }

    /**
     * Gets the event timestamp in milliseconds.
     * @return the event timestamp in milliseconds
    */
    public long getTimestampMs() {
        return timestampMs;
    }

    /**
     * Gets the event timestamp as an ISO date and time string.
     * @return the formatted timestamp
    */
    public String getTimestampIso() {
        return timestampIso;
    }

    /**
     * Gets the type of telemetry event.
     * Each event subclass provides its own event type.
     * @return the type of event
    */
    @JsonProperty("event_type")
    public abstract String getType();
}