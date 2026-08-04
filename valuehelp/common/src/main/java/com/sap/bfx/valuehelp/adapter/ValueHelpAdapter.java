package com.sap.bfx.valuehelp.adapter;

import com.sap.bfx.valuehelp.model.ValueHelp;
import com.sap.bfx.valuehelp.model.ValueHelpDef;

import java.util.Locale;

/**
 * Adapter interface for Value Help operations.
 */
public interface ValueHelpAdapter {

    /**
     * This method is used to check if the provided ValueHelpDef is valid and can be processed by this adapter.
     * It should return true if the ValueHelpDef is valid, and false otherwise. The implementation of this method
     * will depend on the specific requirements of the ValueHelpDef and the logic of the adapter.
     */
    boolean check(ValueHelpDef vhd);

    /**
     * This method is used to query and retrieve a ValueHelp based on the provided ValueHelpDef and locale.
     * The implementation of this method will depend on the specific logic of the adapter and how it retrieves the
     * data for the ValueHelp. It should return a ValueHelp object that contains the relevant information based on
     * the ValueHelpDef and locale.
     *
     * @param vdh    the ValueHelpDef that defines the structure and requirements for the ValueHelp to be retrieved
     * @param locale the Locale that specifies the language and region for which the ValueHelp should be retrieved
     * @return a ValueHelp object that contains the relevant information based on the ValueHelpDef and locale
     */
    ValueHelp query(ValueHelpDef vdh, Locale locale);
}