//Student IDs: 400017923, 816039066, 816044172

//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/event/InvocationFinishedListener.html
//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/event/InvocationFinishedEvent.html

package com.group22.reportnow.listener;

import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.event.InvocationFinishedEvent;
import bluej.extensions2.event.InvocationFinishedListener;

/**
 * Tracks program execution results in BlueJ.
 * Records whether a method finishes normally or with an exception.
 */
public class ExecutionListener implements InvocationFinishedListener {
    /** Stores the session data used to record execution events. */
    private final SessionData data;

    /**
     * Creates an execution listener using the session data.
     * @param data the session data used to store execution results
    */
    public ExecutionListener(SessionData data) {
        this.data = data;
    }

    /**
     * Records the result of a completed method invocation.
     * Stores whether it succeeded or ended with an exception.
     * @param event the completed invocation event from BlueJ
    */
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