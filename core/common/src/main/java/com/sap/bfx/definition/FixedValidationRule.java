package com.sap.bfx.definition;

import java.math.BigDecimal;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sap.bfx.callback.Context;

@Data
@EqualsAndHashCode(callSuper = true)
public class FixedValidationRule extends AbstractValidationRule {
    private int length;
    private int fractions;

    @JsonIgnore
    private Class<?> dataTypeClass;

    public FixedValidationRule() {
        super(ValidationRuleType.FIXED);
    }

    /**
     * @param rowId
     * @param key
     * @param context
     * @return
     */
    @Override
    public Optional<Message> validate(String rowId, String key, Context<?> context) {
        var value = context.getDataApi().getValue(rowId, key);

        if (dataTypeClass == String.class) {
            if (StringUtils.length((String) value) != length) {
                return Optional.of(new Message(this.severity, this.messageKey, null));
            }
        } else if (dataTypeClass == BigDecimal.class) {
            if (length > 0 && value == null) {
                return Optional.of(new Message(this.severity, this.messageKey, null));
            }
            var v = (BigDecimal) value;

            if (v.precision() + v.scale() + 1 != length) {
                return Optional.of(new Message(this.severity, this.messageKey, null));
            }
            if (v.scale() != fractions) {
                return Optional.of(new Message(this.severity, this.messageKey, null));
            }
        }

        return Optional.empty();
    }
}
