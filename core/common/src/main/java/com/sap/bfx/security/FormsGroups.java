package com.sap.bfx.security;

import org.apache.commons.lang3.Strings;

import java.util.Arrays;

public enum FormsGroups implements AbstractGroups {

    SBFX_StartProcess("SBFX_StartProcess"),
    SBFX_ParticipateProcess("SBFX_ParticipateProcess"),
    SBFX_SearchProcess("SBFX_SearchProcess"),
    SBFX_SeeAfterStart("SBFX_SeeAfterStart"),
    SBFX_TechnicalOwner("SBFX_TechnicalOwner"),
    SBFX_BusinessOwner("SBFX_BusinessOwner"),
    SBFX_FireFighter("SBFX_FireFighter");

    private final String value;

    FormsGroups(String value) {
        this.value = value;
    }

    public static AbstractGroups fromValue(String value) {
        return Arrays.stream(FormsGroups.values()).filter(it -> Strings.CI.equals(
                it.getValue(), value)).findFirst().orElseThrow();
    }

    @Override
    public String getValue() {
        return value;
    }
}
