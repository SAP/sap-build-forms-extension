package com.sap.bfx.security.session;

import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.security.Constants;
import com.sap.bfx.security.ias.IasEnabledCondition;
import com.sap.bfx.utils.AbstractCookieHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * A custom implementation of AuthorizationRequestRepository that stores OAuth2 authorization requests in cookies.
 * This class extends AbstractCookieHandler to handle cookie operations and implements the AuthorizationRequestRepository
 * interface to manage OAuth2AuthorizationRequest objects.
 */
@Component
@Conditional(SecuritySessionEnabled.class)
@Slf4j
public class RedisAuthorizationRequestRepository extends AbstractCookieHandler
        implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private RedisTemplate<String, String> redis;

    /**
     * Constructs a new CookieAuthorizationRequestRepository with the provided RedisTemplate.
     *
     * @param appCtx the application context used to retrieve the RedisTemplate bean
     */
    @Autowired
    public RedisAuthorizationRequestRepository(final ApplicationContext appCtx) {
        log.info("Requesting RedisTemplate 'authorization-request-redis-template'");
        try {
            this.redis = appCtx.getBean("authorization-request-redis-template", RedisTemplate.class);
        } catch (Exception ignored) {
        }

        if (this.redis == null) {
            // redis is necessary for IAS authentication, so we check with the condition if this is enabled
            if (IasEnabledCondition.matches(appCtx.getEnvironment())) {
                throw ExceptionUtils.from(
                        "no redis template for request cache found, but IAS is enabled. Please check your configuration.");
            }
        }
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        // Retrieve and deserialize cookie
        final var cookie = AbstractCookieHandler.getCookie(request, Constants.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME);
        if (cookie == null) {
            return null;
        }
        final var id = cookie.getValue();
        if (id != null) {
            final var result = redis.boundValueOps(id).get();
            if (result != null) {
                // TODO(ML) for some reasons the value returned by REDIS has leading bytes with value 0. These
                // are trimmed before decoding.
                return decodeValue(StringUtils.trim(result), OAuth2AuthorizationRequest.class);
            }
        }
        return null;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest, HttpServletRequest request,
                                         HttpServletResponse response) {
        if (authorizationRequest == null) {
            removeAuthorizationRequest(request, response);
            return;
        }
        // Serialize, store in redis and save to cookie
        final var id = StringUtils.remove(UUID.randomUUID().toString(), '-');
        final var value = encodeValue(authorizationRequest);
        redis.boundValueOps(id).set(value, Constants.TEMP_TIMEOUT);

        addCookie(request, response, Constants.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, id, true, true,
                Constants.TEMP_TIMEOUT);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                                 HttpServletResponse response) {
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        // Delete cookie by setting max age to 0
        addCookie(request, response, Constants.OAUTH2_AUTHORIZATION_REQUEST_COOKIE_NAME, "", true, true, 0);

        return authorizationRequest;
    }
}