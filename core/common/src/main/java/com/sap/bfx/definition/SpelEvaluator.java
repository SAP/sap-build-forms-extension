package com.sap.bfx.definition;

import com.sap.bfx.callback.AccessClass;
import com.sap.bfx.callback.Context;
import org.apache.commons.lang3.StringUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

/**
 * Evals expressions with Spring expression language (SpEL). See
 * https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/expressions.html
 * for a description and documentation
 *
 * @param <T>
 */
public class SpelEvaluator<T> implements Evaluator<T> {

    public static final char START_CHAR = '{';
    public static final String START = String.valueOf(START_CHAR);

    public static final char END_CHAR = '}';
    public static final String END = String.valueOf(END_CHAR);

    private Class<T> cls;
    private Expression expression;

    public SpelEvaluator(final String expressionSource, Class<T> cls) {
        this.cls = cls;

        var src = StringUtils.trim(expressionSource);
        src = StringUtils.substringAfter(src, START_CHAR);
        src = StringUtils.substringBefore(src, END_CHAR);

        var config = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, SpelEvaluator.class.getClassLoader());
        var expressionParser = new SpelExpressionParser(config);
        this.expression = expressionParser.parseExpression(src);
    }

    @Override
    public T eval(Context<? extends AccessClass> ctx, boolean isInitial, T defaultValue) {
        return expression.getValue(new StandardEvaluationContext(ctx), cls);
    }

    public T eval(Object obj) {
        return expression.getValue(new StandardEvaluationContext(obj), cls);
    }
}
