package com.sap.bfx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class ThreadConfig {

    /**
     * Disposable Thread Pool Executor
     *
     * @return
     */
    @Bean("disposableThreadPoolExecutor")
    public TaskExecutor disposableThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.initialize();
        return executor;
    }
}
