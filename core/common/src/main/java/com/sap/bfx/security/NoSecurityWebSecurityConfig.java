package com.sap.bfx.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the application when no security is enabled.
 *
 * <p>This class configures the application to allow all requests without authentication or authorization.
 * It is used when the NoSecurityCondition is met, indicating that security features are disabled.
 */
@Conditional(NoSecurityCondition.class)
@Configuration
@EnableWebSecurity
public class NoSecurityWebSecurityConfig {

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
        http.authorizeHttpRequests(authorize -> authorize.anyRequest().permitAll());
// @formatter:on
        return http.build();
    }

}
