package com.group22.reportnow.listener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.group22.reportnow.model.CompileError;
import com.group22.reportnow.parser.ParseCompileErrors;
import com.group22.reportnow.storage.SessionData;

import bluej.extensions2.event.CompileEvent;
import bluej.extensions2.event.CompileListener;

public class RecordCompile implements CompileListener {
    private final SessionData data;
    private final ParseCompileErrors parser;
    private final List<CompileError> pendingErrors = new ArrayList<>();
    private long startTimeMs;

    public RecordCompile(SessionData data) {
        this.data = data;
        this.parser = new ParseCompileErrors();
    }

    @Override
    public void compileStarted(CompileEvent event) {
        startTimeMs = System.currentTimeMillis();
        pendingErrors.clear();
    }

    @Override
    public void compileError(CompileEvent event) {
        addMessage(event, "ERROR");
    }

    @Override
    public void compileWarning(CompileEvent event) {
        addMessage(event, "WARNING");
    }

    @Override
    public void compileSucceeded(CompileEvent event) {
        long duration = System.currentTimeMillis() - startTimeMs;
        data.recordCompile(data.getCurrentProjectName(), true, duration, new ArrayList<>());
        pendingErrors.clear();
    }

    @Override
    public void compileFailed(CompileEvent event) {
        long duration = System.currentTimeMillis() - startTimeMs;
        data.recordCompile(data.getCurrentProjectName(), false, duration,
                new ArrayList<>(pendingErrors));
        pendingErrors.clear();
    }

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