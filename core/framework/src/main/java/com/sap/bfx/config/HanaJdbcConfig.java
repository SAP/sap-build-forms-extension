package com.sap.bfx.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
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
    //@ConditionalOnProperty(name = "forms.scenario.data.database.dbType", havingValue = "hana")
    //@ConditionalOnProperty(name = "forms.scenario.attachments.database.dbType", havingValue = "hana")
    @ConditionalOnExpression("'${forms.scenario.data.database.dbType:postgres}'.equals('hana') or '${forms.scenario.attachments.database.dbType:postgres}'.equals('hana')")
    public Dialect hanaJdbcDialect() {
        return AnsiDialect.INSTANCE;
    }
}