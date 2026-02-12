package com.sap.bfx.exception;

/**
 * NotFoundException is thrown to indicate that a requested resource could not be found.
 */
public class NotFoundException extends FormsCoreException {
    /**
     * Constructor
     *
     * @param message the detail message
     */
    public NotFoundException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     *
     * @param cause the cause
     */
    public NotFoundException(Throwable cause) {
        super(cause);
    }
}
