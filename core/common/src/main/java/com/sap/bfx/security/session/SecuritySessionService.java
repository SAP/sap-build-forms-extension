package com.sap.bfx.security.session;

import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.security.SecuritySession;
import com.sap.bfx.security.ias.IasEnabledCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

/**
 * Repository class for managing SecuritySession objects in Redis.
 */
@Service
public class SecuritySessionService {

    private JwtDecoder jwtDecoder;
    private RedisTemplate<String, SecuritySession> redis;
    private final ApplicationContext appCtx;

    /**
     * Constructs a new SecuritySessionRepository with the provided RedisTemplate and JwtDecoder.
     *
     * @param appCtx the application context used to retrieve the RedisTemplate bean
     */
    @Autowired
    public SecuritySessionService(ApplicationContext appCtx) {
        // Store context for lazy bean resolution — do NOT resolve beans here,
        // as AMS auto-configuration forces early initialisation before all beans are ready.
        this.appCtx = appCtx;
    }

    /**
     * Lazily resolve the JwtDecoder and RedisTemplate on first use.
     * This avoids startup ordering issues introduced by spring-boot-starter-ams.
     */
    private void ensureInitialized() {
        if (this.jwtDecoder == null) {
            try {
                this.jwtDecoder = appCtx.getBean(JwtDecoder.class);
            } catch (Exception ignored) {
            }
            if (this.jwtDecoder == null && IasEnabledCondition.matches(appCtx.getEnvironment())) {
                throw ExceptionUtils.from(
                        "no jwt decoder found, but IAS is enabled. Please check your configuration.");
            }
        }
        if (this.redis == null) {
            try {
                this.redis = appCtx.getBean("security-session-redis-template", RedisTemplate.class);
            } catch (Exception ignored) {
            }
            if (this.redis == null && IasEnabledCondition.matches(appCtx.getEnvironment())) {
                throw ExceptionUtils.from(
                        "no redis template for request cache found, but IAS is enabled. Please check your configuration.");
            }
        }
    }

    /**
     * Finds a SecuritySession by its session ID in Redis.
     *
     * @param sessionId the ID of the session to find
     * @return the SecuritySession associated with the provided session ID
     * @throws JwtException if the JWT token in the session is invalid or cannot be decoded
     */
    public SecuritySession findSessionById(final String sessionId) throws JwtException {
        ensureInitialized();
        final var session = redis.boundValueOps(sessionId).get();
        if (session != null) {
            session.decodeToken(jwtDecoder);
        }
        return session;
    }

    /**
     * Saves the provided SecuritySession in Redis with an expiration time based on the JWT token's expiration.
     *
     * @param session the SecuritySession to save
     */
    public void saveSession(final SecuritySession session) {
        ensureInitialized();
        final var timeout = Duration.between(Instant.now(), session.getToken().getExpiresAt());
        // reduce the timeout to ensure that token is valid for short time after it is taken from repo
        timeout.minus(Duration.ofSeconds(2));
        // store value
        redis.boundValueOps(session.getId()).set(session, timeout);
    }

    /**
     * Deletes the SecuritySession associated with the provided session ID from Redis.
     *
     * @param sessionId the ID of the session to delete
     * @return the deleted SecuritySession, or null if no session was found for the given ID
     */
    public SecuritySession deleteSession(final String sessionId) {
        ensureInitialized();
        final var session = redis.boundValueOps(sessionId).getAndDelete();
        if (session != null) {
            session.decodeToken(jwtDecoder);
        }
        return session;
    }
}
