package com.sap.bfx.security.ias;

import com.sap.bfx.config.IasConnectionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.List;

/**
 * Configuration class for JWT decoding and validation, as well as mapping JWT claims to Spring Security authorities.
 * <p>
 * This class sets up a {@link JwtDecoder} that validates JWT tokens issued by the configured OIDC provider (IAS),
 * including audience validation. It also configures a {@link JwtAuthenticationConverter} to map IAS JWT claims
 * to Spring Security {@link org.springframework.security.core.GrantedAuthority} objects.
 */
@Configuration
@Slf4j
public class JwtConfig {

    private final IasConnectionConfig idpConfig;

    /**
     * Constructs a JwtConfig with the provided OIDC configuration.
     *
     * @param idpConfig the OIDC configuration containing issuer URI and client ID
     */
    @Autowired
    public JwtConfig(IasConnectionConfig idpConfig) {
        log.debug("Setting IDP to '{}'", idpConfig);
        this.idpConfig = idpConfig;
    }

    /**
     * Configures a JwtDecoder that validates JWT tokens issued by the configured OIDC provider (IAS).
     * It includes audience validation to ensure the token is intended for this application.
     *
     * @return JwtDecoder instance
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Build decoder using OIDC Discovery (fetches JWKS URI automatically)
        final var decoder = NimbusJwtDecoder.withIssuerLocation(idpConfig.getUrl()).build();

        // ── Audience validator: token must target this application ──────────
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
                aud -> aud != null && aud.contains(idpConfig.getOidcClientId()));

        // ── Combine standard validators with audience validator ─────────────
        OAuth2TokenValidator<Jwt> combinedValidator =
                new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(idpConfig.getUrl()),
                        audienceValidator);

        decoder.setJwtValidator(combinedValidator);
        return decoder;
    }

    /**
     * Maps IAS JWT claims to Spring Security {@link org.springframework.security.core.GrantedAuthority} objects.
     *
     * <p>IAS encodes group memberships in the {@code groups} claim as a JSON
     * array of strings. This converter reads that claim and adds a {@code ROLE_}
     * prefix so that Spring's {@code hasRole("d051677srvRuntime_admin")} expressions work correctly.
     *
     * <p>Example mapping:
     * <pre>
     *   JWT claim: "groups": ["d051677srvRuntime_admin", "viewer"]
     *   Spring authorities: [ROLE_d051677srvRuntime_admin, ROLE_viewer]
     * </pre>
     *
     * <p>If you also want to expose OAuth2 scopes, add a second
     * {@link JwtGrantedAuthoritiesConverter} for the {@code scope} claim
     * and combine them with a
     * {@link JwtGrantedAuthoritiesConverter}.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter groupsConverter = new JwtGrantedAuthoritiesConverter();
        // IAS stores group memberships in the "groups" claim
        groupsConverter.setAuthoritiesClaimName("groups");
        // Standard Spring Security role prefix – enables hasRole("xxxRuntime_admin") expressions
        groupsConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(groupsConverter);
        return converter;
    }
}
