package com.sap.bfx.definition;

import com.sap.bfx.utils.Identifier;

public enum InputType implements Identifier {

    Text("text"),
    Password("password"),
    Number("number"),
    Email("email"),
    Telephone("telephone");

    private final String identifier;

    private InputType(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}
