package com.sap.bfx.definition;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sap.bfx.callback.Context;

@Data
@EqualsAndHashCode(callSuper = true)
public class RegexValidationRule extends AbstractValidationRule {

    private String pattern;

    @JsonIgnore
    private Pattern compiledPattern;


    public RegexValidationRule() {
        super(ValidationRuleType.REGEX);
    }

    /**
     *
     */
    @Override
    public void postLoad() {
        compiledPattern = Pattern.compile(pattern);
    }

    @Override
    public Optional<Message> validate(final String rowId, final String key, Context<?> context) {
        var value = context.getDataApi().getValue(rowId, key);
        if (value == null) {
            return Optional.empty();
        }
        final var params = Map.of("value", (Object) pattern);
        if (!compiledPattern.matcher((String) value).matches()) {
            return Optional.of(new Message(this.severity, this.messageKey, params));
        }
        return Optional.empty();
    }
}
