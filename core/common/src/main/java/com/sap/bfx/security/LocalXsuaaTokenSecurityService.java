package com.sap.bfx.security;

import com.sap.bfx.exception.NotAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;

/**
 *
 */
@Slf4j
public class LocalXsuaaTokenSecurityService implements SecurityService {
    /**
     * Evaluates if the given user is allowed to execute the given role
     *
     * @param token Security token
     * @param role  role that should be checked against
     * @return true if allowed, otherwise false
     */
    @Override
    public boolean isAuthorized(AbstractAuthenticationToken token, String role) {
        // if there is no token or xsappname not filled then we deny access
        if (token == null) {
            log.debug("isAuthorized called with token '{}'", token);
            return false;
        }

        log.debug("Token is {}", (token == null) ? "not available" : token.toString());
        log.debug("Credentials ('{}') = {}", token.getCredentials().getClass().getName(),
                token.getCredentials().toString());

        final var jwt = (Jwt) token.getCredentials();
        final var scopes = jwt.getClaimAsStringList("scope");

        final var authorized = scopes.stream().anyMatch(scope -> StringUtils.endsWith(scope, "." + role));
        log.debug("User '{}' is {}", jwt.getClaim("user_uuid"), authorized ? "authorized" : "NOT authorized");

        return authorized;
    }

    /**
     * checks if user has the current tole, if not then a NotAuthorizedException is thrown
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
     * checks if user has the current tole, if not then a NotAuthorizedException is thrown
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
