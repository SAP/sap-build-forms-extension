package com.sap.bfx.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class XsuaaSecurityEnabledCondition implements Condition {
    /**
     * @param context
     * @param metadata
     * @return
     */
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        final var xsappname = context.getEnvironment().getProperty("forms.xsAppName");
        log.debug("XSAPPNAME is {}", xsappname);
        return StringUtils.isNotBlank(xsappname);
    }
}
