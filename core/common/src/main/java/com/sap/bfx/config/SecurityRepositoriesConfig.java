package com.sap.bfx.config;

import com.sap.bfx.security.SecuritySession;
import com.sap.bfx.security.SecuritySessionRedisSerializer;
import com.sap.bfx.security.ias.IasEnabledCondition;
import com.sap.bfx.security.session.RedisRequestCache;
import com.sap.bfx.security.session.SecuritySessionEnabled;
import com.sap.bfx.utils.JsonRedisSerializer;
import io.lettuce.core.resource.ClientResources;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DnsResolvers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Provides Redis connection factories and named RedisTemplate beans for the IAS security
 * session layer (RedisRequestCache, SecuritySessionService, RedisAuthorizationRequestRepository).
 *
 * <p>When IAS/OIDC is active ({@code forms.security.session.redis.ssl=true}, set by apps
 * deployed to Kyma with ElastiCache), a Lettuce+TLS factory with JVM DNS resolution is
 * provided. For non-IAS contexts (local dev / XSUAA-only), a plain Jedis factory is used.
 *
 * <p>No app-level {@code RedisTlsConfig} override is needed. Apps only supply
 * {@code forms.security.session.redis.*} properties in their yml.
 */
@Configuration
@Slf4j
public class SecurityRepositoriesConfig {

    @Value("${forms.security.session.redis.host:#{null}}")
    private String redisHost;

    @Value("${forms.security.session.redis.port:#{null}}")
    private Integer redisPort;

    @Value("${forms.security.session.redis.password:#{null}}")
    private String redisPassword;

    @Value("${forms.security.session.redis.database:0}")
    private Integer database;

    /**
     * When true (Kyma / ElastiCache), uses Lettuce+TLS+JVM-DNS.
     * When false (local dev), uses plain Jedis.
     */
    @Value("${forms.security.session.redis.ssl:false}")
    private boolean ssl;

    /**
     * Single connection factory covering both paths:
     * - ssl=true  → Lettuce + TLS + JVM DNS resolver (required for ElastiCache on Kyma)
     * - ssl=false → plain Jedis (local dev / XSUAA-only / no ElastiCache)
     *
     * Marked {@link Primary} so it is preferred for any by-type injection
     * (e.g. valuehelp {@code lockRegistry}).
     */
    @Bean("security-session-redis-factory")
    @Conditional(SecuritySessionEnabled.class)
    @Primary
    public RedisConnectionFactory securitySessionRedisFactory() {
        log.info("SecurityRepositoriesConfig: creating {}Redis factory for '{}:{}, database:{}'",
                ssl ? "Lettuce+TLS " : "Jedis ", redisHost, redisPort, database);

        final RedisStandaloneConfiguration config = new RedisStandaloneConfiguration(redisHost, redisPort);
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

    @Bean("security-session-redis-template")
    @ConditionalOnMissingBean(name = "security-session-redis-template")
    public RedisTemplate<String, SecuritySession> securitySessionRedisTemplate() {
        log.info("SecurityRepositoriesConfig: creating security-session-redis-template for '{}:{}, database:{}'",
                redisHost, redisPort, database);

        if (StringUtils.isNotEmpty(redisHost) && redisPort != null) {
            final RedisTemplate<String, SecuritySession> template = new RedisTemplate<>();
            template.setConnectionFactory(securitySessionRedisFactory());
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new SecuritySessionRedisSerializer());
            return template;
        }
        return null;
    }

    @Bean("request-cache-redis-template")
    @ConditionalOnMissingBean(name = "request-cache-redis-template")
    public RedisTemplate<String, RedisRequestCache.SimpleSavedRequest> requestCacheRedisTemplate() {
        if (StringUtils.isNotEmpty(redisHost) && redisPort != null) {
            final RedisTemplate<String, RedisRequestCache.SimpleSavedRequest> template = new RedisTemplate<>();
            template.setConnectionFactory(securitySessionRedisFactory());
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new JsonRedisSerializer<>(RedisRequestCache.SimpleSavedRequest.class));
            return template;
        }
        return null;
    }

    @Bean("authorization-request-redis-template")
    @ConditionalOnMissingBean(name = "authorization-request-redis-template")
    public RedisTemplate<String, String> authorizationRequestRedisTemplate() {
        if (StringUtils.isNotEmpty(redisHost) && redisPort != null) {
            final RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setConnectionFactory(securitySessionRedisFactory());
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new StringRedisSerializer());
            return template;
        }
        return null;
    }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        final CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
