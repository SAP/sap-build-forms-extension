package com.sap.bfx.p13n.model;

public enum P13NDbType {

    POSTGRES("postgres"),
    HANA("hana");

    private final String value;

    P13NDbType(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static P13NDbType fromValue(String value) {
        for (P13NDbType c : P13NDbType.values()) {
            if (c.value.equals(value)) {
                return c;
            }
        }
        return valueOf(value);
    }
}
