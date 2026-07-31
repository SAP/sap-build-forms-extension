package com.sap.bfx.security;

public interface Constants {

    String AUTH_TYPE_NONE = "none";
    String AUTH_TYPE_OIDC = "oidc";

    String SECURITY_SESSION_COOKIE_NAME = "SECS";
    String AUTH_REQUEST_COOKIE_NAME = "AUTH_REQUEST";
    String OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME = "OAUTH2_AUTH_REQUEST";

    int TEMP_TIMEOUT = 180; // 2 minutes
}
