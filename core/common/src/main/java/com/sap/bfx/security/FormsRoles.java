package com.sap.bfx.security;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public enum FormsRoles implements AbstractRoles {

    StartProcess("StartProcess"),
    ParticipateProcess("ParticipateProcess"),
    SearchProcess("SearchProcess"),
    SeeAfterStart("SeeAfterStart"),
    TechnicalOwner("TechnicalOwner"),
    BusinessOwner("BusinessOwner"),
    FireFighter("FireFighter");

    private final String value;

    FormsRoles(String value) {
        this.value = value;
    }

    public static AbstractRoles fromValue(String value) {
        return Arrays.stream(FormsRoles.values()).filter(it -> StringUtils.equalsIgnoreCase(
                it.getValue(), value)).findFirst().orElseThrow();
    }

    @Override
    public String getValue() {
        return value;
    }
}
