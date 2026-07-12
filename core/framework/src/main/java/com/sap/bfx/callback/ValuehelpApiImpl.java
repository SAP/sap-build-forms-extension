package com.sap.bfx.callback;

import com.sap.bfx.valuehelp.ValueHelpData;
import com.sap.bfx.valuehelp.ValueHelpService;

import java.util.Locale;

/**
 * Implementation of the ValuehelpApi interface that provides methods to interact with value help data. This class
 * is currently a placeholder and can be extended in the future to include specific functionalities related
 * to value helps.
 */
public class ValuehelpApiImpl implements ValuehelpApi {

    private final ValueHelpService valueHelpService;

    public ValuehelpApiImpl(ValueHelpService valueHelpService) {
        this.valueHelpService = valueHelpService;
    }

    @Override
    public ValueHelpData findLatest(String id, Locale locale) {
        return valueHelpService.findValues(id, locale);
    }
}
