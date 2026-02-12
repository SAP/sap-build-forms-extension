package com.sap.bfx.usermanagement.utility;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.Set;

@Setter
@Getter
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;                          // id
    private String userName;                    // userName / logon name
    private String lastname;                    // familyName
    private String firstname;                   // givenName
    private String fullName;
    private String displayName;                 // displayName
    private String email;                       // primary mail address
    private String phone;                       // telephoneNumber
    private String mobile;                      // mobile
    private String jobRole;                     // job code
    private String company;                     // company
    private String orgUnit;                     // department
    private String country;                     // co
    private String city;                        // l
    private String address;                     // physicalDeliveryOfficeName
    private String externalId;                  // externalID
    private String cn;                          // cn
    private String dn;                          // distinguishedName
    private String managerDn;                   // manager
    private String zoneId;                      // zone id
    private String locale;                      // locale
    private String userType;                    // user type
    private Set<String> groups;                 // the set of groups
    private OffsetDateTime modifyTimestamp;     // whenChanged
    private OffsetDateTime createTimestamp;     // whenCreated

    public User() {
    }

    public User(User other) {
        this.id = other.id;
        this.userName = other.userName;
        this.lastname = other.lastname;
        this.firstname = other.firstname;
        this.fullName = other.fullName;
        this.displayName = other.displayName;
        this.email = other.email;
        this.phone = other.phone;
        this.mobile = other.mobile;
        this.jobRole = other.jobRole;
        this.company = other.company;
        this.orgUnit = other.orgUnit;
        this.country = other.country;
        this.city = other.city;
        this.address = other.address;
        this.externalId = other.externalId;
        this.cn = other.cn;
        this.dn = other.dn;
        this.managerDn = other.managerDn;
        this.zoneId = other.zoneId;
        this.modifyTimestamp = other.modifyTimestamp;
        this.createTimestamp = other.createTimestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        EqualsBuilder eb = new EqualsBuilder();
        User other = (User) obj;
        eb.append(this.getId(), other.getId());
        return eb.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hb = new HashCodeBuilder();
        hb.append(this.getId());
        return hb.toHashCode();
    }

    @Override
    public String toString() {
        return this.getFullName() + " (" + this.getUserName() + "/" + this.getId() + ")";
    }
}