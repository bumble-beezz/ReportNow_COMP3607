package com.group22.reportnow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stores information about a compilation error or warning.
 * This includes the file, line number, error type and message.
 */
public class CompileError {
    /** Stores the file where the error occurred. */
    @JsonProperty("file_name")
    private final String fileName;

    /** Stores the line number linked to the error. */
    @JsonProperty("line_number")
    private final int lineNumber;

    /** Stores the error or warning type. */
    @JsonProperty("error_code")
    private final String errorCode;

    /** Stores the message describing the problem. */
    @JsonProperty("message")
    private final String message;

    /**
     * Creates a compilation error using the details provided.
     * @param fileName the file where the problem occurred
     * @param lineNumber the line number of the problem
     * @param errorCode the type or code of the problem
     * @param message the message explaining the problem
    */
    public CompileError(String fileName, int lineNumber,  String errorCode, String message) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.errorCode = errorCode;
        this.message = message;
    }

    /**    
     * Gets the name of the file containing the error.
     * @return the file name
    */
    public String getFileName() {
        return fileName;
    }

    /**
     * Gets the line number where the error occurred.
     * @return the line number
    */
    public int getLineNumber() {
        return lineNumber;
    }

    /**
     * Gets the error or warning code.
     * @return the error code
    */
    public String getErrorCode() {
        
        return errorCode;
    }

    /**
     * Gets the message explaining the compilation problem.
     * @return the error message
    */
    public String getMessage() {
        return message;
    }
}