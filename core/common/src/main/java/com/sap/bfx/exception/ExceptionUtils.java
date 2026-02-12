package com.sap.bfx.exception;

/**
 * Utility class for exception handling
 */
public final class ExceptionUtils {

    /**
     * Private constructor to prevent instantiation
     */
    private ExceptionUtils() {
    }

    /**
     * Convert any Throwable to FormsCoreException if not already one. Otherwise, return the given FormsCoreException.
     *
     * @param base base exception
     * @return FormsCoreException
     */
    public static FormsCoreException from(Throwable base) {
        if (base instanceof FormsCoreException) {
            return (FormsCoreException) base;
        } else {
            return new FormsCoreException(base);
        }
    }

    /**
     * Convert any Throwable to FormsCoreException if not already one. Otherwise, return the given FormsCoreException.
     *
     * @param message message
     * @param base    base exception
     * @return FormsCoreException
     */
    public static FormsCoreException from(String message, Throwable base) {
        if (base instanceof FormsCoreException) {
            return (FormsCoreException) base;
        } else {
            return new FormsCoreException(message, base);
        }
    }

    /**
     * Create a new FormsCoreException with the given message
     *
     * @param message message
     * @return FormsCoreException
     */
    public static FormsCoreException from(String message) {
        return new FormsCoreException(message);
    }

    /**
     * Convert any Throwable to CallbackException if not already one. Otherwise, return the given CallbackException.
     * Additionally, set the action property of the CallbackException.
     *
     * @param base   base exception
     * @param action action
     * @return CallbackException
     */
    public static CallbackException fromCallback(Throwable base, String action) {
        if (base instanceof CallbackException be) {
            be.setAction(action);
            return be;
        } else {
            CallbackException ex = new CallbackException(base);
            ex.setAction(action);
            return ex;
        }
    }
}
