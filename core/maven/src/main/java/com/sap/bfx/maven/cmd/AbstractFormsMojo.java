package com.sap.bfx.maven.cmd;

import org.apache.maven.plugin.AbstractMojo;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public abstract class AbstractFormsMojo extends AbstractMojo {

    protected ApplicationContext createApplicationContext() {
        final var appContext = new AnnotationConfigApplicationContext();
        appContext.getBeanFactory().registerSingleton("log", this.getLog());
        appContext.getBeanFactory().registerSingleton("applicationContext", appContext);
        appContext.scan("com.sap.bfx.maven");
        appContext.refresh();

        return appContext;
    }
}
