//Student IDs: 816039066, 400017923

package com.group22.reportnow.export;

import java.io.File;

import com.group22.reportnow.storage.SessionData;

/**
 * Defines the method used to export ReportNow session data.
 * Both the JSON and PDF exporters use this interface.
 */
public interface ExportStrategy {
    /**
     * Exports the session data to a file.
     * @param data the session data being exported
     * @param file the file where the report will be saved
     * @throws ExportException if an error occurs during exporting
    */
    void export(SessionData data, File file) throws ExportException;
}
