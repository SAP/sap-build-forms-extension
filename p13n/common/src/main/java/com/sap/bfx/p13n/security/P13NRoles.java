package com.sap.bfx.p13n.security;

import com.sap.bfx.security.AbstractRoles;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum P13NRoles implements AbstractRoles {

    P13NEdit("P13NEdit"),
    P13NDisplay("P13NDisplay"),
    P13NEnduser("P13NEnduser"),
    P13NUsage("P13NUsage");

    private final String value;

    P13NRoles(String value) {
        this.value = value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public P13NRoles fromValue(String value) {
        return Arrays.stream(P13NRoles.values()).filter(it -> StringUtils.equalsIgnoreCase(
                it.getValue(), value)).findFirst().orElseThrow();
    }
}
