package com.sap.bfx.security;

import com.sap.bfx.exception.NotAuthorizedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 *
 */
public interface SecurityService {

    /**
     *
     * @param token
     * @param role
     * @return
     */
    boolean isAuthorized(final AbstractAuthenticationToken token, final String role);

    /**
     *
     * @param token
     * @param role
     */
    void ensureAuthorized(final AbstractAuthenticationToken token, final String role) throws NotAuthorizedException;

    /**
     *
     * @param token
     * @param role
     */
    void ensureAuthorized(final AbstractAuthenticationToken token, final AbstractRoles role) throws NotAuthorizedException;

    /**
     *
     * @param token
     * @param roles
     */
    void ensureAnyAuthorized(final AbstractAuthenticationToken token, final String... roles) throws NotAuthorizedException;

    /**
     *
     * @param token
     * @param roles
     */
    void ensureAnyAuthorized(final AbstractAuthenticationToken token, final AbstractRoles... roles) throws NotAuthorizedException;
}
