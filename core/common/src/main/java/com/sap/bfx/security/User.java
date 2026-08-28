package com.sap.bfx.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

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
