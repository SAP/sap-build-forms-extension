package com.sap.bfx.callback.operation;

/**
 * Frontend operation that sets the entire form to readonly mode.
 * If text and type are provided, the frontend will additionally display
 * a non-closeable dialog with the given text and severity type.
 */
public class ReadonlyOperation extends FrontendOperation {

    private final String text;
    private final String type;

    /**
     * Sets the form to readonly without showing a dialog.
     */
    public ReadonlyOperation() {
        super("readonly");
        this.text = null;
        this.type = null;
    }

    /**
     * Sets the form to readonly and shows a non-closeable dialog with the given text.
     * @param text the text to display in the dialog (already localized)
     * @param type the severity type of the dialog (e.g., "e" for Error, "w" for Warning, "i" for Info)
     */
    public ReadonlyOperation(final String text, final String type) {
        super("readonly");
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }
}
