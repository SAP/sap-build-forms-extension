package com.sap.bfx.security;

import jakarta.annotation.Nonnull;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Condition that checks if no security configuration is provided.
 * This condition is used to determine whether to enable the NoSecurityService.
 */
public class NoSecurityCondition implements Condition {
    @Override
    public boolean matches(@Nonnull ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
        final var issuerUrl = context.getEnvironment().getProperty("forms.security.ias.url");
        final var oidcClientId = context.getEnvironment().getProperty("forms.security.ias.oidc.clientId");
        final var oidcClientSecret = context.getEnvironment().getProperty("forms.security.ias.oidc.clientSecret");

        return StringUtils.isBlank(issuerUrl) && StringUtils.isBlank(oidcClientId) &&
                StringUtils.isBlank(oidcClientSecret);
    }
}
