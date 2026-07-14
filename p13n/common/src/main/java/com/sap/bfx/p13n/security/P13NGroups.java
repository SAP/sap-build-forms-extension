package com.sap.bfx.p13n.security;

import com.sap.bfx.security.AbstractGroups;
import org.apache.commons.lang3.Strings;

import java.util.Arrays;

public enum P13NGroups implements AbstractGroups {

    SBFX_P13NEdit("SBFX_P13NEdit"),
    SBFX_P13NDisplay("SBFX_P13NDisplay"),
    SBFX_P13NEnduser("SBFX_P13NEnduser"),
    SBFX_P13NGrpcUsage("SBFX_P13NGrpcUsage");

    private final String value;

    P13NGroups(String value) {
        this.value = value;
    }

    public static P13NGroups fromValue(String value) {
        return Arrays.stream(P13NGroups.values()).filter(it -> Strings.CI.equals(
                it.getValue(), value)).findFirst().orElseThrow();
    }

    @Override
    public String getValue() {
        return value;
    }
}
