package com.sap.bfx.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class PublicOidcSecurityEnabledCondition implements Condition {
    /**
     * @param context
     * @param metadata
     * @return
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final var localJwtFile = context.getEnvironment().getProperty("forms.localJwtFile");
        final var authType = context.getEnvironment().getProperty("forms.security.auth.type");

        log.debug("forms.JwtFile is '{}' and forms.security.auth.type is '{}'", localJwtFile, authType);
        return StringUtils.isBlank(localJwtFile) && StringUtils.isBlank(authType);
    }

}
