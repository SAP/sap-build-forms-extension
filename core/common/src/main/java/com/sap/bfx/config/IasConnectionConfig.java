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
public class IasConnectionConfig {

    @Value("${forms.security.ias.url:#{null}}")
    private String url;
    @Value("${forms.security.ias.oidc.clientSecret:#{null}}")
    private String oidcClientSecret;
    @Value("${forms.security.ias.oidc.clientId:#{null}}")
    private String oidcClientId;
    @Value("${forms.security.ias.scim.clientId:#{null}}")
    private String scimClientId;
    @Value("${forms.security.ias.scim.clientSecret:#) {null}}")
    private String scimClientSecret;

}
