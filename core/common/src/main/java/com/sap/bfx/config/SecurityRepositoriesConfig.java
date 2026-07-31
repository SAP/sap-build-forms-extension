package com.sap.bfx.config;

import com.sap.bfx.security.SecuritySession;
import com.sap.bfx.security.oidc.RedisRequestCache;
import com.sap.bfx.utils.JsonRedisSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@Slf4j
public class SecurityRepositoriesConfig {

    @Value("${forms.security.session.redis.host:#{null}}")
    private String redisHost;
    @Value("${forms.security.session.redis.port:#{null}}")
    private Integer redisPort;
    @Value("${forms.security.session.redis.password:#{null}}")
    private String redisPassword;
    @Value("${forms.security.session.redis.database:#{null}}")
    private Integer database;

    /**
     * Configures the RedisConnectionFactory using Jedis.
     *
     * @return a configured RedisConnectionFactory
     */
    @Bean("security-session-redis-factory")
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
    @Bean("security-session-redis-template")
    public RedisTemplate<String, SecuritySession> securitySessionRedisTemplate() {
        log.info("Creating RedisTemplate for '{}:{}, database:{}'", redisHost, redisPort, database);

        if (StringUtils.isNotEmpty(redisHost) && redisPort != null) {
            RedisTemplate<String, SecuritySession> template = new RedisTemplate<>();
            template.setConnectionFactory(jedisConnectionFactory());
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new JsonRedisSerializer<>(SecuritySession.class));
            return template;
        }
        return null;
    }

    /**
     * Configures the RedisTemplate for SimpleSavedRequest objects.
     *
     * @return a configured RedisTemplate
     */
    @Bean("request-cache-redis-template")
    public RedisTemplate<String, RedisRequestCache.SimpleSavedRequest> requestCacheRedisTemplate() {
        if (StringUtils.isNotEmpty(redisHost) && redisPort != null) {
            RedisTemplate<String, RedisRequestCache.SimpleSavedRequest> template = new RedisTemplate<>();
            template.setConnectionFactory(jedisConnectionFactory());
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new JsonRedisSerializer<>(RedisRequestCache.SimpleSavedRequest.class));
            return template;
        }
        return null;
    }

    /**
     * Configures the RedisTemplate for OAuth2AuthorizationRequest objects.
     *
     * @return a configured RedisTemplate
     */
    @Bean("authorization-request-redis-template")
    public RedisTemplate<String, String> authorizationRequestRedisTemplate() {
        if (StringUtils.isNotEmpty(redisHost) && redisPort != null) {
            RedisTemplate<String, String> template = new RedisTemplate<>();
            template.setConnectionFactory(jedisConnectionFactory());
            template.setKeySerializer(new StringRedisSerializer());
            template.setValueSerializer(new StringRedisSerializer());
            return template;
        }
        return null;
    }

    @Bean
    UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "DELETE", "PUT"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
