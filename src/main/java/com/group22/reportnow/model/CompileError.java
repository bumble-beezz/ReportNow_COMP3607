package com.group22.reportnow.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CompileError {

    @JsonProperty("file_name")
    private final String fileName;

    @JsonProperty("line_number")
    private final int lineNumber;

    @JsonProperty("error_code")
    private final String errorCode;

    @JsonProperty("message")
    private final String message;

    public CompileError(String fileName, int lineNumber,  String errorCode, String message) {
        this.fileName = fileName;
        this.lineNumber = lineNumber;
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getFileName() {
        return fileName;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getErrorCode() {
        
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}