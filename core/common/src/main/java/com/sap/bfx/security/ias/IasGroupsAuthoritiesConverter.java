package com.sap.bfx.security.ias;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;

/**
 * Converts a validated IAS JWT into Spring Security {@link GrantedAuthority} objects
 * by querying the user's current group memberships <b>live from the IAS SCIM API</b>.
 *
 * <h2>Difference from the token-based approach</h2>
 * <table>
 *   <tr><th>Token Claims</th><th>SCIM Lookup (this converter)</th></tr>
 *   <tr><td>Groups embedded once upon token issuance</td>
 *       <td>Groups queried fresh upon each authentication (cached)</td></tr>
 *   <tr><td>Group changes take effect only with the next token</td>
 *       <td>Group changes take effect within the cache TTL (default: 5 min)</td></tr>
 *   <tr><td>No API call required</td>
 *       <td>One API call per user (cached)</td></tr>
 * </table>
 *
 * <h2>Authority Mapping</h2>
 * <pre>
 *   IAS group "admin"  →  Spring authority "ROLE_admin"
 *   IAS group "viewer" →  Spring authority "ROLE_viewer"
 * </pre>
 *
 * These authorities are evaluated by Spring Security, e.g.:
 * <pre>
 *   .requestMatchers("/api/admin/**").hasRole("admin")
 *   @PreAuthorize("hasRole('viewer')")
 * </pre>
 */
@Slf4j
@Component
public class IasGroupsAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private static final String ROLE_PREFIX = "ROLE_";

    private final IasScimClient iasScimClient;

    public IasGroupsAuthoritiesConverter(IasScimClient iasScimClient) {
        this.iasScimClient = iasScimClient;
    }

    /**
     * Reads the {@code sub} claim from the JWT and queries the user's
     * current IAS groups via SCIM.
     *
     * @param jwt validated IAS JWT
     * @return Collection of {@code ROLE_xxx} authorities
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        String subject = jwt.getSubject();

        if (subject == null || subject.isBlank()) {
            log.warn("JWT without 'sub' claim – no groups assigned");
            return Collections.emptyList();
        }

        return iasScimClient.getUserGroups(subject).stream()
                .map(group -> (GrantedAuthority) new SimpleGrantedAuthority(ROLE_PREFIX + group))
                .toList();
    }
}
