package com.sap.bfx.definition;

import com.sap.bfx.utils.Identifier;

public enum ValidationRuleType implements Identifier {

    MIN(Constants.VALIDATION_TYPE_MIN),
    MAX(Constants.VALIDATION_TYPE_MAX),
    REGEX(Constants.VALIDATION_TYPE_REGEX),
    SPEL(Constants.VALIDATION_TYPE_SPEL),
    BEAN(Constants.VALIDATION_TYPE_BEAN),
    FIXED(Constants.VALIDATION_TYPE_FIXED);

    private final String identifier;

    ValidationRuleType(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}
