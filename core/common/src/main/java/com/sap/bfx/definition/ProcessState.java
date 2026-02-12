package com.sap.bfx.definition;

import com.sap.bfx.utils.Identifier;

public enum ProcessState implements Identifier {
    Draft("0"),
    Submitted("10"),
    Running("20"),
    Cancelled("90"),
    Finished("100");

    private final String identifier;

    /**
     * @param identifier
     */
    private ProcessState(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @return
     */
    @Override
    public String getIdentifier() {
        return this.identifier;
    }
}
