package com.sap.bfx.security;

import com.sap.bfx.exception.NotAuthorizedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Arrays;

public class PublicSecurityService implements SecurityService {
    /**
     * Evaluates if the given user is allowed to execute the given role
     *
     * @param token Security token
     * @param role  role that should be checked against
     * @return true if allowed, otherwise false
     */
    @Override
    public boolean isAuthorized(AbstractAuthenticationToken token, String role) {
        return true;
    }

    /**
     * checks if user has the current role, if not then a NotAuthorizedException is thrown
     *
     * @param token security token
     * @param role  role that should be checked against
     * @throws NotAuthorizedException
     */
    @Override
    public void ensureAuthorized(AbstractAuthenticationToken token, String role) throws NotAuthorizedException {
        if (!isAuthorized(token, role)) {
            var name = "";
            try {
                name = token.getName();
            } catch (Exception ignore) {
            }
            throw new NotAuthorizedException(null, new String[]{role}, name);
        }
    }

    /**
     * checks if user has the current role, if not then a NotAuthorizedException is thrown
     *
     * @param token security token
     * @param role  role that should be checked against
     * @throws NotAuthorizedException
     */
    @Override
    public void ensureAuthorized(AbstractAuthenticationToken token, AbstractRoles role) throws NotAuthorizedException {
        ensureAuthorized(token, role.getValue());
    }

    /**
     * checks if user has at least one of the roles, if not then a NotAuthorizedException is thrown
     *
     * @param token security token
     * @param roles roles that should be checked against
     * @throws NotAuthorizedException
     */
    @Override
    public void ensureAnyAuthorized(AbstractAuthenticationToken token, String... roles) throws NotAuthorizedException {
        if (Arrays.stream(roles).noneMatch(r -> isAuthorized(token, r))) {
            var name = "";
            try {
                name = token.getName();
            } catch (Exception ignore) {
            }
            throw new NotAuthorizedException(null, roles, name);
        }
    }

    /**
     * checks if user has at least one of the roles, if not then a NotAuthorizedException is thrown
     *
     * @param token security token
     * @param roles roles that should be checked against
     * @throws NotAuthorizedException
     */
    @Override
    public void ensureAnyAuthorized(AbstractAuthenticationToken token, AbstractRoles... roles) throws NotAuthorizedException {
        ensureAnyAuthorized(token, Arrays.stream(roles).map(AbstractRoles::getValue).toArray(String[]::new));
    }
}
