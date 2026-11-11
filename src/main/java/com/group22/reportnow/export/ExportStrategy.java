package com.group22.reportnow.export;

import java.io.File;

import com.group22.reportnow.storage.SessionData;

public interface ExportStrategy {
    void export(SessionData data, File file) throws ExportException;
}
