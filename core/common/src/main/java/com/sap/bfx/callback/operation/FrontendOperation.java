package com.sap.bfx.callback.operation;

/**
 * Abstract base class for operations that the backend can send to the frontend.
 * Subclasses define specific operations by setting the command
 * in their constructor. The frontend uses the "command" property to distinguish operations.
 */
public abstract class FrontendOperation {

    private final String command;

    /**
     * @param command the command identifier sent to the frontend
     */
    protected FrontendOperation(final String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
