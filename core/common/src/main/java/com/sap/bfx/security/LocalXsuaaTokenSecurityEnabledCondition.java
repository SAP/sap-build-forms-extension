package com.sap.bfx.security;

import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class LocalXsuaaTokenSecurityEnabledCondition implements Condition {
    /**
     * @param context
     * @param metadata
     * @return
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final var localJwtFile = context.getEnvironment().getProperty("forms.localJwtFile", "");
        final var xsappname = context.getEnvironment().getProperty("forms.xsAppName", "");

        log.debug("forms.JwtFile is '{}' and forms.xsAppName is '{}'", localJwtFile, xsappname);
        return StringUtils.isNotBlank(localJwtFile) && StringUtils.isBlank(xsappname);
    }
}
