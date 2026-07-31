package com.sap.bfx.security.oidc;

import com.sap.bfx.security.SecuritySession;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/**
 * Repository class for managing SecuritySession objects in Redis.
 */
@Service
public class SecuritySessionService {

    private final RedisTemplate<String, SecuritySession> redis;
    private final JwtDecoder jwtDecoder;

    /**
     * Constructs a new SecuritySessionRepository with the provided RedisTemplate and JwtDecoder.
     *
     * @param redis      the RedisTemplate for interacting with Redis
     * @param jwtDecoder the JwtDecoder for decoding JWT tokens
     */
    @Autowired
    public SecuritySessionService(
            @Qualifier("security-session-redis-template") RedisTemplate<String, SecuritySession> redis,
            JwtDecoder jwtDecoder) {
        this.redis = redis;
        this.jwtDecoder = jwtDecoder;
    }

    /**
     * Finds a SecuritySession by its session ID in Redis.
     *
     * @param sessionId the ID of the session to find
     * @return the SecuritySession associated with the provided session ID
     * @throws JwtException if the JWT token in the session is invalid or cannot be decoded
     */
    public SecuritySession findSessionById(final String sessionId) throws JwtException {
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
        final var session = redis.boundValueOps(sessionId).getAndDelete();
        if (session != null) {
            session.decodeToken(jwtDecoder);
        }
        return session;
    }

    /**
     * Creates a new SecuritySession with a unique ID and the provided JWT token.
     *
     * @param tokenValue the JWT token value to associate with the new session
     * @return a new SecuritySession instance
     */
    public SecuritySession createSession(String tokenValue) {
        final var session =
                new SecuritySession(StringUtils.remove(UUID.randomUUID().toString(), '-'), tokenValue, null);
        session.decodeToken(jwtDecoder);
        return session;
    }
}
