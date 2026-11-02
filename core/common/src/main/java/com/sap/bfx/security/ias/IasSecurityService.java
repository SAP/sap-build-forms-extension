package com.sap.bfx.security.ias;

import com.sap.bfx.definition.EventType;
import com.sap.bfx.exception.NotAuthenticatedException;
import com.sap.bfx.exception.NotAuthorizedException;
import com.sap.bfx.security.FormsGrantedAuthority;
import com.sap.bfx.security.SecurityService;
import com.sap.bfx.security.User;
import com.sap.cloud.security.ams.api.AuthorizationManagementService;
import com.sap.cloud.security.ams.api.Authorizations;
import com.sap.cloud.security.ams.api.PolicyName;
import com.sap.cloud.security.ams.api.expression.AttributeName;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * IAS implementation of {@link SecurityService}.
 *
 * <h2>Authorization path (Stage 3)</h2>
 * <p>Scenario {@link EventType}s (StartProcessAuth, TaskExecutionAuth, etc.) are now decided
 * via AMS {@code checkPrivilege(action, "process", {scenarioId: appName})}, which evaluates
 * each policy's {@code WHERE/RESTRICT} body server-side.  This correctly handles both
 * unrestricted assignments ({@code SBFX_StartProcess}) and admin-derived restricted assignments
 * ({@code SBFX_StartProcess_ScenarioA}) — the latter's name does not match the
 * {@link FormsGrantedAuthority} constant, so a coarse string-match would silently fail.
 *
 * <p>Non-scenario authorities ({@code SBFX_P13N*}, {@code SBFX_ValueHelp*}) keep the Stage-2
 * coarse {@code user.getAuthorities().contains(...)} path unchanged.
 *
 * <h2>Rebuild model</h2>
 * <p>{@code Authorizations} is rebuilt per request from
 * {@link AuthorizationManagementService#getAuthorizations(Set)} using the policy-name strings
 * stored on {@link User#getAssignedPolicyNames()} at login.  {@code Authorizations} is
 * non-serializable and must never be stored in Redis.
 *
 * <h2>OR semantics</h2>
 * <p>The existing {@link #enrichFormsGroupsByType(EventType)} method determines which
 * authorities satisfy a given event type (OR logic).  For scenario EventTypes Stage 3 iterates
 * that same authority set, maps each to its DCL action, and grants if ANY
 * {@code checkPrivilege} call returns {@code isGranted()}.
 */
@Service
@Slf4j
@Conditional(IasEnabledCondition.class)
public class IasSecurityService implements SecurityService {

    // ── EventTypes that require scenario-scoped AMS checkPrivilege ────────────
    private static final Set<EventType> SCENARIO_EVENT_TYPES = EnumSet.of(
            EventType.StartProcessAuth,
            EventType.TaskExecutionAuth,
            EventType.ShowContextAuth,
            EventType.GetScenarioControllerAuth,
            EventType.PostScenarioControllerAuth,
            EventType.UploadAttachmentAuth,
            EventType.DownloadAttachmentAuth,
            EventType.DeleteAttachmentAuth,
            EventType.FindValueHelpAuth
    );

    // ── FormsGrantedAuthority → DCL action (must match deployed DCL casing) ──
    // Map key is AttributeName, NOT String (SAP docs prose is wrong;
    // ams-core 4.5.1 bytecode confirms Map<AttributeName,Object>).
    private static final Map<GrantedAuthority, String> AUTHORITY_ACTION_MAP = Map.of(
            FormsGrantedAuthority.SBFX_StartProcess,       "start",
            FormsGrantedAuthority.SBFX_ParticipateProcess, "participate",
            FormsGrantedAuthority.SBFX_SearchProcess,      "search",
            FormsGrantedAuthority.SBFX_SeeAfterStart,      "view",
            FormsGrantedAuthority.SBFX_TechnicalOwner,     "technicalOwn",
            FormsGrantedAuthority.SBFX_BusinessOwner,      "businessOwn",
            FormsGrantedAuthority.SBFX_FireFighter,        "firefight"
    );

    private final AuthorizationManagementService ams;

    @Autowired
    public IasSecurityService(AuthorizationManagementService ams) {
        this.ams = ams;
    }

    // ── SecurityService interface ─────────────────────────────────────────────

    @Override
    public void ensureAuthorized(final String appName, final User user, final EventType type,
                                 final boolean disableEnrichFormsGroups, final String sourceRowId,
                                 final String... sourceKeys) throws NotAuthorizedException {

        if (user == null) {
            throw new NotAuthenticatedException();
        }

        switch (type) {
            case StartProcessAuth, TaskExecutionAuth, ShowContextAuth, DownloadAttachmentAuth, FindValueHelpAuth,
                 UploadAttachmentAuth, DeleteAttachmentAuth, GetScenarioControllerAuth, PostScenarioControllerAuth ->
                    ensureAuthorized(appName, user, type, disableEnrichFormsGroups, null);
            default -> throw new NotAuthorizedException(type.getIdentifier(), user.getUserName());
        }
    }

    @Override
    public void ensureAuthorized(final String appName, final User user, final EventType type,
                                 final boolean disableEnrichFormsGroups, final GrantedAuthority authority)
            throws NotAuthorizedException {

        if (user == null) {
            throw new NotAuthenticatedException();
        }

        Collection<GrantedAuthority> scannableGroups = new ArrayList<>();
        if (null != authority) {
            scannableGroups.add(authority);
        }
        if (!disableEnrichFormsGroups) {
            scannableGroups.addAll(enrichFormsGroupsByType(type));
        }
        if (scannableGroups.isEmpty()) {
            final var authorities = new ArrayList<GrantedAuthority>();
            authorities.add(authority);
            throw new NotAuthorizedException(type.getIdentifier(), authorities, user.getUserName());
        }
        ensureAnyAuthorized(appName, user, scannableGroups);
    }

    @Override
    public void ensureAnyAuthorized(final String appName, final User user, final EventType type,
                                    final boolean disableEnrichFormsGroups, final GrantedAuthority... authorities)
            throws NotAuthorizedException {

        if (user == null) {
            throw new NotAuthenticatedException();
        }

        Collection<GrantedAuthority> scannableGroups = new ArrayList<>();
        if (null != authorities && 0 < authorities.length) {
            scannableGroups.addAll(Arrays.stream(authorities).toList());
        }
        if (!disableEnrichFormsGroups) {
            scannableGroups.addAll(enrichFormsGroupsByType(type));
        }
        if (scannableGroups.isEmpty()) {
            throw new NotAuthorizedException(type.getIdentifier(), Arrays.stream(authorities).toList(),
                    user.getUserName());
        }
        ensureAnyAuthorized(appName, user, scannableGroups);
    }

    // ── Internal check dispatch ───────────────────────────────────────────────

    /**
     * Checks whether the user holds at least one of the provided authorities,
     * dispatching to scenario-scoped AMS {@code checkPrivilege} for scenario
     * EventType authorities, or the coarse set-comparison for others.
     */
    protected void ensureAnyAuthorized(final String appName, final User user,
                                       @NonNull final Collection<GrantedAuthority> authorities)
            throws NotAuthorizedException {

        if (user == null) {
            throw new NotAuthenticatedException();
        }

        if (authorities.stream().noneMatch(authority -> isAuthorized(appName, user, authority))) {
            throw new NotAuthorizedException(authorities, user.getUserName());
        }
    }

    /**
     * Core authorization check for a single authority.
     *
     * <p>For authorities that have a DCL action mapping and a non-blank {@code appName},
     * delegates to {@link #isAuthorizedViaAms(String, User, GrantedAuthority)}.
     * All other authorities (P13N, ValueHelp, unknown) fall back to the Stage-2
     * coarse {@code user.getAuthorities().contains(...)} check.
     */
    protected boolean isAuthorized(final String appName, final User user, final GrantedAuthority authority) {
        if (user == null) {
            log.error("isAuthorized called with user==null");
            return false;
        }

        // Scenario-scoped check via AMS checkPrivilege
        if (StringUtils.isNotBlank(appName) && AUTHORITY_ACTION_MAP.containsKey(authority)) {
            return isAuthorizedViaAms(appName, user, authority);
        }

        // Non-scenario authorities (P13N, ValueHelp) — coarse set-comparison (Stage 2 path)
        return isAuthorizedViaAuthorities(appName, user, authority);
    }

    /**
     * Scenario-scoped authorization via AMS {@code checkPrivilege}.
     *
     * <p>Rebuilds an {@link Authorizations} view from the user's persisted policy names
     * (set at login by {@code IasSecuritySessionFactory}) and calls
     * {@code checkPrivilege(action, "process", {scenarioId: appName})}.
     * {@code Authorizations} is a lightweight in-memory view over the loaded bundle;
     * rebuilding per request is the AMS-intended model.
     */
    private boolean isAuthorizedViaAms(final String appName, final User user,
                                       final GrantedAuthority authority) {
        final String action = AUTHORITY_ACTION_MAP.get(authority);

        try {
            final List<String> policyNames = user.getAssignedPolicyNames();
            if (policyNames == null || policyNames.isEmpty()) {
                log.warn("isAuthorizedViaAms: user '{}' has no assigned policy names — denying",
                        user.getUserName());
                return false;
            }

            final Set<PolicyName> assigned = policyNames.stream()
                    .map(PolicyName::of)
                    .collect(Collectors.toSet());

            // Rebuild Authorizations per request — never store in Redis (non-serializable)
            final Authorizations authz = ams.getAuthorizations(assigned);

            final var decision = authz.checkPrivilege(
                    action,
                    "process",
                    Map.of(AttributeName.of("scenarioId"), appName));

            final boolean granted = decision.isGranted();

            if (granted) {
                log.info("User '{}' is authorized for authority='{}' action='{}' scenarioId='{}'",
                        user.getUserName(), authority.getAuthority(), action, appName);
            } else {
                log.warn("User '{}' is NOT authorized for authority='{}' action='{}' scenarioId='{}'",
                        user.getUserName(), authority.getAuthority(), action, appName);
            }

            return granted;

        } catch (Exception e) {
            log.error("isAuthorizedViaAms: AMS checkPrivilege failed for user '{}' action='{}' scenarioId='{}' — {}",
                    user.getUserName(), action, appName, e.getMessage(), e);
            return false;
        }
    }

    /**
     * Coarse authority-set check (Stage 2 path, unchanged).
     * Used for non-scenario authorities and as the legacy scenario-suffix fallback.
     */
    private boolean isAuthorizedViaAuthorities(final String appName, final User user,
                                               final GrantedAuthority authority) {
        log.debug("User {} has authorities '{}'", user.getUserName(), user.getAuthorities());

        var authorized = false;
        if (user.getAuthorities() != null) {
            authorized = user.getAuthorities().contains(authority);
        }
        // Legacy scenario-suffix check (SBFX_StartProcess_<appName>)
        if (!authorized && StringUtils.isNotBlank(appName)) {
            authorized = user.getAuthorities()
                             .contains(new SimpleGrantedAuthority(authority.getAuthority() + "_" + appName));
        }

        if (authorized) {
            log.info("User '{}' is authorized for {}", user.getUserName(),
                    StringUtils.join(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
                            ", "));
        } else {
            log.warn("User '{}' is NOT authorized for {}", user.getUserName(),
                    StringUtils.join(user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList(),
                            ", "));
        }

        return authorized;
    }

    // ── EventType → acceptable authority set (unchanged from Stage 2) ─────────

    /**
     * Maps each auth-relevant {@link EventType} to the set of authorities whose
     * possession satisfies it (OR logic). Used by all three {@code ensureAuthorized}
     * overloads to determine the candidate authority set, which is then evaluated
     * either via AMS {@code checkPrivilege} (scenario EventTypes) or set-comparison.
     */
    protected List<GrantedAuthority> enrichFormsGroupsByType(final EventType type) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (null == type) {
            return authorities;
        }
        switch (type) {
            case StartProcessAuth -> authorities.add(FormsGrantedAuthority.SBFX_StartProcess);
            case TaskExecutionAuth -> authorities.add(FormsGrantedAuthority.SBFX_ParticipateProcess);
            case ShowContextAuth, DownloadAttachmentAuth, FindValueHelpAuth -> {
                authorities.add(FormsGrantedAuthority.SBFX_StartProcess);
                authorities.add(FormsGrantedAuthority.SBFX_ParticipateProcess);
                authorities.add(FormsGrantedAuthority.SBFX_SeeAfterStart);
            }
            case UploadAttachmentAuth, DeleteAttachmentAuth -> {
                authorities.add(FormsGrantedAuthority.SBFX_StartProcess);
                authorities.add(FormsGrantedAuthority.SBFX_ParticipateProcess);
            }
            case GetScenarioControllerAuth -> {
                authorities.add(FormsGrantedAuthority.SBFX_StartProcess);
                authorities.add(FormsGrantedAuthority.SBFX_ParticipateProcess);
                authorities.add(FormsGrantedAuthority.SBFX_SeeAfterStart);
                authorities.add(FormsGrantedAuthority.SBFX_FireFighter);
                authorities.add(FormsGrantedAuthority.SBFX_TechnicalOwner);
                authorities.add(FormsGrantedAuthority.SBFX_BusinessOwner);
            }
            case PostScenarioControllerAuth -> {
                authorities.add(FormsGrantedAuthority.SBFX_StartProcess);
                authorities.add(FormsGrantedAuthority.SBFX_ParticipateProcess);
                authorities.add(FormsGrantedAuthority.SBFX_FireFighter);
                authorities.add(FormsGrantedAuthority.SBFX_TechnicalOwner);
                authorities.add(FormsGrantedAuthority.SBFX_BusinessOwner);
            }
        }
        return authorities;
    }
}
