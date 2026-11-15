//Student IDs: 400017923, 816039066

//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/event/CompileListener.html
//https://www.bluej.org/doc/extensionsAPI2/bluej/extensions2/event/CompileEvent.html

package com.group22.reportnow.listener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.group22.reportnow.model.CompileError;
import com.group22.reportnow.parser.ParseCompileErrors;
import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.event.CompileEvent;
import bluej.extensions2.event.CompileListener;

/**
 * Tracks compilation attempts in BlueJ.
 * Records successful and failed compilations, their duration and any compilation errors.
 */
public class RecordCompile implements CompileListener {
    /** Stores the session data used to record compilations. */
    private final SessionData data;
    /** Parses compilation error and warning messages. */
    private final ParseCompileErrors parser;
    /** Stores errors and warnings from the current compilation. */
    private final List<CompileError> pendingErrors = new ArrayList<>();
    /** Stores the time when the current compilation started. */
    private long startTimeMs;


    /**
     * Sets up compilation tracking and the compilation error parser.
     * @param data the session data used to record compilations
    */
    public RecordCompile(SessionData data) {
        this.data = data;
        this.parser = new ParseCompileErrors();
    }

    /**
     * Records the compilation start time and clears previous errors.
     * @param event the compilation start event from BlueJ
    */
    @Override
    public void compileStarted(CompileEvent event) {
        startTimeMs = System.currentTimeMillis();
        pendingErrors.clear();
    }

    /**
     * Collects an error message produced during compilation.
     * @param event the compilation error event from BlueJ
    */
    @Override
    public void compileError(CompileEvent event) {
        addMessage(event, "ERROR");
    }

    /**
     * Collects a warning message produced during compilation.
     * @param event the compilation warning event from BlueJ
    */
    @Override
    public void compileWarning(CompileEvent event) {
        addMessage(event, "WARNING");
    }

    /**
     * Records a successful compilation and calculates its duration.
     * @param event the successful compilation event from BlueJ
    */
    @Override
    public void compileSucceeded(CompileEvent event) {
        long duration = System.currentTimeMillis() - startTimeMs;
        data.recordCompile(data.getCurrentProjectName(), true, duration, new ArrayList<>());
        pendingErrors.clear();
    }


    /**
     * Records a failed compilation, its duration and collected errors.
     * @param event the failed compilation event from BlueJ
    */
    @Override
    public void compileFailed(CompileEvent event) {
        long duration = System.currentTimeMillis() - startTimeMs;
        data.recordCompile(data.getCurrentProjectName(), false, duration,
                new ArrayList<>(pendingErrors));
        pendingErrors.clear();
    }

    /**
     * Parses a compilation message and stores its details.
     * Uses the BlueJ event information if parsing is unsuccessful.
     * @param event the compilation event containing the message
     * @param severity the type of message, such as ERROR or WARNING
    */
    private void addMessage(CompileEvent event, String severity) {
        String rawMessage = event.getErrorMessage();
        List<CompileError> parsed = parser.parse(rawMessage);
        if (!parsed.isEmpty()) {
            pendingErrors.addAll(parsed);
            return;
        }
        File[] files = event.getFiles();
        String fileName = "unknown";
        if (files != null && files.length > 0) {
            fileName = files[0].getName();
        }
        pendingErrors.add(new CompileError(fileName, event.getErrorLineNumber(), severity, rawMessage));
    }
}