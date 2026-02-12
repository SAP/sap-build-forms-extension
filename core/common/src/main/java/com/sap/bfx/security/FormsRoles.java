package com.sap.bfx.security;

import org.apache.commons.lang3.Strings;

import java.util.Arrays;

public enum FormsRoles implements AbstractRoles {

    StartProcess("StartProcess"), ParticipateProcess("ParticipateProcess"), SearchProcess("SearchProcess"),
    SeeAfterStart("SeeAfterStart"), TechnicalOwner("TechnicalOwner"), BusinessOwner("BusinessOwner"),
    FireFighter("FireFighter");

    private final String value;

    FormsRoles(String value) {
        this.value = value;
    }

    @Override public String getValue() {
        return value;
    }

    @Override public AbstractRoles fromValue(String value) {
        return Arrays.stream(FormsRoles.values()).filter(it -> Strings.CI.equals(it.getValue(), value)).findFirst()
                     .orElseThrow();
    }
}
