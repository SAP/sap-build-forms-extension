package com.sap.bfx.valuehelp.adapter;

import com.sap.bfx.valuehelp.model.ValueHelp;
import com.sap.bfx.valuehelp.model.ValueHelpDef;

import java.util.Locale;

/**
 * Adapter interface for Value Help operations.
 */
public interface ValueHelpAdapter {

    /**
     * Checks the validity of the given ValueHelpDef object.
     *
     * @param vhd
     * @return
     */
    boolean check(ValueHelpDef vhd);

    /**
     * Queries and retrieves a ValueHelp based on the provided ValueHelpDef and locale.
     *
     * @param vdh
     * @param locale
     * @return
     */
    ValueHelp query(ValueHelpDef vdh, Locale locale);
}
