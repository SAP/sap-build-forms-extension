package com.sap.bfx.usermanagement.utility.query.ldap;

import com.sap.bfx.usermanagement.exception.UserManagementRuntimeException;
import com.sap.bfx.usermanagement.utility.User;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.SearchResult;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;

/**
 * mapper for ldap user
 */
public final class LdapUserMapper {

    public static final String LDAP_ATTRIBUTE_EMPLOYEEID = "employeeID";
    public static final String LDAP_ATTRIBUTE_USERPRINCIPALNAME = "userPrincipalName";
    public static final String LDAP_ATTRIBUTE_LASTNAME = "sn";
    public static final String LDAP_ATTRIBUTE_FIRSTNAME = "givenName";
    public static final String LDAP_ATTRIBUTE_DISPLAYNAME = "displayName";
    public static final String LDAP_ATTRIBUTE_EMAIL = "mail";
    public static final String LDAP_ATTRIBUTE_PHONE = "telephoneNumber";
    public static final String LDAP_ATTRIBUTE_MOBILE = "mobile";
    public static final String LDAP_ATTRIBUTE_JOBROLE = "description";
    public static final String LDAP_ATTRIBUTE_COMPANY = "company";
    public static final String LDAP_ATTRIBUTE_ORGUNIT = "department";
    public static final String LDAP_ATTRIBUTE_COUNTRY = "co";
    public static final String LDAP_ATTRIBUTE_CITY = "l";
    public static final String LDAP_ATTRIBUTE_ADDRESS = "physicalDeliveryOfficeName";
    public static final String LDAP_ATTRIBUTE_CN = "cn";
    public static final String LDAP_ATTRIBUTE_DN = "distinguishedName";
    public static final String LDAP_ATTRIBUTE_MANAGERDN = "manager";
    public static final String LDAP_ATTRIBUTE_USERTYPE = "objectClass";
    public static final String LDAP_ATTRIBUTE_MODIFYTIMESTAMP = "whenChanged";
    public static final String LDAP_ATTRIBUTE_CREATETIMESTAMP = "whenCreated";
    private static final LdapUserMapper INSTANCE = new LdapUserMapper();

    private LdapUserMapper() {
    }

    public static LdapUserMapper getInstance() {
        return INSTANCE;
    }

    /**
     * attributelist necessary for user
     *
     * @return
     */
    public String[] getAttributesForUser() {
        String[] attributes = getAttributes();
        return Arrays.copyOf(attributes, attributes.length);
    }

    private String[] getAttributes() {
        return new String[]{LDAP_ATTRIBUTE_EMPLOYEEID, LDAP_ATTRIBUTE_USERPRINCIPALNAME, LDAP_ATTRIBUTE_LASTNAME, LDAP_ATTRIBUTE_FIRSTNAME, LDAP_ATTRIBUTE_DISPLAYNAME, LDAP_ATTRIBUTE_EMAIL,
                LDAP_ATTRIBUTE_PHONE, LDAP_ATTRIBUTE_MOBILE, LDAP_ATTRIBUTE_JOBROLE, LDAP_ATTRIBUTE_COMPANY, LDAP_ATTRIBUTE_ORGUNIT, LDAP_ATTRIBUTE_COUNTRY, LDAP_ATTRIBUTE_CITY,
                LDAP_ATTRIBUTE_ADDRESS, LDAP_ATTRIBUTE_CN, LDAP_ATTRIBUTE_DN, LDAP_ATTRIBUTE_MANAGERDN, LDAP_ATTRIBUTE_USERTYPE,
                LDAP_ATTRIBUTE_MODIFYTIMESTAMP, LDAP_ATTRIBUTE_CREATETIMESTAMP};
    }

    /**
     * maps search-result to User
     *
     * @param ldapResult
     * @return
     */
    public User mapToUser(SearchResult ldapResult) {
        try {
            Attributes attribs = ldapResult.getAttributes();
            User user = new User();
            user.setId(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_EMPLOYEEID));
            user.setUserName(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_USERPRINCIPALNAME));
            user.setLastname(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_LASTNAME));
            user.setFirstname(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_FIRSTNAME));
            user.setFullName(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_FIRSTNAME) + " " + this.getAttributeValue(attribs, LDAP_ATTRIBUTE_LASTNAME));
            user.setDisplayName(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_DISPLAYNAME));
            user.setEmail(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_EMAIL));
            user.setPhone(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_PHONE));
            user.setMobile(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_MOBILE));
            user.setJobRole(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_JOBROLE));
            user.setCompany(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_COMPANY));
            user.setOrgUnit(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_ORGUNIT));
            user.setCountry(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_COUNTRY));
            user.setCity(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_CITY));
            user.setAddress(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_ADDRESS));
            user.setCn(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_CN));
            String dn = this.getAttributeValue(attribs, LDAP_ATTRIBUTE_DN);
            user.setDn((dn != null) ? dn : ldapResult.getNameInNamespace());
            user.setManagerDn(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_MANAGERDN));
            if (null != this.getAttributeValue(attribs, LDAP_ATTRIBUTE_USERTYPE)) {
                String attVal = this.getAttributeValue(attribs, LDAP_ATTRIBUTE_USERTYPE);
                if (null != attVal && null != this.getUserType(attVal)) {
                    LdapUserType uType = this.getUserType(attVal);
                    user.setUserType((null != uType) ? uType.getUsrType() : null);
                }
            }
            user.setModifyTimestamp(this.parseOffsetDateTime(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_MODIFYTIMESTAMP)));
            user.setCreateTimestamp(this.parseOffsetDateTime(this.getAttributeValue(attribs, LDAP_ATTRIBUTE_CREATETIMESTAMP)));
            return user;
        } catch (NamingException e) {
            throw new UserManagementRuntimeException("mapping to user failed due naming", e);
        }
    }

    private OffsetDateTime parseOffsetDateTime(String timestamp) {
        if (timestamp == null) {
            return null;
        }
        Date parsedDate = parseTimestamp(timestamp);
        if (null != parsedDate) {
            return parsedDate.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        return null;
    }

    private Date parseTimestamp(String timestamp) {
        if (timestamp == null) {
            return null;
        }
        // Beispiel: 20120730110853.0Z
        try {
            return new SimpleDateFormat("yyyyMMddHHmmss.S'Z'").parse(timestamp);
        } catch (ParseException e) {
            return null;
        }
    }

    private LdapUserType getUserType(String ldapUserTypes) {
        String[] types = ldapUserTypes.split(",");
        for (int i = 0; i < types.length; i++) {
            types[i] = StringUtils.trim(types[i]);
        }
        return (ArrayUtils.contains(types, LdapUserType.USER.getUsrType())) ? LdapUserType.USER : null;
    }

    @SuppressWarnings({"rawtypes"})
    private String getAttributeValue(Attributes attributes, String ldapName) throws NamingException {
        String value = null;
        if (attributes.get(ldapName) == null) {
            return null;
        }
        NamingEnumeration values = ((BasicAttribute) attributes.get(ldapName)).getAll();
        if (values.hasMore()) {
            Object next = values.next();
            value = next == null ? null : String.valueOf(next);
        }
        return value;
    }
}
