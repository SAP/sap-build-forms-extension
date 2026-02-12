package com.sap.bfx.definition;

import lombok.Data;

@Data
abstract class AbstractValidationRule implements ValidationRule {

    protected ValidationRuleType type;
    protected Severity severity;
    protected String messageKey;

    protected AbstractValidationRule(ValidationRuleType type) {
        this.type = type;
    }

    protected AbstractValidationRule() {
    }

    /**
     *
     */
    @Override
    public void postLoad() {
    }
}
