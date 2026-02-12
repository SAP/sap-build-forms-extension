package com.sap.bfx.session;

import com.sap.bfx.utils.Identifier;

/**
 *
 */
public enum SortOrder implements Identifier {
    ASCENDING("a"),
    DESCENDING("d");

    private final String identifier;

    private SortOrder(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}