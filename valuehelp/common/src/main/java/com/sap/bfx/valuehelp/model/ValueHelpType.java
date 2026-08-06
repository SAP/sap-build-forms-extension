package com.sap.bfx.valuehelp.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.bfx.utils.Identifier;

/**
 * Enum to define the type of value help. This can be used to distinguish different types of value helps and handle
 * them accordingly on the frontend.
 */
public enum ValueHelpType implements Identifier {

    @JsonProperty("freestyle") FREESTYLE("freestyle"),
    @JsonProperty("currency") CURRENCY("currency");

    private final String identifier;

    ValueHelpType(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public java.lang.String getIdentifier() {
        return this.identifier;
    }

}
