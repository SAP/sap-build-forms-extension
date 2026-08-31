package com.sap.bfx.security.session;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;

/**
 * Condition that checks if the security session is enabled based on the presence of a Redis host configuration.
 * This condition is used to determine whether to enable the SecuritySessionService.
 */
public class SecuritySessionEnabled implements Condition {
    @Override
    public boolean matches(org.springframework.context.annotation.ConditionContext context,
                           org.springframework.core.type.AnnotatedTypeMetadata metadata) {
        final var host = context.getEnvironment().getProperty("forms.security.session.redis.host");

        return StringUtils.isNotBlank(host);
    }
}
