package com.sap.bfx.security;

import com.sap.bfx.definition.EventType;
import com.sap.bfx.exception.NotAuthorizedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 *
 */
public interface SecurityService {

    /**
     *
     * @param token
     * @param type
     * @param disableEnrichFormsRoles
     * @param sourceRowId
     * @param sourceKeys
     * @return
     */
    void ensureAuthorized(final AbstractAuthenticationToken token, final EventType type, final Boolean disableEnrichFormsRoles, final String sourceRowId, final String... sourceKeys) throws NotAuthorizedException;

    /**
     *
     * @param token
     * @param type
     * @param disableEnrichFormsRoles
     * @param role
     */
    void ensureAuthorized(final AbstractAuthenticationToken token, final EventType type, final Boolean disableEnrichFormsRoles, final AbstractRoles role) throws NotAuthorizedException;

    /**
     *
     * @param token
     * @param type
     * @param disableEnrichFormsRoles
     * @param roles
     */
    void ensureAnyAuthorized(final AbstractAuthenticationToken token, final EventType type, final Boolean disableEnrichFormsRoles, final AbstractRoles... roles) throws NotAuthorizedException;
}
