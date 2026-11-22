package com.elementalrift.exceptions;

/**
 * Custom Exception thrown when parsing or loading a corrupted, malformed or invalid save file.
 * [OOP Requirement: Custom Exception 3 of 3]
 */
public class InvalidSaveFileException extends Exception {
    private final String filename;
    private final int lineNumber;

    public InvalidSaveFileException(String message, String filename, int lineNumber) {
        super(String.format("Save file error in '%s' at line %d: %s", filename, lineNumber, message));
        this.filename = filename;
        this.lineNumber = lineNumber;
    }

    public InvalidSaveFileException(String message, Throwable cause) {
        super(message, cause);
        this.filename = "unknown";
        this.lineNumber = -1;
    }

    public String getFilename() {
        return filename;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
