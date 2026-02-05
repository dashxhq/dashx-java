package com.dashx.exception;

/**
 * Exception thrown when there are configuration-related errors in the DashX SDK.
 * This includes invalid URLs, missing required configuration, or invalid configuration values.
 */
public class DashXConfigurationException extends DashXException {
    /**
     * Constructs a new configuration exception with the specified detail message.
     *
     * @param message the detail message
     */
    public DashXConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructs a new configuration exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of this exception
     */
    public DashXConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
