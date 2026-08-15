package com.sap.bfx.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.security.Principal;

/**
 * Utility class for security-related operations.
 */
public final class SecurityUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private SecurityUtils() {
    }

    /**
     * Get the principal from the authentication token.
     *
     * @param token the authentication token
     * @return the principal or null if not found
     */
    public static <T> T getPrinciapl(final AbstractAuthenticationToken token, Class<T> clazz) {
        if (token != null && token instanceof UsernamePasswordAuthenticationToken) {
            if (token.getPrincipal() instanceof Jwt jwt && clazz.isAssignableFrom(Jwt.class)) {
                return (T) jwt;
            }
            if (token.getPrincipal() instanceof Principal principal && clazz.isAssignableFrom(Principal.class)) {
                return (T) principal;
            }
        }

        return null;
    }

    /**
     * Get the username from the authentication token.
     *
     * @param token the authentication token
     * @return the username or null if not found
     */
    public static String getUsername(final AbstractAuthenticationToken token) {
        if (token != null && token instanceof UsernamePasswordAuthenticationToken) {
            if (token.getPrincipal() instanceof Jwt jwt) {
                return jwt.getSubject();
            }
            if (token.getPrincipal() instanceof Principal principal) {
                return principal.getName();
            }
        }

        return null;
    }
}