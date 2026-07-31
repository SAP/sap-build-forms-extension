package com.sap.bfx.security.oidc;

import com.sap.bfx.security.Constants;
import com.sap.bfx.utils.AbstractCookieHandler;
import com.sap.bfx.utils.SpringUtils;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * A filter that intercepts incoming HTTP requests to extract and validate JWT tokens from cookies.
 * If a valid token is found, it reconstructs the Spring Security Authentication context for the request.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request, @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {

        final var cookie = AbstractCookieHandler.getCookie(request, Constants.SECURITY_SESSION_COOKIE_NAME);
        final var id = (cookie != null) ? cookie.getValue() : null;

        if (StringUtils.isNotBlank(id)) {
            try {
                final var service = SpringUtils.getApplicationContext().getBean(SecuritySessionService.class);
                final var session = service.findSessionById(id);

                if (session != null && session.getToken() != null) {
                    // Reconstruct the Spring Security Authentication context for this request thread
                    SecurityContextHolder.getContext().setAuthentication(
                            new UsernamePasswordAuthenticationToken(session.getToken(), session,
                                    Collections.emptyList()));
                }
            } catch (Exception e) {
                // Handle token decoding/validation errors if necessary
                log.error("Failed to decode JWT token: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}