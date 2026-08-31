package com.sap.bfx.security.ias;

import com.sap.bfx.definition.EventType;
import com.sap.bfx.exception.NotAuthorizedException;
import com.sap.bfx.security.FormsGrantedAuthority;
import com.sap.bfx.security.SecurityService;
import com.sap.bfx.security.User;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of the SecurityService interface for IAS (Identity Authentication Service) security.
 * This service ensures that users are authorized for specific event types and groups based on their authorities.
 */
@Service
@Slf4j
@Conditional(IasEnabledCondition.class)
public class IasSecurityService implements SecurityService {

    @Override
    public void ensureAuthorized(final String appName, final User user, final EventType type,
                                 final boolean disableEnrichFormsGroups, final String sourceRowId,
                                 final String... sourceKeys) throws NotAuthorizedException {
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
        Collection<GrantedAuthority> scannableGroups = new ArrayList<>();
        if (null != authority) {
            scannableGroups.add(authority);
        }
        if (!disableEnrichFormsGroups) {
            scannableGroups.addAll(enrichFormsGroupsByType(type));
        }
        if (scannableGroups.isEmpty()) {
            throw new NotAuthorizedException(type.getIdentifier(), user.getUserName());
        }
        ensureAnyAuthorized(appName, user, scannableGroups);
    }

    @Override
    public void ensureAnyAuthorized(final String appName, final User user, final EventType type,
                                    final boolean disableEnrichFormsGroups, final GrantedAuthority... authorities)
            throws NotAuthorizedException {
        Collection<GrantedAuthority> scannableGroups = new ArrayList<>();
        if (null != authorities && 0 < authorities.length) {
            scannableGroups.addAll(Arrays.stream(authorities).toList());
        }
        if (!disableEnrichFormsGroups) {
            scannableGroups.addAll(enrichFormsGroupsByType(type));
        }
        if (scannableGroups.isEmpty()) {
            throw new NotAuthorizedException(type.getIdentifier(), user.getUserName());
        }
        ensureAnyAuthorized(appName, user, scannableGroups);
    }

    /**
     * Mapped the event type to a list of forms groups
     *
     * @param type event type of the security check that is to map
     * @return list of the forms groups corresponding to the event type
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

    /**
     * Ensures that the given user is authorized for at least one of the provided authorities.
     *
     * @param user        the user to check
     * @param authorities the authorities to check against
     * @throws NotAuthorizedException if the user is not authorized for any of the provided authorities
     */
    protected void ensureAnyAuthorized(final String appName, final User user,
                                       @NonNull final Collection<GrantedAuthority> authorities)
            throws NotAuthorizedException {
        if (authorities.stream().noneMatch(authority -> isAuthorized(appName, user, authority))) {
            throw new NotAuthorizedException(authorities, user.getUserName());
        }
    }

    /**
     * Evaluates if the given user is allowed to execute the given group
     *
     * @param user      user object
     * @param authority authority that should be checked against
     * @return true if allowed, otherwise false
     */
    protected boolean isAuthorized(final String appName, final User user, final GrantedAuthority authority) {
        // if there is no token then we deny access
        if (user == null) {
            log.error("isAuthorized called with user==null");
            return false;
        }

        log.debug("User {} has authorities '{}'", user.getUserName(), user.getAuthorities());

        var authorized = false;
        if (user.getAuthorities() != null) {
            authorized = user.getAuthorities().contains(authority);
        }
        if (!authorized && StringUtils.isNotBlank(appName)) {
            authorized = user.getAuthorities()
                             .contains(new SimpleGrantedAuthority(authority.getAuthority() + "_" + appName));
        }
        log.debug("User '{}' is {}", user.getUserName(), authorized ? "authorized" : "NOT authorized");

        return authorized;
    }
}
