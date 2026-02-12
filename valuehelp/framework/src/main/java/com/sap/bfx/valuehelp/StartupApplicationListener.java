package com.sap.bfx.valuehelp;

import com.sap.bfx.valuehelp.config.ApplicationConfig;
import com.sap.bfx.valuehelp.service.ValueHelpRefreshService;
import com.sap.bfx.valuehelp.service.ValueHelpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
public class StartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        final var appContext = event.getApplicationContext();
        log.info("valuehelp -> all beans are initilized");

        final var service = appContext.getBean(ValueHelpService.class);
        service.initValueHelpAdapters(appContext);

        final var refreshService = appContext.getBean(ValueHelpRefreshService.class);
        refreshService.initValueHelpAdapters(appContext);

        final var taskScheduler = appContext.getBean(TaskScheduler.class);
        final var appProperties = appContext.getBean(ApplicationConfig.class);

        taskScheduler.scheduleAtFixedRate(refreshService::task, Instant.now(), appProperties.getRefresh());
    }
}
