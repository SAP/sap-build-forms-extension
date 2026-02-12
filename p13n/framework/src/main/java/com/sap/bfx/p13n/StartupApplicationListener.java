package com.sap.bfx.p13n;

import com.sap.bfx.p13n.service.PersonalizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        final var appContext = event.getApplicationContext();
        log.info("personalization -> all beans are initilized");

        final var service = appContext.getBean(PersonalizationService.class);
        service.initPersonalizationAdapter(appContext);
    }
}
