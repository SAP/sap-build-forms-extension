package com.sap.bfx.btp;

import com.sap.cloud.sdk.cloudplatform.connectivity.*;
import com.sap.cloud.sdk.cloudplatform.connectivity.exception.DestinationAccessException;
import com.sap.cloud.sdk.cloudplatform.connectivity.exception.DestinationNotFoundException;
import com.sap.cloud.sdk.cloudplatform.resilience.ResilienceConfiguration;
import com.sap.cloud.sdk.cloudplatform.security.AuthTokenAccessor;
import com.sap.cloud.sdk.cloudplatform.security.principal.PrincipalAccessor;
import com.sap.cloud.sdk.cloudplatform.tenant.TenantAccessor;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;
import com.sap.bfx.exception.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for accessing SAP BTP Destinations and creating ApiClient instances.
 * Provides methods to retrieve HTTP destinations, including those that forward user tokens,
 * and to create ApiClient instances with specified base paths and authorization tokens.
 */
@Slf4j
public final class ConnectivityUtils {

    private final static String PROPERTY_SCENARIO_NAME = "scenario-name";

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ConnectivityUtils() {
    }

    /**
     * Retrieves an HTTP destination by its name.
     *
     * @param destinationName the name of the destination to retrieve
     * @return the HTTP destination
     */
    public static HttpDestination getHttpDestination(String destinationName) {
        return getDestination(destinationName).asHttp();
    }

    /**
     * Retrieves an HTTP destination that forwards the user token.
     * Logs the headers of the retrieved destination for debugging purposes.
     *
     * @param destinationName the name of the destination to retrieve
     * @return the HTTP destination with forwarded user token
     */
    public static HttpDestination getHttpDestinationWithForwardUserToken(String destinationName) {
        HttpDestination httpDestination = getDestinationWithForwardUserToken(destinationName).asHttp();
        log.trace("Headers: {}", httpDestination.getHeaders().stream().map(h -> h.getName() + " " + h.getValue()).collect(Collectors.joining(", ")));
        return httpDestination;
    }

    /**
     * Creates an ApiClient instance with the specified base path and authorization token.
     * Sets the "Authorization" header with the provided token.
     *
     * @param basePath the base path for the ApiClient
     * @param token    the authorization token to be included in the header
     * @return a configured ApiClient instance
     */
    public static ApiClient getApiClient(String basePath, String token) {
        ApiClient sbpaApiClient = new ApiClient();
        sbpaApiClient.setBasePath(basePath);
        sbpaApiClient.addDefaultHeader("Authorization", "Bearer " + token);
        return sbpaApiClient;
    }

    /**
     * Retrieves a destination by its name.
     * Disables caching for the destination service and sets a time limiter configuration.
     * Uses a custom destination loader to attempt to get the destination.
     *
     * @param destinationName the name of the destination to retrieve
     * @return the retrieved Destination object
     * @throws DestinationAccessException   if there is an issue accessing the destination
     * @throws DestinationNotFoundException if the destination is not found
     */
    public static Destination getDestination(String destinationName) {
        DestinationService.Cache.disable();
        DestinationService service = DestinationService.builder().withTimeLimiterConfiguration(ResilienceConfiguration.TimeLimiterConfiguration.of(Duration.ofSeconds(10L))).build();
        DestinationAccessor.prependDestinationLoader(service);
        //return service.tryGetDestination(destinationName).getOrElseThrow((failure) -> {
        return DestinationAccessor.getLoader().tryGetDestination(destinationName).getOrElseThrow((failure) -> {
            //Destination theDestination = service.tryGetDestination(destinationName).getOrElseThrow((failure) -> {
            if (failure instanceof DestinationAccessException || failure instanceof DestinationNotFoundException) {
                throw ExceptionUtils.from(failure);
            } else {
                String msg = "Failed to get destination with name '" + destinationName + "'.";
                throw ExceptionUtils.from(new DestinationAccessException(msg, failure));
            }
        });
    }

    /**
     * Retrieves a destination by its name, forwarding the user token.
     * Logs the current authentication token, tenant, and principal for debugging purposes.
     * Disables caching for the destination service and sets a time limiter configuration.
     * Uses specific retrieval and token exchange strategies to always use the provider and forward the user token.
     *
     * @param destinationName the name of the destination to retrieve
     * @return the retrieved Destination object with forwarded user token
     * @throws DestinationAccessException   if there is an issue accessing the destination
     * @throws DestinationNotFoundException if the destination is not found
     */
    public static Destination getDestinationWithForwardUserToken(String destinationName) {
        log.trace("AuthTokenAccessor.getCurrentToken():{}", AuthTokenAccessor.getCurrentToken().getJwt().getToken());
        log.trace("TenantAccessor.getCurrentTenant():{}", TenantAccessor.getCurrentTenant());
        log.trace("PrincipalAccessor.getCurrentPrincipal():{}", PrincipalAccessor.getCurrentPrincipal());
        DestinationService.Cache.disable();
        DestinationService service = DestinationService.builder().withTimeLimiterConfiguration(ResilienceConfiguration.TimeLimiterConfiguration.of(Duration.ofSeconds(10L))).build();
        DestinationOptions options = DestinationOptions.builder()
                .augmentBuilder(
                        DestinationServiceOptionsAugmenter.augmenter().retrievalStrategy(DestinationServiceRetrievalStrategy.ALWAYS_PROVIDER))
                .augmentBuilder(
                        DestinationServiceOptionsAugmenter.augmenter().tokenExchangeStrategy(DestinationServiceTokenExchangeStrategy.FORWARD_USER_TOKEN))
                .build();
        return service.tryGetDestination(destinationName, options).getOrElseThrow((failure) -> {
            if (failure instanceof DestinationAccessException) {
                throw (DestinationAccessException) failure;
            } else if (failure instanceof DestinationNotFoundException) {
                throw (DestinationNotFoundException) failure;
            } else {
                String msg = "Failed to get destination with name '" + destinationName + "'.";
                throw new DestinationAccessException(msg, failure);
            }
        });
    }

    /**
     * Retrieves the properties of a destination by its name.
     * Disables caching for the destination service and sets a time limiter configuration.
     *
     * @param destinationName the name of the destination whose properties are to be retrieved
     * @return the DestinationProperties object containing the properties of the destination
     */
    public static DestinationProperties getDestinationProperties(String destinationName) {
        DestinationService.Cache.disable();
        DestinationService service = DestinationService.builder().withTimeLimiterConfiguration(ResilienceConfiguration.TimeLimiterConfiguration.of(Duration.ofSeconds(10L))).build();
        return service.getDestinationProperties(destinationName);
    }

    /**
     * Retrieves a map of all scenario names to their corresponding URLs from the available destinations.
     * Disables caching for the destination service and sets a time limiter configuration.
     *
     * @return a map where keys are scenario names and values are their corresponding URLs
     */
    public static Map<String, String> getAllScenarioUrls() {
        DestinationService.Cache.disable();
        final var service = DestinationService.builder().withTimeLimiterConfiguration(ResilienceConfiguration
                .TimeLimiterConfiguration.of(Duration.ofSeconds(10L))).build();

        final Map<String, String> scenarioUrlsMap = new HashMap<>();
        for (var dp : service.getAllDestinationProperties()) {
            final var scenarioNameOpt = dp.get(PROPERTY_SCENARIO_NAME, String.class);
            if (!scenarioNameOpt.isEmpty()) {
                scenarioUrlsMap.put(scenarioNameOpt.get(), dp.get(DestinationProperty.URI).get());
            }
        }

        return scenarioUrlsMap;
    }
}
