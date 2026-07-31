package com.sap.bfx.security.oidc;

import com.sap.bfx.config.OidcConnectionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
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
public class JwtOidcConfig {

    private final OidcConnectionConfig idpConfig;

    /**
     * Constructs a JwtConfig with the provided OIDC configuration.
     *
     * @param idpConfig the OIDC configuration containing issuer URI and client ID
     */
    @Autowired
    public JwtOidcConfig(OidcConnectionConfig idpConfig) {
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
        final var decoder = NimbusJwtDecoder.withIssuerLocation(idpConfig.getIssuerUri()).build();

        // ── Audience validator: token must target this application ──────────
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(JwtClaimNames.AUD,
                aud -> aud != null && aud.contains(idpConfig.getClientId()));

        // ── Combine standard validators with audience validator ─────────────
        OAuth2TokenValidator<Jwt> combinedValidator =
                new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefaultWithIssuer(idpConfig.getIssuerUri()),
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

    // ─────────────────────────────────────────────────────────────────────────
    // Registration of IDP (e.g. IAS instance)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a client-registration with name "ias" that should point to an IAS for authentication and
     * authorization.
     *
     * @return ClientRegistration instance
     */
    @Bean
    public ClientRegistration iasClientRegistration() {
        final var result = ClientRegistration.withRegistrationId("ias").clientId(idpConfig.getClientId())
                                             .clientSecret(idpConfig.getClientSecret())
                                             .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                             .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                             .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                                             .scope("openid", "profile", "email")
                                             .authorizationUri(idpConfig.getIssuerUri() + "/oauth2/authorize")
                                             .tokenUri(idpConfig.getIssuerUri() + "/oauth2/token")
                                             .userInfoUri(idpConfig.getIssuerUri() + "/oauth2/userinfo")
                                             .userNameAttributeName(IdTokenClaimNames.SUB)
                                             .issuerUri(idpConfig.getIssuerUri())
                                             .jwkSetUri(idpConfig.getIssuerUri() + "/oauth2/certs").clientName("ias")
                                             .build();

        log.debug("IAS Client Registration: '{}' with Issuer-Uri: '{}'", result, idpConfig.getIssuerUri());
        return result;
    }

    /**
     * Creates an in-memory ClientRegistrationRepository that holds the IAS client registration.
     *
     * @return ClientRegistrationRepository instance
     */
    @Bean
    public ClientRegistrationRepository clientRegistrationRepository() {
        return new InMemoryClientRegistrationRepository(iasClientRegistration());
    }
}
