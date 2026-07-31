package com.sap.bfx.security.oidc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security configuration for the application.
 *
 * <p>This class configures OAuth2 login with an OpenID Connect (OIDC) provider (e.g., SAP IAS).
 * It sets up JWT decoding, audience validation, and maps JWT claims to Spring Security authorities.
 */
@Conditional(OidcEnabledCondition.class)
@Configuration
@EnableWebSecurity
@Slf4j
//@EnableMethodSecurity   // enables @PreAuthorize / @PostAuthorize on controller methods
public class OidcWebSecurityConfig {

    private final RedisAuthorizationRequestRepository authRequestRepo;
    private final RedisRequestCache requestCache;
    private final AuthenticationSuccessHandler successHandler;

    /**
     * Constructor for OidcSecurityConfig.
     *
     * @param securitySessionService the SecuritySessionService used to manage security sessions
     */
    @Autowired
    public OidcWebSecurityConfig(SecuritySessionService securitySessionService, RedisRequestCache requestCache,
                                 RedisAuthorizationRequestRepository authRequestRepo,
                                 AuthenticationSuccessHandler successHandler) {
        this.successHandler = successHandler;
        this.requestCache = requestCache;
        this.authRequestRepo = authRequestRepo;
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
//                        .csrf(csrf -> csrf.disable())
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
