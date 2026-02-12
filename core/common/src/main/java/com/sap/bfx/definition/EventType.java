package com.sap.bfx.definition;

import com.sap.bfx.utils.Identifier;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public enum EventType implements Identifier {
    Action("action"),
    Browse("browse"),
    Change("change"),
    Delete("delete"),
    EnterFocus("enterFocus"),
    LeaveFocus("leaveFocus"),
    Open("open"),
    Sort("sort");

    private final String identifier;

    EventType(String identifier) {
        this.identifier = identifier;
    }

    public static Optional<EventType> valueByKey(String key) {
        final var k = StringUtils.trim(StringUtils.lowerCase(key));
        return Arrays.stream(EventType.values()).filter(it -> StringUtils.equals(it.getIdentifier(), k)).findFirst();
    }

    @Override
    public java.lang.String getIdentifier() {
        return this.identifier;
    }
}
