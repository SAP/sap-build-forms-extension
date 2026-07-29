package com.sap.bfx.p13n.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.relational.core.dialect.AnsiDialect;
import org.springframework.data.relational.core.dialect.Dialect;

@Configuration
public class HanaJdbcConfig {

    @Bean
    @Primary
    @ConditionalOnProperty(name = "forms.dbType", havingValue = "hana")
    public Dialect hanaJdbcDialect() {
        return AnsiDialect.INSTANCE;
    }
}