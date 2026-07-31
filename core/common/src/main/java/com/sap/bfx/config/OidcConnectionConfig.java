package com.sap.bfx.config;

import lombok.Getter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * OidcConfiguration class holds the configuration properties for OpenID Connect (OIDC) authentication.
 * It retrieves the issuer URI, client secret, and client ID from the application properties.
 */
@Configuration
@Getter
@ToString
public class OidcConnectionConfig {

    @Value("${forms.security.oidc.issuerUri:#{null}}")
    private String issuerUri;
    @Value("${forms.security.oidc.clientSecret:#{null}}")
    private String clientSecret;
    @Value("${forms.security.oidc.clientId:#{null}}")
    private String clientId;

}
