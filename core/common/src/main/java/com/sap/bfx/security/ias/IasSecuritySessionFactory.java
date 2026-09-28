package com.sap.bfx.security.ias;

import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.security.SecuritySession;
import com.sap.bfx.security.SecuritySessionFactory;
import com.sap.bfx.security.User;
import com.sap.bfx.security.ias.scim.IasScimClient;
import com.sap.bfx.security.ias.scim.ScimUser;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

/**
 * Factory class for creating SecuritySession objects based on JWT tokens and SCIM user information.
 */
@Service
@Conditional(IasEnabledCondition.class)
public class IasSecuritySessionFactory implements SecuritySessionFactory {
    private final JwtDecoder jwtDecoder;
    private final IasScimClient iasScimClient;

    /**
     * Constructs a new IasSecuritySessionFactory with the provided JwtDecoder and IasScimClient.
     *
     * @param jwtDecoder    the JwtDecoder for decoding JWT tokens
     * @param iasScimClient the IasScimClient for retrieving SCIM user information
     */
    @Autowired
    public IasSecuritySessionFactory(JwtDecoder jwtDecoder, IasScimClient iasScimClient) {
        this.jwtDecoder = jwtDecoder;
        this.iasScimClient = iasScimClient;
    }

    @Override
    public SecuritySession create(Object input) {
        if (!(input instanceof String tokenValue && StringUtils.isNotBlank(tokenValue))) {
            throw ExceptionUtils.from("Cannot create security-session because token is empty!");
        }
        final var token = jwtDecoder.decode(tokenValue);
        final var scimUser = iasScimClient.getUser(token.getClaimAsString("scim_id"));

        // convert ScimUser to User
        final var user = new User();
        try {
            // copy properties from ScimUser to User
            BeanUtils.copyProperties(user, scimUser);
            // copy emails from ScimUser to User
            if (scimUser.getScimEmails() != null) {
                user.setEmails(
                        Arrays.stream(scimUser.getScimEmails()).map(ScimUser.Info::getValue).toArray(String[]::new));
            }
            // copy phone numbers from ScimUser to User
            if (scimUser.getScimPhoneNumbers() != null) {
                user.setPhonesNumbers(Arrays.stream(scimUser.getScimPhoneNumbers()).map(ScimUser.Info::getValue)
                                            .toArray(String[]::new));
            }
            // create permission list from roles and groups
            final var permissions = new ArrayList<GrantedAuthority>();
            if (scimUser.getRoles() != null) {
                Arrays.stream(scimUser.getRoles())
                      .forEach(role -> permissions.add(new SimpleGrantedAuthority(role.getDisplay())));
            }
            if (scimUser.getGroups() != null) {
                Arrays.stream(scimUser.getGroups())
                      .forEach(group -> permissions.add(new SimpleGrantedAuthority(group.getDisplay())));
            }
            user.setAuthorities(permissions);
        } catch (Exception e) {
            throw ExceptionUtils.from("Failed to copy properties from ScimUser to User", e);
        }

        return new SecuritySession(StringUtils.remove(UUID.randomUUID().toString(), '-'), user, tokenValue, token);
    }
}
