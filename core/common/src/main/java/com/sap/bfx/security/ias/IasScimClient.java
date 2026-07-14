package com.sap.bfx.security.ias;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

/**
 * REST client for the SAP IAS SCIM API (/scim/Users).
 *
 * <p>Used to query a user's current group memberships from IAS at runtime—
 * independently of the claims contained in the JWT. This ensures that changes
 * to group memberships in IAS take effect immediately (subject to the cache TTL),
 * without the user requiring a new token.
 *
 * <h2>Flow</h2>
 * <ol>
 *   <li>Spring Security validates the incoming JWT (signature, issuer, audience).</li>
 *   <li>{@link IasGroupsAuthoritiesConverter} calls {@code getUserGroups(sub)}.</li>
 *   <li>This client queries {@code GET /scim/Users/{sub}} from IAS.</li>
 *   <li>The groups from the SCIM response are added to the Spring
 *       {@code SecurityContext} as {@code ROLE_xxx}.</li>
 * </ol>
 *
 * <h2>Caching</h2>
 * Results are cached in-memory using Caffeine (key = user UUID).
 * Default TTL: 5 minutes – configurable via {@code ias.groups-cache.*}.
 *
 * <h2>Prerequisite</h2>
 * The IAS client ({@code clientId}/{@code clientSecret}) must be registered
 * in the IAS Admin Console as "System as Administrator" with the
 * <b>Manage Users</b> permission (read access is sufficient).
 */
@Slf4j
@Component
public class IasScimClient {

    public static final String CACHE_NAME = "ias-user-groups";

    private final RestClient restClient;

    public IasScimClient(
            RestClient.Builder builder,
            @Value("${forms.security.auth.url:#{null}}") String iasUrl,
            @Value("${forms.security.auth.clientId:#{null}}") String clientId,
            @Value("${forms.security.auth.clientSecret:#{null}}") String clientSecret) {

        String basic = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));

        this.restClient = builder
                .baseUrl(iasUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .defaultHeader(HttpHeaders.ACCEPT, "application/scim+json")
                .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the group display names for the user with the specified UUID.
     *
     * <p>The {@code userId} parameter corresponds to the {@code sub} claim of
     * the IAS JWT. In IAS, this is identical to the SCIM user ID.
     *
     * <p>Example result: {@code ["admin", "viewer"]}
     *
     * @param userId IAS user UUID (= JWT {@code sub})
     * @return immutable list of group names; empty if not found
     */
    @Cacheable(value = CACHE_NAME, key = "#userId")
    public List<String> getUserGroups(String userId) {
        log.debug("IAS SCIM – Retrieve groups for userId={}", userId);
        try {
            ScimUser user = restClient.get()
                    .uri("/scim/Users/{id}", userId)
                    .retrieve()
                    .body(ScimUser.class);

            if (user == null || user.groups() == null || user.groups().isEmpty()) {
                log.debug("IAS SCIM – no groups for userId={}", userId);
                return Collections.emptyList();
            }

            List<String> groups = user.groups().stream()
                    .map(ScimGroup::display)
                    .filter(g -> g != null && !g.isBlank())
                    .toList();

            log.debug("IAS SCIM – userId={} -> Groups={}", userId, groups);
            return groups;

        } catch (HttpClientErrorException.NotFound e) {
            log.warn("IAS SCIM – User {} not found; empty group list", userId);
            return Collections.emptyList();

        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized ex) {
            log.error("IAS SCIM – Authentication/authorization error ({}). "
                            + "Check: IAS Admin Console → Users & Authorizations → Administrators "
                            + "→ System as Administrator → Enable 'Manage Users'.",
                    ex.getStatusCode());
            return Collections.emptyList();

        } catch (Exception e) {
            log.error("IAS SCIM – unexpected error for userId={}: {}", userId, e.getMessage());
            return Collections.emptyList();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SCIM response model (subset of the IAS SCIM user resource)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Subset of the IAS SCIM user resource.
     * Unknown fields (e.g., {@code $ref}, {@code emails}, ...) are ignored.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ScimUser(
            String id,
            @JsonProperty("groups") List<ScimGroup> groups
    ) {
    }

    /**
     * Individual group entry in the SCIM user resource.
     *
     * <p>IAS provides:
     * <pre>
     * {
     *   "value":   "group-uuid",
     *   "display": "admin",
     *   "type":    "direct"   // or "indirect" for nested groups
     * }
     * </pre>
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ScimGroup(
            String value,
            String display,
            String type
    ) {
    }
}
