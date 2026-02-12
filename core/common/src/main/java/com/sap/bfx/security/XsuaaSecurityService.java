package com.sap.bfx.security;

import com.sap.bfx.exception.NotAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Arrays;

@Slf4j
public class XsuaaSecurityService implements SecurityService {

    private final String xsAppName;

    /**
     * @param xsAppName
     */
    public XsuaaSecurityService(final String xsAppName) {
        this.xsAppName = xsAppName;
    }

    /**
     * Evaluates if the given user is allowed to execute the given role
     *
     * @param token Security token
     * @param role  role that should be checked against
     * @return true if allowed, otherwise false
     */
    @Override
    public boolean isAuthorized(final AbstractAuthenticationToken token, final String role) {
        // if there is no token or xsappname not filled then we deny access
        if (token == null || StringUtils.isBlank(xsAppName)) {
            log.error("isAuthorized called with token={} and xsAppName={}", token, xsAppName);
            return false;
        }

        log.debug("Token is {}", (token == null) ? "not available" : token.toString());
        log.debug("Credentials ('{}') = {}", token.getCredentials().getClass().getName(),
                token.getCredentials().toString());

        final var jwt = (Jwt) token.getCredentials();
        log.debug("Token-Value: '{}'", jwt.getTokenValue());

        // jwt.getClaims().keySet().forEach(key -> log.debug("Claim {} = {}", key,
        // jwt.getClaimAsString(key)));

        final var scopes = jwt.getClaimAsStringList("scope");
        log.debug("Scopes are '{}'", scopes);

        final var authorized = scopes.stream().anyMatch(scope ->
                StringUtils.equals(xsAppName + "." + role, scope));
        log.debug("User '{}' is {}", jwt.getClaim("user_uuid"), authorized ? "authorized" : "NOT authorized");

        return authorized;
    }

    /**
     * checks if user has the current role, if not then a NotAuthorizedException is thrown
     *
     * @param token security token
     * @param role  role that should be checked against
     * @throws NotAuthorizedException
     */
    @Override
    public void ensureAuthorized(final AbstractAuthenticationToken token, final String role)
            throws NotAuthorizedException {
        if (!isAuthorized(token, role)) {
            var name = "";
            try {
                name = token.getName();
            } catch (Exception ignore) {
            }
            throw new NotAuthorizedException(xsAppName, new String[]{role}, name);
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
            throw new NotAuthorizedException(xsAppName, roles, name);
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
