package com.sap.bfx.security.ias.scim;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.bfx.security.User;
import lombok.Data;

@Data
public class ScimUser extends User {
    @JsonProperty("emails")
    private Info[] scimEmails;
    @JsonProperty("phoneNumbers")
    private Info[] scimPhoneNumbers;
    private Info[] roles;
    private Info[] groups;

    @Data
    public static class Info {
        private String type;
        private String value;
        private String display;
        private boolean primary;
    }
}
