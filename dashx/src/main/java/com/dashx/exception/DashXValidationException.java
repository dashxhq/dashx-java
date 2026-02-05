package com.dashx.exception;

/**
 * Exception thrown when input validation fails in the DashX SDK.
 * This includes null checks, empty string validation, and other input parameter validation.
 */
public class DashXValidationException extends DashXException {
    /**
     * Constructs a new validation exception with the specified detail message.
     *
     * @param message the detail message
     */
    public DashXValidationException(String message) {
        super(message);
    }

    /**
     * Constructs a new validation exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public DashXValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
