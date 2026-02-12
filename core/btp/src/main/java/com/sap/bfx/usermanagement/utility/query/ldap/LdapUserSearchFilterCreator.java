package com.sap.bfx.usermanagement.utility.query.ldap;

import com.sap.bfx.usermanagement.exception.IllegalQueryException;
import com.sap.bfx.usermanagement.utility.UserSearchCriteria;
import com.sap.bfx.usermanagement.utility.query.ldap.LdapCompositeCondition.CompositionType;
import com.sap.bfx.usermanagement.utility.query.ICondition;
import org.apache.commons.lang3.StringUtils;

public final class LdapUserSearchFilterCreator {

    private static final String MEMBER_OF = "memberOf";
    private static final String OBJECT_CLASS = "objectClass";
    private static final LdapUserSearchFilterCreator INSTANCE = new LdapUserSearchFilterCreator();

    private LdapUserSearchFilterCreator() {
    }

    public String toFilterString(UserSearchCriteria search) throws IllegalQueryException {
        /*
         * UserType[] userTypes = search.getUserTypes();
         * if (userTypes == null || userTypes.length == 0) {
         * userTypes = new UserType[] { UserType.USER };
         * }
         * Condition[] userTypeConditions = new Condition[userTypes.length];
         * for (int i = 0; i < userTypes.length; i++) {
         * userTypeConditions[i] = new Condition("objectClass",
         * userTypes[i].getUsrType());
         * }
         */
        ICondition condition = new LdapCompositeCondition(CompositionType.AND, new LdapCondition(OBJECT_CLASS, LdapUserType.USER.getUsrType()));
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_EMPLOYEEID, StringUtils.trim(search.getId()));
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_USERPRINCIPALNAME, StringUtils.trim(search.getUserName()));
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_LASTNAME, StringUtils.trim(search.getLastname()));
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_FIRSTNAME, StringUtils.trim(search.getFirstname()));
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_DISPLAYNAME, StringUtils.trim(search.getDisplayName()));
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_EMAIL, StringUtils.trim(search.getEmail()));
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_PHONE, StringUtils.trim(search.getPhone()));
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_MOBILE, StringUtils.trim(search.getMobile()));
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_JOBROLE, StringUtils.trim(search.getJobRole()));
        if (null != search.getCompany()) {
            if (search.getCompany().length >= 2) {
                String[] companies = search.getCompany();
                LdapCondition[] comps = new LdapCondition[companies.length];
                for (int i = 0; i < companies.length; i++) {
                    comps[i] = new LdapCondition(LdapUserMapper.LDAP_ATTRIBUTE_COMPANY, StringUtils.trim(companies[i]));
                }
                ICondition companyCondition = new LdapCompositeCondition(CompositionType.OR, comps);
                condition = LdapConditionUtil.add(condition, CompositionType.AND, companyCondition);
            } else if (search.getCompany().length == 1) {
                condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_COMPANY, StringUtils.trim(search.getCompany()[0]));
            }
        }
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_ORGUNIT, StringUtils.trim(search.getOrgUnit()));
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_COUNTRY, StringUtils.trim(search.getCountry()));
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_CITY, StringUtils.trim(search.getCity()));
        condition = LdapConditionUtil.add(condition, CompositionType.AND, LdapUserMapper.LDAP_ATTRIBUTE_ADDRESS, StringUtils.trim(search.getAddress()));
        if (null != search.getMemberOfGroups()) {
            if (search.getMemberOfGroups().length >= 2) {
                String[] memberOfGroups = search.getMemberOfGroups();
                LdapCondition[] memberOfGroupsConditions = new LdapCondition[memberOfGroups.length];
                for (int i = 0; i < memberOfGroups.length; i++) {
                    memberOfGroupsConditions[i] = new LdapCondition(MEMBER_OF, StringUtils.trim(memberOfGroups[i]));
                }
                ICondition memberOfGroupsCondition = new LdapCompositeCondition(CompositionType.OR, memberOfGroupsConditions);
                condition = LdapConditionUtil.add(condition, CompositionType.AND, memberOfGroupsCondition);
            } else if (search.getMemberOfGroups().length == 1) {
                condition = LdapConditionUtil.add(condition, CompositionType.AND, MEMBER_OF, StringUtils.trim(search.getMemberOfGroups()[0]));
            }
        }
        return condition.toQueryString();
    }

    public static LdapUserSearchFilterCreator getInstance() {
        return INSTANCE;
    }
}
