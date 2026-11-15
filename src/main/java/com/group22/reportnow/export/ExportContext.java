// Student IDs: 816039066, 400017923, 816044172

package com.group22.reportnow.export;

import java.io.File;

import com.group22.reportnow.storage.SessionData;

/**
 * Allows ReportNow to choose which exporter to use.
 * It can use either the JSON or PDF export strategy.
 */
public class ExportContext {
    /** Stores the export strategy */
    private ExportStrategy strategy;

    /**
     * Sets the exporter that will be used to generate the report.
     * @param strategy the selected export strategy
    */
    public void setStrategy(ExportStrategy strategy) {
        this.strategy = strategy;
    }

    /**
     * Exports the session data using the selected strategy.
     * @param data the session data being exported
     * @param file the file where the report will be saved
     * @throws ExportException if exporting fails
     * @throws IllegalStateException if no strategy has been selected
    */
    public void export(SessionData data, File file) throws ExportException {
        if (strategy == null) {
            throw new IllegalStateException("No ExportStrategy set");
        }
        strategy.export(data, file);
    }
}