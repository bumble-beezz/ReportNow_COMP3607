package com.group22.reportnow.listener;

import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.event.InvocationFinishedEvent;
import bluej.extensions2.event.InvocationFinishedListener;

public class ExecutionListener implements InvocationFinishedListener {
    private final SessionData data;
    private long startTimeMs = System.currentTimeMillis();

    public ExecutionListener(SessionData data) {
        this.data = data;
    }

    @Override
    public void invocationFinished(InvocationFinishedEvent event) {
        long duration = System.currentTimeMillis() - startTimeMs;
        InvocationFinishedEvent.EventType type = event.getEventType();
        boolean success = false;
        String message = null;

        if (type == InvocationFinishedEvent.EventType.NORMAL_EXIT) {
            success = true;
        } else if (type == InvocationFinishedEvent.EventType.EXCEPTION_EXIT) {
            message = "Runtime exception";
        } else {
            message = type.name();
        }

        data.recordRun(data.getCurrentProjectName(), success, duration, message);
        startTimeMs = System.currentTimeMillis();
    }
}