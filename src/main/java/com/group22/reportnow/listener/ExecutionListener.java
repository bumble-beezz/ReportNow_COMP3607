//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/event/InvocationFinishedListener.html
//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/event/InvocationFinishedEvent.html
package com.group22.reportnow.listener;

import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.event.InvocationFinishedEvent;
import bluej.extensions2.event.InvocationFinishedListener;

public class ExecutionListener implements InvocationFinishedListener {
    private final SessionData data;

    public ExecutionListener(SessionData data) {
        this.data = data;
    }

    @Override
    public void invocationFinished(InvocationFinishedEvent event) {
        InvocationFinishedEvent.EventType type = event.getEventType();
        boolean success = false;
        String message = null;

        if (type == InvocationFinishedEvent.EventType.NORMAL_EXIT) {
            success = true;
        }else if(type == InvocationFinishedEvent.EventType.EXCEPTION_EXIT) {
            message = "Runtime exception";
        }else{
            message = type.name();
        }
        data.recordRun(data.getCurrentProjectName(), success, 0L, message);
    }
}