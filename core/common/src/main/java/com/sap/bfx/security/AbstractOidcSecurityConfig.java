package com.sap.bfx.security;

import com.sap.bfx.security.ias.IasGroupsAuthoritiesConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.List;

/**
 * Spring Security configuration for the IAS / OIDC resource server.
 *
 * <h2>Authentication flow</h2>
 * <ol>
 *   <li>A client obtains a JWT access token from SAP IAS using OIDC Authorization Code
 *       or Client Credentials flow.</li>
 *   <li>The client sends the token as a {@code Bearer} token in the
 *       {@code Authorization} header.</li>
 *   <li>Spring Security validates the token:
 *       <ul>
 *         <li>Signature – fetched JWKS from
 *             {@code {ias.url}/oauth2/certs}</li>
 *         <li>Expiry, not-before, issuer</li>
 *         <li>Audience – must contain the application's IAS client ID</li>
 *       </ul>
 *   </li>
 *   <li>IAS {@code groups} claim is mapped to Spring Security roles
 *       (e.g. group {@code admin} → {@code ROLE_admin}).</li>
 * </ol>
 *
 * <h2>Authorization rules</h2>
 * <ul>
 *   <li>All paths             – requires a valid JWT</li>
 * </ul>
 */
public abstract class AbstractOidcSecurityConfig {

    protected final String iasUrl;
    protected final String iasClientId;
    protected final boolean iasGroupLookup;

    /**
     * SCIM-based authorities converter.
     * Reads groups from the IAS SCIM API at runtime instead of from the JWT token.
     */
    protected final IasGroupsAuthoritiesConverter iasGroupsAuthoritiesConverter;

    protected AbstractOidcSecurityConfig(final String iasUrl, final String iasClientId, final boolean iasGroupLookup, final IasGroupsAuthoritiesConverter iasGroupsAuthoritiesConverter) {
        this.iasUrl = iasUrl;
        this.iasClientId = iasClientId;
        this.iasGroupLookup = iasGroupLookup;
        this.iasGroupsAuthoritiesConverter = iasGroupsAuthoritiesConverter;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Security Filter Chain
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Configures the HTTP security filter chain.
     *
     * <p>The application is a stateless REST API – no HTTP sessions are created.
     * CSRF protection is disabled because REST clients use tokens, not cookies.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            // ── Stateless REST – no sessions ──────────────────────────────────
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── CSRF: disabled for stateless token-based API ─────────────────
            .csrf(AbstractHttpConfigurer::disable)

            // ── Authorization rules ───────────────────────────────────────────
            .authorizeHttpRequests(registry -> registry
                    .requestMatchers("/**").authenticated()
                    .anyRequest().denyAll()
            )

            // ── OAuth2 Resource Server – JWT validation ───────────────────────
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            ).build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // JWT Decoder
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Configures the JWT decoder that:
     * <ol>
     *   <li>Fetches IAS public keys via OIDC discovery
     *       ({@code {iasUrl}/.well-known/openid-configuration → jwks_uri})</li>
     *   <li>Validates issuer, expiry, and not-before (standard validators)</li>
     *   <li>Validates the {@code aud} claim to ensure the token targets this
     *       specific application.</li>
     * </ol>
     *
     * <p><b>Note:</b> If the SAP Cloud Security auto-configuration
     * ({@code resourceserver-security-spring-boot-autoconfigure}) is active and
     * detects a service binding, it replaces this bean with its own
     * {@code HybridJwtDecoder}. This bean therefore acts as a fallback for
     * environments where the binding is configured via plain environment
     * variables only.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Build decoder using OIDC Discovery (fetches JWKS URI automatically)
        NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withIssuerLocation(iasUrl)
            .build();

        // ── Audience validator: token must target this application ──────────
        OAuth2TokenValidator<Jwt> audienceValidator = new JwtClaimValidator<List<String>>(
            JwtClaimNames.AUD,
            aud -> aud != null && aud.contains(iasClientId)
        );

        // ── Combine standard validators with audience validator ─────────────
        OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(iasUrl),
            audienceValidator
        );

        decoder.setJwtValidator(combinedValidator);
        return decoder;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // JWT → Spring Security Authority Mapping
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * <p> In case iasGroupLookup is activated than the SCIM-based authorities converter will be used.
     * In that case the groups are not read from the JWT claim but are queried live from IAS.
     *
     * <p> In case iasGroupLookup is deactivated the converter maps IAS JWT claims to Spring Security
     * {@link org.springframework.security.core.GrantedAuthority} objects.
     *  IAS encodes group memberships in the {@code groups} claim as a JSON
     * array of strings. This converter reads that claim and adds a {@code ROLE_}
     * prefix so that Spring's {@code hasRole("admin")} expressions work correctly.
     *
     * <p>Example mapping:
     * <pre>
     *   JWT claim: "groups": ["SBFX_StartProcess", "SBFX_ParticipateProcess"]
     *   Spring authorities: [ROLE_SBFX_StartProcess, ROLE_SBFX_ParticipateProcess]
     * </pre>
     *
     * <p>If you also want to expose OAuth2 scopes, add a second
     * {@link JwtGrantedAuthoritiesConverter} for the {@code scope} claim
     * and combine them with a
     * {@link org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter}.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        if (iasGroupLookup) {
            JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
            converter.setJwtGrantedAuthoritiesConverter(iasGroupsAuthoritiesConverter);
            return converter;
        } else {
            JwtGrantedAuthoritiesConverter groupsConverter = new JwtGrantedAuthoritiesConverter();
            // IAS stores group memberships in the "groups" claim
            groupsConverter.setAuthoritiesClaimName("groups");
            // Standard Spring Security role prefix – enables hasRole("SBFX_StartProcess") expressions
            groupsConverter.setAuthorityPrefix("ROLE_");

            JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
            converter.setJwtGrantedAuthoritiesConverter(groupsConverter);
            return converter;
        }

    }
}
