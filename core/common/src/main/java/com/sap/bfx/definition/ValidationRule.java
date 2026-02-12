package com.sap.bfx.definition;

import com.sap.bfx.callback.Context;
import com.sap.bfx.utils.EnumUtils;

import java.util.Optional;

public interface ValidationRule {

    static Severity mapSeverity(String identifier) {
        return EnumUtils.valueById(Severity.class, identifier, Severity.None);
    }

    Optional<Message> validate(final String rowId, final String key, final Context<?> context);

    void postLoad();
}
