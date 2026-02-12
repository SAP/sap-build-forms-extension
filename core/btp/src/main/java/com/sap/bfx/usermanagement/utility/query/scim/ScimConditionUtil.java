package com.sap.bfx.usermanagement.utility.query.scim;

import com.sap.bfx.usermanagement.utility.query.ICondition;
import org.apache.commons.lang3.StringUtils;

public final class ScimConditionUtil {

    private ScimConditionUtil() {
    }

    public static ICondition add(ICondition existing, ScimCompositeCondition.CompositionType type, String attribute, ScimCondition.OperatorType operator, String value) {
        if (StringUtils.isBlank(value)) {
            return existing;
        } else if (existing == null) {
            return new ScimCondition(attribute, operator, value);
        } else {
            return new ScimCompositeCondition(type, new ScimCondition(attribute, operator, value), existing);
        }
    }

    public static ICondition add(ICondition existing, ScimCompositeCondition.CompositionType type, ICondition newCondition) {
        if (newCondition == null) {
            return existing;
        } else if (existing == null) {
            return newCondition;
        } else {
            return new ScimCompositeCondition(type, newCondition, existing);
        }
    }
}
