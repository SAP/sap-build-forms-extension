package com.sap.bfx.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Strings;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class OidcSecurityEnabledCondition implements Condition {
    /**
     * @param context
     * @param metadata
     * @return
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final var authType = context.getEnvironment().getProperty("forms.security.auth.type");
        log.debug("forms.security.auth.type is {}", authType);
        return Strings.CI.equals(authType, Constants.AUTH_TYPE_OIDC);
    }
}
