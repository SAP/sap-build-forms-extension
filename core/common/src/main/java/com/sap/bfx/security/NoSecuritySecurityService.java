package com.sap.bfx.security;

import com.sap.bfx.definition.EventType;
import com.sap.bfx.exception.NotAuthorizedException;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

/**
 * Implementation of the SecurityService interface for public security.
 * This service does not enforce any authorization checks and allows all users to access all event types.
 */
@Service
@Conditional(NoSecurityCondition.class)
public class NoSecuritySecurityService implements SecurityService {
    @Override
    public void ensureAuthorized(String appName, User user, EventType type, boolean disableEnrichFormsGroups,
                                 String sourceRowId, String... sourceKeys) throws NotAuthorizedException {

    }

    @Override
    public void ensureAuthorized(String appName, User user, EventType type, boolean disableEnrichFormsGroups,
                                 GrantedAuthority authority) throws NotAuthorizedException {

    }

    @Override
    public void ensureAnyAuthorized(String appName, User user, EventType type, boolean disableEnrichFormsGroups,
                                    GrantedAuthority... authorities) throws NotAuthorizedException {

    }
}
