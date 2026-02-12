package com.sap.bfx.definition;

import java.util.Optional;

import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sap.bfx.callback.Context;

@Data
@EqualsAndHashCode(callSuper = true)
public class SpelValidationRule extends AbstractValidationRule {

    private String expression;

    @JsonIgnore
    private Expression compiledExpression;
    @JsonIgnore
    private Context<?> context;

    public SpelValidationRule() {
        super(ValidationRuleType.SPEL);
    }

    /**
     *
     */
    @Override
    public void postLoad() {
        var config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, SpelEvaluator.class.getClassLoader());
        var expressionParser = new SpelExpressionParser(config);
        compiledExpression = expressionParser.parseExpression(expression);
    }

    @Override
    public Optional<Message> validate(final String rowId, final String key, Context<?> context) {
        var r = compiledExpression.getValue(new StandardEvaluationContext(new ImmutableTriple(rowId, key, context)),
                Object.class);
        if (r != null) {
            if (r == Boolean.FALSE) {
                return Optional.of(new Message(this.severity, this.messageKey, null));
            }
            if (r instanceof Message) {
                return Optional.of((Message) r);
            }
        }
        return Optional.empty();
    }
}
