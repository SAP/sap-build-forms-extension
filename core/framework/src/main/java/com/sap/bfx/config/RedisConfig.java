package com.sap.bfx.config;

import com.sap.bfx.session.FormsService;
import com.sap.bfx.session.Session;
import com.sap.bfx.session.SessionRedisSerializer;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DnsResolvers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Provides the forms-session Redis connection factory and {@code session-redis-template}
 * for {@link com.sap.bfx.session.SessionService}.
 *
 * <p>When {@code forms.session.redis.ssl=true} (Kyma / AWS ElastiCache, which enforces
 * in-transit TLS), a Lettuce connection factory with JVM DNS resolution is used.
 * Netty's default async DNS resolver fails on Kubernetes {@code ndots:5} with the
 * ElastiCache hostname; switching to JVM DNS fixes resolution.
 *
 * <p>When {@code ssl=false} (local dev), a plain Jedis factory is used.
 *
 * <p>{@code session-redis-template} uses {@link ConditionalOnMissingBean} so that
 * a downstream app can still override it if needed without requiring
 * {@code spring.main.allow-bean-definition-overriding=true}.
 */
@Configuration
@Slf4j
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
     * When true (Kyma / ElastiCache), uses Lettuce + TLS + JVM DNS resolver.
     * When false (local dev), uses plain Jedis.
     */
    @Value("${forms.session.redis.ssl:false}")
    private boolean ssl;

    @Autowired
    public RedisConfig(final FormsService formsService) {
        this.formsService = formsService;
    }

    /**
     * Connection factory for the forms session store.
     * Lettuce + TLS when {@code ssl=true}, plain Jedis otherwise.
     */
    @Bean
    public RedisConnectionFactory jedisConnectionFactory() {
        log.info("RedisConfig: creating {}Redis factory for '{}:{}, database:{}'",
                ssl ? "Lettuce+TLS " : "Jedis ", redisHost, redisPort, database);

        final RedisStandaloneConfiguration config =
                new RedisStandaloneConfiguration(redisHost, redisPort);
        config.setPassword(redisPassword);
        config.setDatabase(database);

        if (ssl) {
            final ClientResources clientResources = DefaultClientResources.builder()
                    .dnsResolver(DnsResolvers.JVM_DEFAULT)
                    .build();
            final LettuceClientConfiguration lettuce = LettuceClientConfiguration.builder()
                    .clientResources(clientResources)
                    .useSsl()
                    .build();
            return new LettuceConnectionFactory(config, lettuce);
        }

        return new JedisConnectionFactory(config);
    }

    /**
     * Forms session template — consumed by {@link com.sap.bfx.session.SessionService}
     * via {@code @Qualifier("session-redis-template")}.
     */
    @Bean("session-redis-template")
    @ConditionalOnMissingBean(name = "session-redis-template")
    public RedisTemplate<String, Session> redisSessionTemplate() {
        final RedisTemplate<String, Session> template = new RedisTemplate<>();
        template.setConnectionFactory(jedisConnectionFactory());
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new SessionRedisSerializer(formsService));
        return template;
    }
}
