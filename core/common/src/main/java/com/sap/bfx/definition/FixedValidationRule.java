package com.sap.bfx.definition;

import java.math.BigDecimal;
import java.util.Map;
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
        final var params = Map.of("length", (Object) length, "fractions", (Object) fractions);

        if (value == null) {
            return Optional.empty();
        }

        if (dataTypeClass == String.class) {
            if (StringUtils.length((String) value) != length) {
                return Optional.of(new Message(this.severity, this.messageKey, params));
            }
        } else if (dataTypeClass == BigDecimal.class) {
            var v = (BigDecimal) value;

            if (v.precision() != length) {
                return Optional.of(new Message(this.severity, this.messageKey, params));
            }
            if (v.scale() != fractions) {
                return Optional.of(new Message(this.severity, this.messageKey, params));
            }
        }

        return Optional.empty();
    }
}
