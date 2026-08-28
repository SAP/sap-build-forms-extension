package com.sap.bfx.security.ias;

import com.sap.bfx.security.Constants;
import com.sap.bfx.security.SecuritySessionFactory;
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
class IasAuthSuccessHandler extends AbstractCookieHandler implements AuthenticationSuccessHandler {

    private final RedisRequestCache requestCache;
    private final SecuritySessionService securitySessionService;
    private final SecuritySessionFactory securitySessionFactory;

    /**
     * Constructs a new IasAuthSuccessHandler with the provided RedisRequestCache, SecuritySessionService, and
     * SecuritySessionFactory.
     *
     * @param requestCache           the RedisRequestCache for storing and retrieving original request URLs
     * @param securitySessionService the SecuritySessionService for managing security sessions
     * @param securitySessionFactory the SecuritySessionFactory for creating SecuritySession objects from JWT tokens
     */
    @Autowired
    public IasAuthSuccessHandler(final RedisRequestCache requestCache,
                                 final SecuritySessionService securitySessionService,
                                 final SecuritySessionFactory securitySessionFactory) {
        this.requestCache = requestCache;
        this.securitySessionService = securitySessionService;
        this.securitySessionFactory = securitySessionFactory;
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
            final var session = securitySessionFactory.create(jwtToken);
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
