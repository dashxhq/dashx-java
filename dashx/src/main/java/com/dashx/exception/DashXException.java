package com.dashx.exception;

/**
 * Base exception class for all DashX-related exceptions.
 * All custom exceptions in the DashX SDK should extend this class.
 */
public class DashXException extends RuntimeException {
    /**
     * Constructs a new DashX exception with the specified detail message.
     *
     * @param message the detail message
     */
    public DashXException(String message) {
        super(message);
    }

    /**
     * Constructs a new DashX exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public DashXException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new DashX exception with the specified cause.
     *
     * @param cause the cause of this exception
     */
    public DashXException(Throwable cause) {
        super(cause);
    }
}
