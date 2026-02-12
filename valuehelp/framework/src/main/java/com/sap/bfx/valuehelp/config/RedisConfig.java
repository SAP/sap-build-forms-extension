package com.sap.bfx.valuehelp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.integration.support.locks.ExpirableLockRegistry;

import java.time.Duration;

@Configuration
public class RedisConfig {

    @Value("${forms.valuehelp.locking.redis.host}")
    private String redisHost;

    @Value("${forms.valuehelp.locking.redis.port}")
    private Integer redisPort;

    @Value("${forms.valuehelp.locking.redis.password}")
    private String redisPassword;

    @Value("${forms.valuehelp.locking.redis.database}")
    private Integer database;

    // read expire from application.yml; if not set: default 10 minutes
    @Value("${forms.valuehelp.locking.expire:10m}")
    private Duration lockExpire;

    @Autowired
    public RedisConfig() {
    }

    @Bean
    public LettuceConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
        redisConfiguration.setPassword(redisPassword);
        redisConfiguration.setDatabase(database);

        return new LettuceConnectionFactory(redisConfiguration);
    }

    @Bean
    public ExpirableLockRegistry lockRegistry(RedisConnectionFactory redisConnectionFactory) {
        return new RedisLockRegistry(redisConnectionFactory,
                "FORMS_LOCKS",
                lockExpire.toMillis());
    }
}
