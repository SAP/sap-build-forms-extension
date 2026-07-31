package com.sap.bfx.security.oidc;

import com.sap.bfx.security.Constants;
import com.sap.bfx.utils.AbstractCookieHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * An AuthenticationSuccessHandler implementation that takes the JWT Token returned by SAP IAS after successful
 * authentication and stores it in a HttpOnly cookie for subsequent requests. It also handles redirecting the
 * user to their original destination after login.
 */
@Component
class OidcAuthSuccessHandler extends AbstractCookieHandler implements AuthenticationSuccessHandler {

    private final RedisRequestCache requestCache;
    private final SecuritySessionService securitySessionService;

    /**
     * Constructor for IasAuthSuccessHandler.
     *
     * @param requestCache the CookieRequestCache to retrieve saved requests
     */
    @Autowired
    public OidcAuthSuccessHandler(RedisRequestCache requestCache, SecuritySessionService securitySessionService) {
        this.requestCache = requestCache;
        this.securitySessionService = securitySessionService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        // Generate your JWT string based on the 'authentication' principal object here
        String jwtToken = null;

        if (authentication.getPrincipal() instanceof OidcUser oidcUser) {
            // This is the raw signed JWT token string generated directly by SAP IAS
            jwtToken = oidcUser.getIdToken().getTokenValue();
        }

        if (jwtToken != null) {
            final var session = securitySessionService.createSession(jwtToken);
            securitySessionService.saveSession(session);
            // now store the id in a cookie for subsequent requests
            final var timeout = Duration.between(Instant.now(), session.getToken().getExpiresAt());
            timeout.minus(Duration.ofSeconds(2));

            addCookie(request, response, Constants.SECURITY_SESSION_COOKIE_NAME, session.getId(), true, true,
                    Long.valueOf(timeout.toSeconds()).intValue());
        }

        // Extract original destination from your custom cookie request cache
        final var savedRequest = requestCache.getRequest(request, response);
        final var targetUrl = (savedRequest != null) ? savedRequest.getRedirectUrl() : "/";
        requestCache.removeRequest(request, response);
        response.sendRedirect(targetUrl);
    }
}
