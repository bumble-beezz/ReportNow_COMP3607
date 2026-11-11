package com.group22.reportnow.export;

import java.io.File;

import com.group22.reportnow.storage.SessionData;

public class ExportContext {

    private ExportStrategy strategy;

    public void setStrategy(ExportStrategy strategy) {
        this.strategy = strategy;
    }

    public void export(SessionData data, File file) throws ExportException {
        if (strategy == null) {
            throw new IllegalStateException("No ExportStrategy set");
        }
        strategy.export(data, file);
    }
}