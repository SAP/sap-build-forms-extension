package com.sap.bfx.usermanagement.utility;

import com.sap.bfx.usermanagement.utility.query.ldap.LdapUserType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class UserSearchCriteria implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String userName;
    private String lastname;
    private String firstname;
    private String email;
    private String zoneId;
    private String displayName;
    private String phone;
    private String mobile;
    private String jobRole;
    private String orgUnit;
    private String country;
    private String city;
    private String address;
    private String externalId;
    private String originKey;
    private LdapUserType[] ldapUserTypes;
    private String[] company;
    private String[] memberOfGroups;
    private long maxResults = -1;

    public UserSearchCriteria() {
        super();
    }

}
