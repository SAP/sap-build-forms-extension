package com.sap.bfx.usermanagement.utility.query.scim;

import com.sap.bfx.usermanagement.exception.IllegalQueryException;
import com.sap.bfx.usermanagement.utility.UserSearchCriteria;
import com.sap.bfx.usermanagement.utility.query.ICondition;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class ScimUserSearchFilterCreator {

    private static final ScimUserSearchFilterCreator INSTANCE = new ScimUserSearchFilterCreator();
    private static final String WILDCARD = "*";
    private static final String REGEX_WILDCARD = "\\*";

    private ScimUserSearchFilterCreator() {
    }

    public static ScimUserSearchFilterCreator getInstance() {
        return INSTANCE;
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
        ICondition condition = null;
        Pair<ScimCondition.OperatorType, List<String>> idPair = trimAndSplitValue(search.getId());
        for (String value : idPair.getRight()) {
            condition = ScimConditionUtil.add(condition, ScimCompositeCondition.CompositionType.AND, ScimUserMapper.SCIM_ATTRIBUTE_ID, idPair.getLeft(), value);
        }
        Pair<ScimCondition.OperatorType, List<String>> userNamePair = trimAndSplitValue(search.getUserName());
        for (String value : userNamePair.getRight()) {
            condition = ScimConditionUtil.add(condition, ScimCompositeCondition.CompositionType.AND, ScimUserMapper.SCIM_ATTRIBUTE_USER_NAME, userNamePair.getLeft(), value);
        }
        Pair<ScimCondition.OperatorType, List<String>> lastNamePair = trimAndSplitValue(search.getLastname());
        for (String value : lastNamePair.getRight()) {
            condition = ScimConditionUtil.add(condition, ScimCompositeCondition.CompositionType.AND, ScimUserMapper.SCIM_ATTRIBUTE_FAMILY_NAME, lastNamePair.getLeft(), value);
        }
        Pair<ScimCondition.OperatorType, List<String>> firstNamePair = trimAndSplitValue(search.getFirstname());
        for (String value : firstNamePair.getRight()) {
            condition = ScimConditionUtil.add(condition, ScimCompositeCondition.CompositionType.AND, ScimUserMapper.SCIM_ATTRIBUTE_GIVEN_NAME, firstNamePair.getLeft(), value);
        }
        Pair<ScimCondition.OperatorType, List<String>> emailPair = trimAndSplitValue(search.getEmail());
        for (String value : emailPair.getRight()) {
            condition = ScimConditionUtil.add(condition, ScimCompositeCondition.CompositionType.AND, ScimUserMapper.SCIM_ATTRIBUTE_EMAIL, emailPair.getLeft(), value);
        }
        Pair<ScimCondition.OperatorType, List<String>> externalIdPair = trimAndSplitValue(search.getExternalId());
        for (String value : externalIdPair.getRight()) {
            condition = ScimConditionUtil.add(condition, ScimCompositeCondition.CompositionType.AND, ScimUserMapper.SCIM_ATTRIBUTE_EXTERNAL_ID, externalIdPair.getLeft(), value);
        }
        Pair<ScimCondition.OperatorType, List<String>> mobilePair = trimAndSplitValue(search.getMobile());
        for (String value : mobilePair.getRight()) {
            condition = ScimConditionUtil.add(condition, ScimCompositeCondition.CompositionType.AND, ScimUserMapper.SCIM_ATTRIBUTE_ZONE_ID, mobilePair.getLeft(), value);
        }
        Pair<ScimCondition.OperatorType, List<String>> originKeyPair = trimAndSplitValue(search.getOriginKey());
        for (String value : originKeyPair.getRight()) {
            condition = ScimConditionUtil.add(condition, ScimCompositeCondition.CompositionType.AND, ScimUserMapper.SCIM_ATTRIBUTE_ORIGIN, originKeyPair.getLeft(), value);
        }
        /*if (null != search.getCompany()) {
            if (search.getCompany().length >= 2) {
                String[] companies = search.getCompany();
                ScimCondition[] comps = new ScimCondition[companies.length];
                for (int i = 0; i < companies.length; i++) {
                    comps[i] = new ScimCondition(ScimUserMapper.SCIM_ATTRIBUTE_COMPANY, ScimCondition.OperatorType.EQ, StringUtils.trim(companies[i]));
                }
                ICondition companyCondition = new ScimCompositeCondition(ScimCompositeCondition.CompositionType.OR, comps);
                condition = ScimConditionUtil.add(condition, ScimCompositeCondition.CompositionType.AND, companyCondition);
            } else if (search.getCompany().length == 1) {
                condition = ScimConditionUtil.add(condition, ScimCompositeCondition.CompositionType.AND, ScimUserMapper.SCIM_ATTRIBUTE_COMPANY, ScimCondition.OperatorType.EQ, StringUtils.trim(search.getCompany()[0]));
            }
        }*/
        /*if (null != search.getMemberOfGroups()) {
            if (search.getMemberOfGroups().length >= 2) {
                String[] memberOfGroups = search.getMemberOfGroups();
                ScimCondition[] memberOfGroupsConditions = new ScimCondition[memberOfGroups.length];
                for (int i = 0; i < memberOfGroups.length; i++) {
                    memberOfGroupsConditions[i] = new ScimCondition(ScimUserMapper.SCIM_ATTRIBUTE_GROUPS, ScimCondition.OperatorType.EQ, StringUtils.trim(memberOfGroups[i]));
                }
                ICondition memberOfGroupsCondition = new ScimCompositeCondition(ScimCompositeCondition.CompositionType.OR, memberOfGroupsConditions);
                condition = ScimConditionUtil.add(condition, ScimCompositeCondition.CompositionType.AND, memberOfGroupsCondition);
            } else if (search.getMemberOfGroups().length == 1) {
                condition = ScimConditionUtil.add(condition, ScimCompositeCondition.CompositionType.AND, ScimUserMapper.SCIM_ATTRIBUTE_GROUPS, ScimCondition.OperatorType.EQ, StringUtils.trim(search.getMemberOfGroups()[0]));
            }
        }*/
        return condition.toQueryString();
    }

    private Pair<ScimCondition.OperatorType,List<String>> trimAndSplitValue(String value) {
        ScimCondition.OperatorType operator = (StringUtils.contains(value, WILDCARD)) ? ScimCondition.OperatorType.CO : ScimCondition.OperatorType.EQ;
        List<String> values = new ArrayList<String>();
        if (StringUtils.contains(value, WILDCARD)) {
            values.addAll(Arrays.stream(StringUtils.trim(value).split(REGEX_WILDCARD)).toList());
        } else {
            values.add(StringUtils.trim(value));
        }
        return new ImmutablePair<ScimCondition.OperatorType,List<String>>(operator, values);
    }

    public String toFilterString(List<String> uniqueNames) throws IllegalQueryException {
        ICondition condition = null;
        if (uniqueNames.size() >= 2) {
            ScimCondition[] comps = new ScimCondition[uniqueNames.size()];
            for (int i = 0; i < uniqueNames.size(); i++) {
                comps[i] = new ScimCondition(ScimUserMapper.SCIM_ATTRIBUTE_USER_NAME, ScimCondition.OperatorType.EQ, StringUtils.trim(uniqueNames.get(i)));
            }
            condition = new ScimCompositeCondition(ScimCompositeCondition.CompositionType.OR, comps);
        } else if (uniqueNames.size() == 1) {
            condition = new ScimCondition(ScimUserMapper.SCIM_ATTRIBUTE_USER_NAME, ScimCondition.OperatorType.EQ, StringUtils.trim(uniqueNames.get(0)));
        }
        return (null != condition) ? condition.toQueryString() : "";
    }
}
