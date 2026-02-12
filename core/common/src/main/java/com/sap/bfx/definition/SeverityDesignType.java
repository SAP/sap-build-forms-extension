package com.sap.bfx.definition;

import com.sap.bfx.utils.Identifier;

public enum SeverityDesignType implements Identifier {
    Positive("positive"),
    Negative("negative"),
    Warn("warn"),
    Info("info");

    private final String identifier;

    private SeverityDesignType(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}
