package com.sap.bfx.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.bfx.utils.Identifier;

public enum TableStyleType implements Identifier {

    @JsonProperty("dialog")
    Dialog("dialog"),
    @JsonProperty("inline")
    Inline("inline"),
    @JsonProperty("gid")
    Gid("gid");

    private final String identifier;

    TableStyleType(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public java.lang.String getIdentifier() {
        return this.identifier;
    }
}
