package com.sap.bfx.usermanagement.utility.query.ldap;

import com.sap.bfx.usermanagement.utility.query.ICondition;
import org.apache.commons.lang3.StringUtils;

public final class LdapConditionUtil {

    private LdapConditionUtil() {
    }

    public static ICondition add(ICondition existing, LdapCompositeCondition.CompositionType type, String attribute, String value) {
        if (StringUtils.isBlank(value)) {
            return existing;
        } else if (existing == null) {
            return new LdapCondition(attribute, value);
        } else {
            return new LdapCompositeCondition(type, new LdapCondition(attribute, value), existing);
        }
    }

    public static ICondition add(ICondition existing, LdapCompositeCondition.CompositionType type, ICondition newCondition) {
        if (newCondition == null) {
            return existing;
        } else if (existing == null) {
            return newCondition;
        } else {
            return new LdapCompositeCondition(type, newCondition, existing);
        }
    }
}
