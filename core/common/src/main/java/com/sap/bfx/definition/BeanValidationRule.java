package com.sap.bfx.definition;

import java.util.Optional;

import org.springframework.context.ApplicationContext;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sap.bfx.callback.Context;

@Data
@EqualsAndHashCode(callSuper = true)
public class BeanValidationRule extends AbstractValidationRule {
    private String beanName;
    @JsonIgnore
    private ApplicationContext appContext;
    @JsonIgnore
    private ValidationRule rule;

    public BeanValidationRule() {
        super(ValidationRuleType.BEAN);
    }

    @Override
    public void postLoad() {
        rule = appContext.getBean(beanName, ValidationRule.class);
        if (rule == null) {
            throw new RuntimeException("Cannot find validation bean '" + beanName + "'");
        }
    }

    @Override
    public Optional<Message> validate(final String rowId, final String key, Context<?> context) {
        return rule.validate(rowId, key, context);
    }
}
