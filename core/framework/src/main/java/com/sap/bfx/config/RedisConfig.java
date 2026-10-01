package com.sap.bfx.config;

import com.sap.bfx.session.FormsService;
import com.sap.bfx.session.Session;
import com.sap.bfx.session.SessionRedisSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    private final FormsService formsService;
    @Value("${forms.session.redis.host}")
    private String redisHost;
    @Value("${forms.session.redis.port}")
    private Integer redisPort;
    @Value("${forms.session.redis.password}")
    private String redisPassword;
    @Value("${forms.session.redis.database}")
    private Integer database;

    /**
     * Constructor-based dependency injection for DefinitionService and FormsService.
     */
    @Autowired
    public RedisConfig(final FormsService formsService) {
        this.formsService = formsService;
    }

    /**
     * Configures the RedisConnectionFactory using Jedis.
     *
     * @return a configured RedisConnectionFactory
     */
    @Bean
    public RedisConnectionFactory jedisConnectionFactory() {
        RedisStandaloneConfiguration redisConfiguration = new RedisStandaloneConfiguration(redisHost, redisPort);
        redisConfiguration.setPassword(redisPassword);
        redisConfiguration.setDatabase(database);

        return new JedisConnectionFactory(redisConfiguration);
    }

    /**
     * Configures the RedisTemplate for Session objects.
     *
     * @return a configured RedisTemplate
     */
    @Bean("session-redis-template")
    public RedisTemplate<String, Session> redisSessionTemplate() {
        RedisTemplate<String, Session> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new SessionRedisSerializer(formsService));
        return template;
    }
}
