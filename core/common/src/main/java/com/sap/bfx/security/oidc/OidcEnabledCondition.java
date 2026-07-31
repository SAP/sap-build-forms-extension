package com.sap.bfx.security.oidc;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/**
 * Checks if the configuration for OIDC authentication is enabled. If yes this condition can be used to enable
 * a OIDC based security, otherwise it should not be enabled
 */
@Slf4j
public class OidcEnabledCondition implements Condition {
    @Override
    public boolean matches(@Nonnull ConditionContext context, @Nonnull AnnotatedTypeMetadata metadata) {
        final var issuerUrl = context.getEnvironment().getProperty("forms.security.oidc.issuerUri");
        final var clientId = context.getEnvironment().getProperty("forms.security.oidc.clientId");
        final var clientSecret = context.getEnvironment().getProperty("forms.security.oidc.clientSecret");

        final var match = StringUtils.isNotBlank(issuerUrl) && StringUtils.isNotBlank(clientId) &&
                StringUtils.isNotBlank(clientSecret);

        if (match) {
            log.info("OidcEnabledCondition match");
        } else {
            log.warn("OidcEnabledCondition not match");
        }
        return match;
    }
}
