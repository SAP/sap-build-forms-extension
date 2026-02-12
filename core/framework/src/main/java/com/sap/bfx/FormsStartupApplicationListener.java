package com.sap.bfx;

import com.sap.bfx.callback.CallbackService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * Listener to be informed when the Spring ApplicationContext is initialized or refreshed.
 * This is used to perform actions after all beans are created and the context is ready.
 * It registers callbacks in the CallbackService once the application context is fully initialized.
 *
 */
@Slf4j
@Component
public class FormsStartupApplicationListener implements ApplicationListener<ContextRefreshedEvent> {
    /**
     * This method is called when the ApplicationContext is initialized or refreshed.
     * It retrieves the CallbackService bean and calls its method to search and register callbacks.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        final var appContext = event.getApplicationContext();
        log.info("FORMS -> all beans are initilized");

        final var callbackService = appContext.getBean(CallbackService.class);
        callbackService.searchAndRegisterCallbacks(event);
    }
}
