package com.sap.bfx.callback.operation;

/**
 * Frontend operation that triggers a full re-initialization of the form.
 * The frontend will re-execute the same initialization logic that runs on startup.
 */
public class ResetOperation extends FrontendOperation {

    public ResetOperation() {
        super("reset");
    }
}
