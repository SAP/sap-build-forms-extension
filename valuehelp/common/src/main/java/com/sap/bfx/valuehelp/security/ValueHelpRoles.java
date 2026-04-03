package com.sap.bfx.valuehelp.security;

import com.sap.bfx.security.AbstractRoles;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum ValueHelpRoles implements AbstractRoles {

    ValueHelpEdit("ValueHelpEdit"),
    ValueHelpDisplay("ValueHelpDisplay");

    private final String value;

    ValueHelpRoles(String value) {
        this.value = value;
    }

    public static ValueHelpRoles fromValue(String value) {
        return Arrays.stream(ValueHelpRoles.values()).filter(it -> StringUtils.equalsIgnoreCase(
                it.getValue(), value)).findFirst().orElseThrow();
    }

    @Override
    public String getValue() {
        return value;
    }
}
