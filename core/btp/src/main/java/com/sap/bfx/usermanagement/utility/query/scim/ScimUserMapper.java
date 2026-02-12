package com.sap.bfx.usermanagement.utility.query.scim;

import com.sap.bfx.usermanagement.utility.User;
import com.sap.openapi.scim.model.Group;
import com.sap.openapi.scim.model.ScimUser;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * mapper for SCIM user
 */
public final class ScimUserMapper {

    public static final String SCIM_ATTRIBUTE_ID = "id";
    public static final String SCIM_ATTRIBUTE_USER_NAME = "userName";
    public static final String SCIM_ATTRIBUTE_EXTERNAL_ID = "externalId";
    public static final String SCIM_ATTRIBUTE_FAMILY_NAME = "familyName";
    public static final String SCIM_ATTRIBUTE_GIVEN_NAME = "givenName";
    public static final String SCIM_ATTRIBUTE_ZONE_ID = "zoneId";
    public static final String SCIM_ATTRIBUTE_EMAIL = "email";
    public static final String SCIM_ATTRIBUTE_PHONE = "telephoneNumber";
    public static final String SCIM_ATTRIBUTE_MOBILE = "mobile";
    public static final String SCIM_ATTRIBUTE_JOBROLE = "description";
    public static final String SCIM_ATTRIBUTE_COMPANY = "company";
    public static final String SCIM_ATTRIBUTE_ORGUNIT = "department";
    public static final String SCIM_ATTRIBUTE_COUNTRY = "co";
    public static final String SCIM_ATTRIBUTE_CITY = "l";
    public static final String SCIM_ATTRIBUTE_ADDRESS = "physicalDeliveryOfficeName";
    public static final String SCIM_ATTRIBUTE_CN = "cn";
    public static final String SCIM_ATTRIBUTE_DN = "distinguishedName";
    public static final String SCIM_ATTRIBUTE_MANAGERDN = "manager";
    public static final String SCIM_ATTRIBUTE_DISPLAY_NAME = "displayName";
    public static final String SCIM_ATTRIBUTE_LOCALE = "locale";
    public static final String SCIM_ATTRIBUTE_GROUPS = "groups";
    public static final String SCIM_ATTRIBUTE_EMAILS = "emails";
    public static final String SCIM_ATTRIBUTE_ORIGIN = "origin";
    public static final String SCIM_ATTRIBUTE_MODIFYTIMESTAMP = "lastModified";
    public static final String SCIM_ATTRIBUTE_CREATETIMESTAMP = "created";

    private static final ScimUserMapper INSTANCE = new ScimUserMapper();

    private ScimUserMapper() {
    }

    public static ScimUserMapper getInstance() {
        return INSTANCE;
    }

    public String[] getAttributesForUser() {
        String[] attributes = getAttributes();
        return Arrays.copyOf(attributes, attributes.length);
    }

    private String[] getAttributes() {
        return new String[]{SCIM_ATTRIBUTE_ID, SCIM_ATTRIBUTE_USER_NAME, SCIM_ATTRIBUTE_EXTERNAL_ID, SCIM_ATTRIBUTE_FAMILY_NAME, SCIM_ATTRIBUTE_GIVEN_NAME, SCIM_ATTRIBUTE_ZONE_ID,
                SCIM_ATTRIBUTE_EMAIL, SCIM_ATTRIBUTE_PHONE, SCIM_ATTRIBUTE_MOBILE, SCIM_ATTRIBUTE_JOBROLE, SCIM_ATTRIBUTE_COMPANY, SCIM_ATTRIBUTE_ORGUNIT, SCIM_ATTRIBUTE_COUNTRY, SCIM_ATTRIBUTE_CITY, SCIM_ATTRIBUTE_ADDRESS, SCIM_ATTRIBUTE_CN, SCIM_ATTRIBUTE_DN, SCIM_ATTRIBUTE_MANAGERDN, SCIM_ATTRIBUTE_DISPLAY_NAME, SCIM_ATTRIBUTE_LOCALE, SCIM_ATTRIBUTE_ORIGIN, SCIM_ATTRIBUTE_MODIFYTIMESTAMP, SCIM_ATTRIBUTE_CREATETIMESTAMP};
    }

    public User mapToUser(ScimUser scimUser) {
        User user = new User();
        user.setId(scimUser.getId());
        user.setUserName(scimUser.getUserName());
        user.setLastname(scimUser.getName().getFamilyName());
        user.setFirstname(scimUser.getName().getGivenName());
        user.setFullName(scimUser.getName().getGivenName() + " " + scimUser.getName().getFamilyName());
        user.setDisplayName(scimUser.getDisplayName());
        user.setEmail((!scimUser.getEmails().isEmpty()) ? scimUser.getEmails().get(0).getValue() : null);
        user.setPhone((!scimUser.getPhoneNumbers().isEmpty()) ? scimUser.getPhoneNumbers().get(0).getValue() : null);
        user.setMobile((scimUser.getCustomFieldNames().contains(SCIM_ATTRIBUTE_MOBILE)) ? StringUtils.trim(Objects.toString(scimUser.getCustomField(SCIM_ATTRIBUTE_MOBILE), "")) : null);
        user.setJobRole((scimUser.getCustomFieldNames().contains(SCIM_ATTRIBUTE_JOBROLE)) ? StringUtils.trim(Objects.toString(scimUser.getCustomField(SCIM_ATTRIBUTE_JOBROLE), "")) : null);
        user.setCompany((scimUser.getCustomFieldNames().contains(SCIM_ATTRIBUTE_COMPANY)) ? StringUtils.trim(Objects.toString(scimUser.getCustomField(SCIM_ATTRIBUTE_COMPANY), "")) : null);
        user.setOrgUnit((scimUser.getCustomFieldNames().contains(SCIM_ATTRIBUTE_ORGUNIT)) ? StringUtils.trim(Objects.toString(scimUser.getCustomField(SCIM_ATTRIBUTE_ORGUNIT), "")) : null);
        user.setCountry((scimUser.getCustomFieldNames().contains(SCIM_ATTRIBUTE_COUNTRY)) ? StringUtils.trim(Objects.toString(scimUser.getCustomField(SCIM_ATTRIBUTE_COUNTRY), "")) : null);
        user.setCity((scimUser.getCustomFieldNames().contains(SCIM_ATTRIBUTE_CITY)) ? StringUtils.trim(Objects.toString(scimUser.getCustomField(SCIM_ATTRIBUTE_CITY), "")) : null);
        user.setAddress((scimUser.getCustomFieldNames().contains(SCIM_ATTRIBUTE_ADDRESS)) ? StringUtils.trim(Objects.toString(scimUser.getCustomField(SCIM_ATTRIBUTE_ADDRESS), "")) : null);
        user.setCn((scimUser.getCustomFieldNames().contains(SCIM_ATTRIBUTE_CN)) ? StringUtils.trim(Objects.toString(scimUser.getCustomField(SCIM_ATTRIBUTE_CN), "")) : null);
        user.setDn((scimUser.getCustomFieldNames().contains(SCIM_ATTRIBUTE_DN)) ? StringUtils.trim(Objects.toString(scimUser.getCustomField(SCIM_ATTRIBUTE_DN), "")) : null);
        user.setManagerDn((scimUser.getCustomFieldNames().contains(SCIM_ATTRIBUTE_MANAGERDN)) ? StringUtils.trim(Objects.toString(scimUser.getCustomField(SCIM_ATTRIBUTE_MANAGERDN), "")) : null);
        user.setZoneId(scimUser.getZoneId());
        user.setExternalId(scimUser.getExternalId());
        user.setLocale(scimUser.getLocale());
        user.setModifyTimestamp(scimUser.getMeta().getLastModified());
        user.setCreateTimestamp(scimUser.getMeta().getCreated());
        user.setGroups(scimUser.getGroups().parallelStream().map(Group::getValue).collect(Collectors.toSet()));
        return user;
    }

    private Date parseTimestamp(String timestamp) {
        if (timestamp == null) {
            return null;
        }
        // example: 20120730110853.0Z
        try {
            return new SimpleDateFormat("yyyyMMddHHmmss.S'Z'").parse(timestamp);
        } catch (ParseException e) {
            return null;
        }
    }
}
