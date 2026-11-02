package com.sap.bfx.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.List;

@Data
public class User {
    private String id;
    private String externalId;
    private String userName;
    private UserName name;
    private String displayName;
    private String nickName;
    private String profileUrl;
    private String title;
    private String userType;
    private String preferredLanguage;
    private String locale;
    private String timezone;
    private boolean active;
    @JsonProperty("mails")
    private String[] emails;
    @JsonProperty("phones")
    private String[] phonesNumbers;
    private Address[] addresses;
    private Collection<GrantedAuthority> authorities;
    /**
     * Full AMS policy names assigned to this user at login time (e.g.
     * {@code "SBFX_StartProcess"}, {@code "SBFX_StartProcess_ScenarioA"}).
     * Stored as strings so the field is Redis-serializable.
     * Used by {@link com.sap.bfx.security.ias.IasSecurityService} to rebuild an
     * {@code Authorizations} instance per request without re-parsing the id-token.
     * Never null after a successful login; empty list if assignment lookup fails.
     */
    private List<String> assignedPolicyNames;

    @Data
    public static class UserName {
        private String familyName;
        private String givenName;
        private String formatted;
        private String middleName;
        private String honorificPrefix;
        private String honorificSuffix;
    }

    @Data
    public static class Address {
        private String type;
        private String formatted;
        private boolean primary;
        private String country;
        private String locality;
        private String postalCode;
        private String region;
        private String streetAddress;
    }
}
