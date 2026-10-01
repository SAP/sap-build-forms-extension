package com.sap.bfx.callback.operation;

/**
 * Frontend operation that removes the readonly set by a previous ReadonlyOperation.
 * The form returns to its normal editable state.
 */
public class EditableOperation extends FrontendOperation {

    public EditableOperation() {
        super("editable");
    }
}
