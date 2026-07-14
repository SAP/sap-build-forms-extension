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
     * @param disableEnrichFormsGroups
     * @param sourceRowId
     * @param sourceKeys
     * @return
     */
    void ensureAuthorized(final AbstractAuthenticationToken token, final EventType type, final Boolean disableEnrichFormsGroups, final String sourceRowId, final String... sourceKeys) throws NotAuthorizedException;

    /**
     *
     * @param token
     * @param type
     * @param disableEnrichFormsGroups
     * @param group
     */
    void ensureAuthorized(final AbstractAuthenticationToken token, final EventType type, final Boolean disableEnrichFormsGroups, final AbstractGroups group) throws NotAuthorizedException;

    /**
     *
     * @param token
     * @param type
     * @param disableEnrichFormsGroups
     * @param groups
     */
    void ensureAnyAuthorized(final AbstractAuthenticationToken token, final EventType type, final Boolean disableEnrichFormsGroups, final AbstractGroups... groups) throws NotAuthorizedException;
}
