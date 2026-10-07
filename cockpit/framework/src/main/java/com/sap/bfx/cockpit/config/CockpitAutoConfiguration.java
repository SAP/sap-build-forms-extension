package com.sap.bfx.cockpit.config;

import com.sap.bfx.callback.FeedService;
import com.sap.bfx.cockpit.service.CockpitService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Spring Boot auto-configuration for cockpit/framework.
 * Registers CockpitService and FeedService so they are available in any app
 * that has cockpit/framework on its classpath, without requiring manual component scanning.
 */
@AutoConfiguration
public class CockpitAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CockpitService cockpitService(ApplicationContext applicationContext) {
        return new CockpitService(applicationContext);
    }

    @Bean
    @ConditionalOnMissingBean
    public FeedService feedService(CockpitService cockpitService) {
        return (formId, userNm, text, type, parentId) ->
                cockpitService.addFeed(formId, userNm, text, type, parentId);
    }
}
