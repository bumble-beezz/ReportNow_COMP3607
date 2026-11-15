package com.group22.reportnow.export;

/**
 * Handles errors that occur while generating reports
 */
public class ExportException extends Exception {
    /**
     * Creates an export exception with an error message
     * @param message the message describing the error
    */
    public ExportException(String message) {
        super(message);
    }

    /**
     * Creates an export exception with an error message and its cause.
     * @param message the message describing the error
     * @param cause the original error that occurred
    */
    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}