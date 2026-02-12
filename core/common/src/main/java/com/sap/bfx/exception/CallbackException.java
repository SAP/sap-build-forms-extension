package com.sap.bfx.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Exception class for handling errors related to callbacks in the Forms application.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class CallbackException extends FormsCoreException {
    private String action;

    /**
     * Constructor
     *
     * @param message message
     */
    public CallbackException(String message) {
        super(message);
    }

    /**
     * Constructor
     *
     * @param message message
     * @param cause   cause of the exception
     */
    public CallbackException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor
     *
     * @param cause cause of the exception
     */
    public CallbackException(Throwable cause) {
        super(cause);
    }
}
