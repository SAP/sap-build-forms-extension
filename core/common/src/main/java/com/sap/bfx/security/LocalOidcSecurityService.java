package com.sap.bfx.security;

import com.sap.bfx.definition.EventType;
import com.sap.bfx.exception.NotAuthorizedException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class LocalOidcSecurityService implements SecurityService {

    /**
     * Mapped the event type to a list of forms groups
     *
     * @param type event type of the security check that is to map
     * @return list of the forms groups corresponding to the event type
     */
    protected List<AbstractGroups> enrichFormsGroupsByType(final EventType type) {
        List<AbstractGroups> formsGroups = new ArrayList<>();
        if (null == type) {
            return formsGroups;
        }
        switch (type) {
            case StartProcessAuth -> formsGroups.add(FormsGroups.SBFX_StartProcess);
            case TaskExecutionAuth -> formsGroups.add(FormsGroups.SBFX_ParticipateProcess);
            case ShowContextAuth, DownloadAttachmentAuth, FindValueHelpAuth -> {
                formsGroups.add(FormsGroups.SBFX_StartProcess);
                formsGroups.add(FormsGroups.SBFX_ParticipateProcess);
                formsGroups.add(FormsGroups.SBFX_SeeAfterStart);
            }
            case UploadAttachmentAuth, DeleteAttachmentAuth -> {
                formsGroups.add(FormsGroups.SBFX_StartProcess);
                formsGroups.add(FormsGroups.SBFX_ParticipateProcess);
            }
            case GetScenarioControllerAuth -> {
                formsGroups.add(FormsGroups.SBFX_StartProcess);
                formsGroups.add(FormsGroups.SBFX_ParticipateProcess);
                formsGroups.add(FormsGroups.SBFX_SeeAfterStart);
                formsGroups.add(FormsGroups.SBFX_FireFighter);
                formsGroups.add(FormsGroups.SBFX_TechnicalOwner);
                formsGroups.add(FormsGroups.SBFX_BusinessOwner);
            }
            case PostScenarioControllerAuth -> {
                formsGroups.add(FormsGroups.SBFX_StartProcess);
                formsGroups.add(FormsGroups.SBFX_ParticipateProcess);
                formsGroups.add(FormsGroups.SBFX_FireFighter);
                formsGroups.add(FormsGroups.SBFX_TechnicalOwner);
                formsGroups.add(FormsGroups.SBFX_BusinessOwner);
            }
        }
        return formsGroups;
    }

    /**
     * Evaluates if the given user is allowed to execute the given group
     *
     * @param token security token
     * @param group group that should be checked against
     * @return true if allowed, otherwise false
     */
    protected boolean isAuthorized(final AbstractAuthenticationToken token, final String group) {
        // if there is no token then we deny access
        if (token == null) {
            log.error("isAuthorized called with token={}", token);
            return false;
        }

        log.debug("Token is {}", (token == null) ? "not available" : token.toString());
        log.debug("Credentials ('{}') = {}", token.getCredentials().getClass().getName(),
                token.getCredentials().toString());

        final var jwt = (Jwt) token.getCredentials();
        log.debug("Token-Value: '{}'", jwt.getTokenValue());

        final var groups = jwt.getClaimAsStringList("groups");
        log.debug("Groups are '{}'", groups);

        final var authorized = groups.stream().anyMatch(aGroup ->
                Strings.CS.endsWith(aGroup, group));
        log.debug("User '{}' is {}", jwt.getClaim("user_uuid"), authorized ? "authorized" : "NOT authorized");

        return authorized;
    }

    /**
     * Checks if user has permission for the event type, if not then a NotAuthorizedException is thrown.
     * Event type and a flag for disable the enrichment to check on general forms groups can be used to check on general forms groups.
     * Additional parameter are sourceRowId and sourceKeys that allows to check on UI field level.
     * Additional internal operation will be executed by this operation.
     *
     * @param token                    security token
     * @param type                     event type of the security check
     * @param disableEnrichFormsGroups flag to disable the enrichment of forms groups
     * @param sourceRowId              row id for field checks
     * @param sourceKeys               keys of UI fields for field checks
     * @throws NotAuthorizedException
     */
    @Override
    public void ensureAuthorized(final AbstractAuthenticationToken token, final EventType type, final Boolean disableEnrichFormsGroups, final String sourceRowId, final String... sourceKeys) throws NotAuthorizedException {
        switch (type) {
            case StartProcessAuth, TaskExecutionAuth, ShowContextAuth, DownloadAttachmentAuth, FindValueHelpAuth,
                 UploadAttachmentAuth, DeleteAttachmentAuth, GetScenarioControllerAuth, PostScenarioControllerAuth ->
                    ensureAuthorized(token, type, disableEnrichFormsGroups, null);
            default -> throw new NotAuthorizedException(type.getIdentifier(), token.getName());
        }
    }

    /**
     * Checks if user has the current group, if not then a NotAuthorizedException is thrown.
     * Event type and a flag for disable the enrichment to check on general forms groups can be used to check on general forms groups.
     *
     * @param token                    security token
     * @param type                     event type of the security check
     * @param disableEnrichFormsGroups flag to disable the enrichment of forms groups
     * @param group                    group that should be checked against
     * @throws NotAuthorizedException
     */
    @Override
    public void ensureAuthorized(final AbstractAuthenticationToken token, final EventType type, final Boolean disableEnrichFormsGroups, final AbstractGroups group) throws NotAuthorizedException {
        List<AbstractGroups> scannableGroups = new ArrayList<>();
        if (null != group) {
            scannableGroups.add(group);
        }
        if (null == disableEnrichFormsGroups || disableEnrichFormsGroups.equals(Boolean.FALSE)) {
            scannableGroups.addAll(enrichFormsGroupsByType(type));
        }
        if (scannableGroups.isEmpty()) {
            throw new NotAuthorizedException(type.getIdentifier(), token.getName());
        }
        ensureAnyAuthorized(token, scannableGroups.stream().map(AbstractGroups::getValue).toArray(String[]::new));
    }

    /**
     * Checks if user has at least one of the groups, if not then a NotAuthorizedException is thrown
     *
     * @param token  security token
     * @param groups groups that should be checked against
     * @throws NotAuthorizedException
     */
    protected void ensureAnyAuthorized(AbstractAuthenticationToken token, String... groups) throws NotAuthorizedException {
        if (Arrays.stream(groups).noneMatch(g -> isAuthorized(token, g))) {
            var name = "";
            try {
                name = token.getName();
            } catch (Exception ignore) {
            }
            throw new NotAuthorizedException(groups, name);
        }
    }

    /**
     * Checks if user has at least one of the groups, if not then a NotAuthorizedException is thrown
     * Event type and a flag for disable the enrichment to check on general forms groups can be used to check on general forms groups.
     *
     * @param token                    security token
     * @param type                     event type of the security check
     * @param disableEnrichFormsGroups flag to disable the enrichment of forms groups
     * @param groups                   groups that should be checked against
     * @throws NotAuthorizedException
     */
    @Override
    public void ensureAnyAuthorized(final AbstractAuthenticationToken token, final EventType type, final Boolean disableEnrichFormsGroups, final AbstractGroups... groups) throws NotAuthorizedException {
        List<AbstractGroups> scannableGroups = new ArrayList<>();
        if (null != groups && 0 < groups.length) {
            scannableGroups.addAll(Arrays.stream(groups).toList());
        }
        if (null == disableEnrichFormsGroups || disableEnrichFormsGroups.equals(Boolean.FALSE)) {
            scannableGroups.addAll(enrichFormsGroupsByType(type));
        }
        if (scannableGroups.isEmpty()) {
            throw new NotAuthorizedException(type.getIdentifier(), token.getName());
        }
        ensureAnyAuthorized(token, scannableGroups.stream().map(AbstractGroups::getValue).toArray(String[]::new));
    }
}
