package com.sap.bfx.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * Security configuration for the application when no security is enabled.
 *
 * <p>This class configures the application to allow all requests without authentication or authorization.
 * It is used when the NoSecurityCondition is met, indicating that security features are disabled.
 *
 * <p>A dev user is injected into the security context for every request so that
 * {@link SecurityUtils#getUserName()} returns a realistic value in local development.
 * Configure the user via the {@code bfx.dev.user} property (default: {@code dev.user@sap.com}).
 */
@Conditional(NoSecurityCondition.class)
@Configuration
@EnableWebSecurity
public class NoSecurityWebSecurityConfig {

    /** Email of the simulated local-dev user. */
    @Value("${bfx.dev.user:dev.user@sap.com}")
    private String devUser;

    /**
     * Configures the security filter chain to allow all requests without authentication or authorization.
     *
     * @param http the HttpSecurity object used to configure security settings
     * @return the configured SecurityFilterChain
     * @throws Exception if an error occurs while configuring the security filter chain
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
// @formatter:off
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll())
            .addFilterBefore(devUserFilter(), UsernamePasswordAuthenticationFilter.class);
// @formatter:on
        return http.build();
    }

    /**
     * Creates a filter that injects a simulated {@link SecuritySession} into the Spring Security context
     * for every request. This makes {@link SecurityUtils#getUserName()} return a non-empty value so that
     * user-based filtering and database writes (changedBy, started_by, etc.) work locally.
     */
    private OncePerRequestFilter devUserFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                            @Nonnull HttpServletResponse response,
                                            @Nonnull FilterChain filterChain) throws ServletException, IOException {
                final var auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
                    final var user = new User();
                    user.setUserName(devUser);
                    user.setAuthorities(Collections.emptyList());

                    final var session = new SecuritySession("dev-session", user, null, null);
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(user, session, user.getAuthorities()));
                }
                filterChain.doFilter(request, response);
            }
        };
    }

}
