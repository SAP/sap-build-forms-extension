package com.sap.bfx.security;

public interface AbstractRoles {

    String getValue();

    AbstractRoles fromValue(String value);
}
