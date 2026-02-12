package com.sap.bfx.session;

import com.fasterxml.jackson.annotation.JsonValue;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

public enum ChangePropertyType {
    Value("va"),
    Visible("vi"),
    Editable("ed"),
    Required("rq"),
    Message("msg"),
    Selected("s"),
    Position("p"),
    PageSize("ps"),
    SortField("sf"),
    SortOrder("so");

    private final String key;

    ChangePropertyType(String key) {
        this.key = key;
    }

    public static Optional<ChangePropertyType> valueByKey(final String key) {
        return Arrays.stream(ChangePropertyType.values()).filter(
                it -> StringUtils.equals(it.key, key)).findFirst();
    }

    @JsonValue
    public String getKey() {
        return this.key;
    }

}
