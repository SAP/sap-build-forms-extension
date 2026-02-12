package com.sap.bfx.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.bfx.utils.Identifier;

public enum TableSelectType implements Identifier {

    @JsonProperty("none")
    None("none"),
    @JsonProperty("single")
    Single("single"),
    @JsonProperty("multiple")
    Multiple("multiple");

    private final String identifier;

    TableSelectType(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public java.lang.String getIdentifier() {
        return this.identifier;
    }

}
