package com.sap.bfx.security;

import com.sap.bfx.definition.EventType;
import com.sap.bfx.exception.NotAuthorizedException;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PublicSecurityService implements SecurityService {

    /**
     * Mapped the event type to a list of forms roles
     *
     * @param type event type of the security check that is to map
     * @return list of the forms roles corresponding to the event type
     */
    protected List<AbstractRoles> enrichFormsRolesByType(final EventType type) {
        List<AbstractRoles> formsRoles = new ArrayList<>();
        if (null == type) {
            return formsRoles;
        }
        switch (type) {
            case StartProcessAuth -> formsRoles.add(FormsRoles.StartProcess);
            case TaskExecutionAuth -> formsRoles.add(FormsRoles.ParticipateProcess);
            case ShowContextAuth, DownloadAttachmentAuth, FindValueHelpAuth -> {
                formsRoles.add(FormsRoles.StartProcess);
                formsRoles.add(FormsRoles.ParticipateProcess);
                formsRoles.add(FormsRoles.SeeAfterStart);
            }
            case UploadAttachmentAuth, DeleteAttachmentAuth -> {
                formsRoles.add(FormsRoles.StartProcess);
                formsRoles.add(FormsRoles.ParticipateProcess);
            }
            case GetScenarioControllerAuth -> {
                formsRoles.add(FormsRoles.StartProcess);
                formsRoles.add(FormsRoles.ParticipateProcess);
                formsRoles.add(FormsRoles.SeeAfterStart);
                formsRoles.add(FormsRoles.FireFighter);
                formsRoles.add(FormsRoles.TechnicalOwner);
                formsRoles.add(FormsRoles.BusinessOwner);
            }
            case PostScenarioControllerAuth -> {
                formsRoles.add(FormsRoles.StartProcess);
                formsRoles.add(FormsRoles.ParticipateProcess);
                formsRoles.add(FormsRoles.FireFighter);
                formsRoles.add(FormsRoles.TechnicalOwner);
                formsRoles.add(FormsRoles.BusinessOwner);
            }
        }
        return formsRoles;
    }

    /**
     * Evaluates if the given user is allowed to execute the given role
     *
     * @param token Security token
     * @param role  role that should be checked against
     * @return true if allowed, otherwise false
     */
    protected boolean isAuthorized(AbstractAuthenticationToken token, String role) {
        return true;
    }

    /**
     * checks if user has the current role, if not then a NotAuthorizedException is thrown
     *
     * @param token security token
     * @param role  role that should be checked against
     * @throws NotAuthorizedException
     */
    protected void ensureAuthorized(AbstractAuthenticationToken token, String role) throws NotAuthorizedException {
        if (!isAuthorized(token, role)) {
            var name = "";
            try {
                name = token.getName();
            } catch (Exception ignore) {
            }
            throw new NotAuthorizedException(null, new String[]{role}, name);
        }
    }

    /**
     * Checks if user has permission for the event type, if not then a NotAuthorizedException is thrown.
     * Event type and a flag for disable the enrichment to check on general forms roles can be used to check on general forms roles.
     * Additional parameter are sourceRowId and sourceKeys that allows to check on UI field level.
     * Additional internal operation will be executed by this operation.
     *
     * @param token                   security token
     * @param type                    event type of the security check
     * @param disableEnrichFormsRoles flag to disable the enrichment of forms roles
     * @param sourceRowId             row id for field checks
     * @param sourceKeys              keys of UI fields for field checks
     * @throws NotAuthorizedException
     */
    @Override
    public void ensureAuthorized(final AbstractAuthenticationToken token, final EventType type, final Boolean disableEnrichFormsRoles, final String sourceRowId, final String... sourceKeys) throws NotAuthorizedException {
        switch (type) {
            case StartProcessAuth, TaskExecutionAuth, ShowContextAuth, DownloadAttachmentAuth, FindValueHelpAuth,
                 UploadAttachmentAuth, DeleteAttachmentAuth, GetScenarioControllerAuth, PostScenarioControllerAuth ->
                    ensureAuthorized(token, type, disableEnrichFormsRoles, null);
            default -> throw new NotAuthorizedException(null, type.getIdentifier(), token.getName());
        }
    }

    /**
     * Checks if user has the current role, if not then a NotAuthorizedException is thrown.
     * Event type and a flag for disable the enrichment to check on general forms roles can be used to check on general forms roles.
     *
     * @param token                   security token
     * @param type                    event type of the security check
     * @param disableEnrichFormsRoles flag to disable the enrichment of forms roles
     * @param role                    role that should be checked against
     * @throws NotAuthorizedException
     */
    @Override
    public void ensureAuthorized(final AbstractAuthenticationToken token, final EventType type, final Boolean disableEnrichFormsRoles, final AbstractRoles role) throws NotAuthorizedException {
        List<AbstractRoles> scannableRoles = new ArrayList<>();
        if (null != role) {
            scannableRoles.add(role);
        }
        if (null == disableEnrichFormsRoles || disableEnrichFormsRoles.equals(Boolean.FALSE)) {
            scannableRoles.addAll(enrichFormsRolesByType(type));
        }
        if (scannableRoles.isEmpty()) {
            throw new NotAuthorizedException(null, type.getIdentifier(), token.getName());
        }
        ensureAnyAuthorized(token, scannableRoles.stream().map(AbstractRoles::getValue).toArray(String[]::new));
    }

    /**
     * Checks if user has at least one of the roles, if not then a NotAuthorizedException is thrown
     *
     * @param token security token
     * @param roles roles that should be checked against
     * @throws NotAuthorizedException
     */
    protected void ensureAnyAuthorized(AbstractAuthenticationToken token, String... roles) throws NotAuthorizedException {
        if (Arrays.stream(roles).noneMatch(r -> isAuthorized(token, r))) {
            var name = "";
            try {
                name = token.getName();
            } catch (Exception ignore) {
            }
            throw new NotAuthorizedException(null, roles, name);
        }
    }

    /**
     * Checks if user has at least one of the roles, if not then a NotAuthorizedException is thrown
     * Event type and a flag for disable the enrichment to check on general forms roles can be used to check on general forms roles.
     *
     * @param token                   security token
     * @param type                    event type of the security check
     * @param disableEnrichFormsRoles flag to disable the enrichment of forms roles
     * @param roles                   roles that should be checked against
     * @throws NotAuthorizedException
     */
    @Override
    public void ensureAnyAuthorized(final AbstractAuthenticationToken token, final EventType type, final Boolean disableEnrichFormsRoles, final AbstractRoles... roles) throws NotAuthorizedException {
        List<AbstractRoles> scannableRoles = new ArrayList<>();
        if (null != roles && 0 < roles.length) {
            scannableRoles.addAll(Arrays.stream(roles).toList());
        }
        if (null == disableEnrichFormsRoles || disableEnrichFormsRoles.equals(Boolean.FALSE)) {
            scannableRoles.addAll(enrichFormsRolesByType(type));
        }
        if (scannableRoles.isEmpty()) {
            throw new NotAuthorizedException(null, type.getIdentifier(), token.getName());
        }
        ensureAnyAuthorized(token, scannableRoles.stream().map(AbstractRoles::getValue).toArray(String[]::new));
    }
}
