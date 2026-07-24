package com.sap.bfx.valuehelp.model;

public enum ValueHelpDbType {

    POSTGRES("postgres"),
    HANA("hana");

    private final String value;

    ValueHelpDbType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static ValueHelpDbType fromValue(String value) {
        for (ValueHelpDbType c : ValueHelpDbType.values()) {
            if (c.value.equals(value)) {
                return c;
            }
        }
        return valueOf(value);
    }
}
