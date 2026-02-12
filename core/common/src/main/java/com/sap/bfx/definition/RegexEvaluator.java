package com.sap.bfx.definition;

import com.sap.bfx.callback.AccessClass;
import com.sap.bfx.callback.Context;
import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;

public class RegexEvaluator implements Evaluator<Boolean> {

    public static final char START_CHAR = '/';
    public static final String START = String.valueOf(START_CHAR);

    public static final char END_CHAR = '/';
    public static final String END = String.valueOf(END_CHAR);

    private Pattern pattern;

    public RegexEvaluator(final String expressionSource) {
        var src = StringUtils.trim(expressionSource);
        src = StringUtils.substringAfter(src, START_CHAR);
        src = StringUtils.substringBefore(src, END_CHAR);

        this.pattern = Pattern.compile(src);
    }

    @Override
    public Boolean eval(Context<? extends AccessClass> ctx, boolean isInitial, Boolean defaultValue) {
        return pattern.matcher(ctx.getDisplayState()).matches();
    }
}

