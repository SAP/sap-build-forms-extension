package com.sap.bfx.exception;

/**
 * BadRequestException is thrown to indicate that a request is invalid or cannot be processed,
 * e.g. by providing invalid parameters.
 */
public class BadRequestException extends FormsCoreException {
    /**
     * Constructor
     *
     * @param message the detail message
     */
    public BadRequestException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public BadRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     *
     * @param cause the cause
     */
    public BadRequestException(Throwable cause) {
        super(cause);
    }
}
