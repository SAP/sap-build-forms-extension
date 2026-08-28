package com.sap.bfx.security;

import com.sap.bfx.definition.EventType;
import com.sap.bfx.exception.NotAuthorizedException;
import org.springframework.security.core.GrantedAuthority;

/**
 * SecurityService interface defines methods for ensuring authorization based on security sessions, event types,
 * and groups.
 */
public interface SecurityService {

    /**
     * Ensures that the provided security session is authorized for the specified event type and groups.
     *
     * @param appName                  the name of the application for which authorization is being checked
     * @param user
     * @param type                     the event type for which authorization is being checked
     * @param disableEnrichFormsGroups flag to disable enrichment of forms groups
     * @param sourceRowId              the source row ID associated with the authorization check
     * @param sourceKeys               the source keys associated with the authorization check
     */
    void ensureAuthorized(final String appName, final User user, final EventType type,
                          final boolean disableEnrichFormsGroups, final String sourceRowId, final String... sourceKeys)
            throws NotAuthorizedException;

    /**
     * Ensures that the provided security session is authorized for the specified event type and group.
     *
     * @param appName                  the name of the application for which authorization is being checked
     * @param user                     the user for which authorization is being checked
     * @param type                     the event type for which authorization is being checked
     * @param disableEnrichFormsGroups flag to disable enrichment of forms groups
     * @param authority
     */
    void ensureAuthorized(final String appName, final User user, final EventType type,
                          final boolean disableEnrichFormsGroups, final GrantedAuthority authority)
            throws NotAuthorizedException;

    /**
     * Ensures that the provided security session is authorized for at least one of the specified groups.
     *
     * @param appName                  the name of the application for which authorization is being checked
     * @param user                     the user for which authorization is being checked
     * @param type                     the event type for which authorization is being checked
     * @param disableEnrichFormsGroups flag to disable enrichment of forms groups
     * @param authorities              the array of authorities for which authorization is being checked
     */
    void ensureAnyAuthorized(final String appName, final User user, final EventType type,
                             final boolean disableEnrichFormsGroups, final GrantedAuthority... authorities)
            throws NotAuthorizedException;
}
