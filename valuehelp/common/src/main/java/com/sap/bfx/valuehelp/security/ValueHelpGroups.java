package com.sap.bfx.valuehelp.security;

import com.sap.bfx.security.AbstractGroups;
import org.apache.commons.lang3.Strings;

import java.util.Arrays;

public enum ValueHelpGroups implements AbstractGroups {

    SBFX_ValueHelpEdit("SBFX_ValueHelpEdit"),
    SBFX_ValueHelpDisplay("SBFX_ValueHelpDisplay"),
    SBFX_ValueHelpGrpcUsage("SBFX_ValueHelpGrpcUsage");

    private final String value;

    ValueHelpGroups(String value) {
        this.value = value;
    }

    public static ValueHelpGroups fromValue(String value) {
        return Arrays.stream(ValueHelpGroups.values()).filter(it -> Strings.CI.equals(
                it.getValue(), value)).findFirst().orElseThrow();
    }

    @Override
    public String getValue() {
        return value;
    }
}
