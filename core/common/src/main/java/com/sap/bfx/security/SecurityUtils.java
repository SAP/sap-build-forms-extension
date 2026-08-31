package com.sap.bfx.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.security.Principal;

/**
 * Utility class for security-related operations.
 */
public final class SecurityUtils {

    public static final String SLASH = "/";

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

    /**
     * Simplifies a principal name by extracting the substring after the last slash.
     *
     * @param principalName the principal name to simplify
     * @return the simplified principal name
     */
    public static String getSimplifiedPrincipalName(final String principalName) {
        if (!principalName.contains(SLASH)) {
            return principalName;
        }
        return principalName.substring(principalName.lastIndexOf(SLASH) + 1);
    }

    /**
     * Retrieves the SecuritySession from the current security context.
     *
     * @return the SecuritySession
     */
    public static SecuritySession getSecuritySession() {
        final var credentials = SecurityContextHolder.getContext().getAuthentication().getCredentials();
        if (credentials instanceof SecuritySession securitySession) {
            return securitySession;
        }
        // we don't have a real security session, but we still want to return a valid object, so we create a
        // new one with a dummy user name
        return SecuritySession.createDummy();
    }
}