package com.sap.bfx.definition;

import com.sap.bfx.callback.AccessClass;
import com.sap.bfx.callback.Context;

public abstract interface Evaluator<T> {

    abstract T eval(Context<? extends AccessClass> ctx, boolean isInitial, T defaultValue);
}
