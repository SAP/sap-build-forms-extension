package com.sap.bfx.definition;

import com.sap.bfx.utils.Identifier;

public enum ButtonDesignType implements Identifier {
    Default("default"),
    Emphasized("emphasized"),
    Positive("positive"),
    Negative("negative"),
    Transparent("transparent"),
    Attention("attention");

    private final String identifier;

    ButtonDesignType(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}
