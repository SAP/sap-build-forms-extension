package com.sap.bfx.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class PublicSecurityEnabledCondition implements Condition {
    /**
     * @param context
     * @param metadata
     * @return
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final var xsappname = context.getEnvironment().getProperty("forms.xsAppName");
        final var localJwtFile = context.getEnvironment().getProperty("forms.localJwtFile");

        log.debug("XSAPPNAME is '{}', localJwtFile is '{}'", xsappname, localJwtFile);
        return StringUtils.isBlank(xsappname) && StringUtils.isBlank(localJwtFile);
    }

}
