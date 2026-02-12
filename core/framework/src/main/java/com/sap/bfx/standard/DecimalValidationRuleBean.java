package com.sap.bfx.standard;

import com.sap.bfx.callback.Context;
import com.sap.bfx.definition.Message;
import com.sap.bfx.definition.ValidationRule;

import java.util.Optional;

public class DecimalValidationRuleBean implements ValidationRule {
    /**
     * @param rowId
     * @param key
     * @param context
     * @return
     */
    @Override
    public Optional<Message> validate(String rowId, String key, Context<?> context) {
        //TODO ML Add according code
        return Optional.empty();
    }

    /**
     *
     */
    @Override
    public void postLoad() {

    }
}
