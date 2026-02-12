package com.sap.bfx.callback;

/**
 * Callback interface for row iteration
 */
@FunctionalInterface
public interface RowVisitor {
    /**
     * Is called for each visited row
     *
     * @param rowId   - The id of the visited row
     * @param context - Callback context in order to retrieve information and forms functions
     * @return either true to proceed or false to cancel iteration through rows
     */
    boolean visit(final String rowId, final Context<? extends AccessClass> context);
}
