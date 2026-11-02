package com.sap.bfx.security.ias;

import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.security.SecuritySession;
import com.sap.bfx.security.SecuritySessionFactory;
import com.sap.bfx.security.User;
import com.sap.cloud.security.ams.api.AuthorizationManagementService;
import com.sap.cloud.security.ams.core.principal.IasTokenPrincipal;
import com.sap.cloud.security.token.SapIdToken;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Factory class for creating {@link SecuritySession} objects from IAS JWT tokens.
 *
 * <p>Stage 2 of the SCIM→AMS migration (2026-10-28): user profile fields are now sourced
 * from the JWT claims, and {@link GrantedAuthority} values are derived from the policies
 * assigned to the user in IAS Authorization Management (AMS), replacing the former SCIM
 * group/role lookup. The {@link com.sap.bfx.security.ias.scim.IasScimClient} is no longer
 * called on the authorization path; its Spring bean remains registered but dormant.
 *
 * <p>Policy names returned by {@link AuthorizationManagementService#getPolicyAssignments}
 * are mapped to {@link SimpleGrantedAuthority} using the local policy name (e.g.
 * {@code SBFX_StartProcess}), matching the constants in
 * {@link com.sap.bfx.security.FormsGrantedAuthority}.
 */
@Service
@Conditional(IasEnabledCondition.class)
@Slf4j
public class IasSecuritySessionFactory implements SecuritySessionFactory {

    private final JwtDecoder jwtDecoder;
    private final AuthorizationManagementService ams;

    @Autowired
    public IasSecuritySessionFactory(JwtDecoder jwtDecoder, AuthorizationManagementService ams) {
        this.jwtDecoder = jwtDecoder;
        this.ams = ams;
    }

    @Override
    public SecuritySession create(Object input) {
        if (!(input instanceof String tokenValue && StringUtils.isNotBlank(tokenValue))) {
            throw ExceptionUtils.from("Cannot create security-session because token is empty!");
        }

        // ── 1. Decode the JWT for claim extraction + SecuritySession constructor ──
        final var token = jwtDecoder.decode(tokenValue);

        // ── 2. Build User from JWT claims (no SCIM call) ────────────────────────
        final var user = new User();
        try {
            user.setId(token.getClaimAsString("scim_id"));
            user.setUserName(token.getClaimAsString("user_name") != null
                    ? token.getClaimAsString("user_name")
                    : token.getClaimAsString("email"));

            final var name = new User.UserName();
            name.setGivenName(token.getClaimAsString("given_name"));
            name.setFamilyName(token.getClaimAsString("family_name"));
            if (name.getGivenName() != null || name.getFamilyName() != null) {
                name.setFormatted(StringUtils.trimToNull(
                        StringUtils.defaultString(name.getGivenName()) + " "
                        + StringUtils.defaultString(name.getFamilyName())));
            }
            user.setName(name);

            final var email = token.getClaimAsString("email");
            if (email != null) {
                user.setEmails(new String[]{ email });
            }
        } catch (Exception e) {
            log.warn("IasSecuritySessionFactory: could not extract profile claims from token — {}", e.getMessage());
        }

        // ── 3. Derive authorities from AMS policy assignments ───────────────────
        try {
            final var sapIdToken  = new SapIdToken(tokenValue);
            final var principal   = IasTokenPrincipal.create(sapIdToken);
            final Set<com.sap.cloud.security.ams.api.PolicyName> assigned =
                    ams.getPolicyAssignments(principal);

            final List<GrantedAuthority> authorities = assigned.stream()
                    .map(p -> (GrantedAuthority) new SimpleGrantedAuthority(p.getLocalName()))
                    .collect(Collectors.toList());

            user.setAuthorities(authorities);
            // Persist full policy names so IasSecurityService can rebuild Authorizations
            // per request without re-parsing the token. Includes both unrestricted names
            // (e.g. "SBFX_StartProcess") and admin-derived restricted names
            // (e.g. "SBFX_StartProcess_ScenarioA") — PolicyName::toString returns the full name.
            user.setAssignedPolicyNames(assigned.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList()));

            log.info("IasSecuritySessionFactory: user '{}' has {} AMS policy assignments: {}",
                    user.getUserName(), authorities.size(),
                    authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.joining(", ")));

        } catch (Exception e) {
            log.error("IasSecuritySessionFactory: failed to resolve AMS policy assignments for user '{}' — {}",
                    user.getUserName(), e.getMessage(), e);
            // Fail safe: empty authorities → user authenticated but not authorized for any action.
            user.setAuthorities(List.of());
            user.setAssignedPolicyNames(List.of());
        }

        return new SecuritySession(
                StringUtils.remove(UUID.randomUUID().toString(), '-'), user, tokenValue, token);
    }
}
