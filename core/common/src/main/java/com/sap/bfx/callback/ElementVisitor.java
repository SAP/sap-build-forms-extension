package com.sap.bfx.callback;

import com.sap.bfx.definition.ElementDefinition;

@FunctionalInterface
public interface ElementVisitor {
    /**
     * @param ed      Element definition of the given element
     * @param rowId   RowId of the element, can be the root row
     * @param context Callback context in order to retrieve information and forms functions
     * @return either true to proceed or false to cancel iteration through rows
     */
    boolean visit(final ElementDefinition ed, final String rowId, final Context<? extends AccessClass> context);
}
