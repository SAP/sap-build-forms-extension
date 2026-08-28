package com.sap.bfx.security.ias;

import com.sap.bfx.config.IasConnectionConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.IdTokenClaimNames;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;

/**
 * Security configuration for the application.
 *
 * <p>This class configures OAuth2 login with an OpenID Connect (OIDC) provider (e.g., SAP IAS).
 * It sets up JWT decoding, audience validation, and maps JWT claims to Spring Security authorities.
 */
@Conditional(IasEnabledCondition.class)
@Configuration
@EnableWebSecurity
@Slf4j
//@EnableMethodSecurity   // enables @PreAuthorize / @PostAuthorize on controller methods
public class IasWebSecurityConfig {

    private final RedisAuthorizationRequestRepository authRequestRepo;
    private final RedisRequestCache requestCache;
    private final AuthenticationSuccessHandler successHandler;
    private final IasConnectionConfig idpConfig;

    /**
     * Constructor for OidcSecurityConfig.
     *
     * @param securitySessionService the SecuritySessionService used to manage security sessions
     */
    @Autowired
    public IasWebSecurityConfig(SecuritySessionService securitySessionService, RedisRequestCache requestCache,
                                RedisAuthorizationRequestRepository authRequestRepo,
                                AuthenticationSuccessHandler successHandler, IasConnectionConfig idpConfig) {
        this.successHandler = successHandler;
        this.requestCache = requestCache;
        this.authRequestRepo = authRequestRepo;
        this.idpConfig = idpConfig;
    }

    /**
     * Creates a client-registration with name "ias" that should point to an IAS for authentication and
     * authorization.
     *
     * @return ClientRegistration instance
     */
    @Bean
    public ClientRegistration iasClientRegistration() {
        final var result = ClientRegistration.withRegistrationId("ias").clientId(idpConfig.getOidcClientId())
                                             .clientSecret(idpConfig.getOidcClientSecret())
                                             .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                                             .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                                             .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                                             .scope("openid", "profile", "email")
                                             .authorizationUri(idpConfig.getUrl() + "/oauth2/authorize")
                                             .tokenUri(idpConfig.getUrl() + "/oauth2/token")
                                             .userInfoUri(idpConfig.getUrl() + "/oauth2/userinfo")
                                             .userNameAttributeName(IdTokenClaimNames.SUB).issuerUri(idpConfig.getUrl())
                                             .jwkSetUri(idpConfig.getUrl() + "/oauth2/certs").clientName("ias").build();

        log.debug("IAS Client Registration: '{}' with Issuer-Uri: '{}'", result, idpConfig.getUrl());
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

    /**
     * Configures the security filter chain for the application.
     *
     * @param http the HttpSecurity object to configure
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs during configuration
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
// @formatter:off
        http
                // enable CORS (Cross-Origin Resource Sharing) support
                .cors(Customizer.withDefaults())
                // Stateless REST – no sessions
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF: disabled for stateless token-based API
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new SpaCsrfTokenRequestHandler()))
//                        .disable())
                // Authorization rules
                .authorizeHttpRequests(
                        authz -> authz
                                .requestMatchers("/login/**", "/oauth2/**", "/actuator/**").permitAll()
                                .anyRequest().authenticated())
                // configuration of OAuth2 login
                .oauth2Login(config -> {
                    // this is a coockie based repository instead of the one that uses a session
                    config.authorizationEndpoint(endpoint -> endpoint
                            .authorizationRequestRepository(authRequestRepo));
                    // add a custom success handler that will store the JWT token in a cookie and redirect to
                    // the original URL
                    config.successHandler(successHandler);
                })
                // this is necessary to find the correct URL to redirect the request after authentication is done
                .requestCache(Customizer.withDefaults()).requestCache(config -> config
                        .requestCache(requestCache))

                // this filter reaads the JWT token from Cookie and uses it to provide user information
                .addFilterBefore(new JwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
// @formatter:on
    }
}
