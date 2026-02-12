package com.sap.bfx.security;

import com.auth0.jwt.JWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Slf4j
public class LocalXsuaaTokenAuthenticationFilter extends OncePerRequestFilter {

    private final static String[] XSUAA_JWT_HEADERS = ArrayUtils.toArray("alg", "jku",
            "kid", "typ", "jid");

    private final String resourceName;

    /**
     * @param resourceName
     */
    public LocalXsuaaTokenAuthenticationFilter(final String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        var authHeader = request.getHeader("Authorization");

        // First try, if no bearer token could be found, try to load the one from given resource
        if ((authHeader == null || !authHeader.startsWith("Bearer ")) && StringUtils.isNotBlank(resourceName)) {
            if (StringUtils.equalsIgnoreCase(resourceName, "ENV")) {
                authHeader = "Bearer " + System.getenv("FORMS_LOCAL_TOKEN");
            } else {
                try (var is = this.getClass().getClassLoader().getResourceAsStream(resourceName)) {
                    final String jwtToken = IOUtils.toString(is, StandardCharsets.UTF_8);
                    log.debug("JWT Token injected '{}'", jwtToken);
                    authHeader = "Bearer " + jwtToken;
                }
            }
        }

        // if still no bearer token could be found, then skip any further action to authenticate user
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final var token = JWT.decode(StringUtils.trim(authHeader.substring(7)));
            final var subject = token.getSubject();

            final var authentication = SecurityContextHolder.getContext().getAuthentication();

            final var jwtBuilder = Jwt.withTokenValue(token.getToken());
            token.getClaims().keySet().forEach(it -> jwtBuilder.claim(it, token.getClaim(it).as(Object.class)));
            Arrays.stream(XSUAA_JWT_HEADERS).forEach(it -> {
                final var claim = token.getHeaderClaim(it);
                if (!claim.isNull()) {
                    jwtBuilder.header(it, claim.as(Object.class));
                }
            });
            // set some value explicit to avoid wrong data-types
            jwtBuilder.subject(token.getSubject())
                    .expiresAt(token.getExpiresAtAsInstant())
                    .issuedAt(token.getIssuedAtAsInstant())
                    .issuer(token.getIssuer())
                    .jti(token.getId())
                    .notBefore(token.getNotBeforeAsInstant())
                    .subject(token.getSubject());

            if (subject != null && authentication == null) {
                final var user = User.builder().username(subject).password("").
                        build();

//                if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        user,
                        jwtBuilder.build(),
                        user.getAuthorities()
                );

                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
//            }

            filterChain.doFilter(new AuthHeaderServletRequestWrapper(request, authHeader), response);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     *
     */
    private static class AuthHeaderServletRequestWrapper extends HttpServletRequestWrapper {

        private final String authHeader;

        public AuthHeaderServletRequestWrapper(HttpServletRequest request, final String authHeader) {
            super(request);
            this.authHeader = authHeader;
        }

        /**
         * @param name
         * @return
         */
        @Override
        public String getHeader(String name) {
            if (StringUtils.equalsIgnoreCase("Authorization", name)) {
                return "Bearer " + authHeader;
            }
            return super.getHeader(name);
        }

//        /**
//         * @return
//         */
//        @Override
//        public Enumeration<String> getHeaderNames() {
//            var values = super.getHeaderNames();
//            var found = false;
//            var l = new LinkedList<String>();
//
//            for (var it = values.asIterator(); it.hasNext(); ) {
//                var v = it.next();
//                found = found || StringUtils.equals("Authorization", v);
//                l.add(v);
//            }
//            if (!found) {
//                l.add("Authorization");
//            }
//            return Collections.enumeration(l);
//        }
    }
}
