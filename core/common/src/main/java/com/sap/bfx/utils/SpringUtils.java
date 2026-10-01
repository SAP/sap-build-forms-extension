package com.sap.bfx.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Utility class for accessing the Spring application context.
 */
@Component
public final class SpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    private SpringUtils() {
    }

    /**
     * Get the Spring application context.
     *
     * @return the Spring application context
     */
    public static ApplicationContext getApplicationContext() {
        return SpringUtils.applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SpringUtils.applicationContext = applicationContext;
    }
}
