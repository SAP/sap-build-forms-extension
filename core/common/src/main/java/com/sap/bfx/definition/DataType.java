package com.sap.bfx.definition;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.bfx.utils.Identifier;

public enum DataType implements Identifier {
    @JsonProperty("auto")
    Auto("auto"),
    @JsonProperty("boolean")
    Boolean("boolen"),
    @JsonProperty("collection")
    Collection("collection"),
    @JsonProperty("date")
    Date("date"),
    @JsonProperty("datetime")
    DateTime("datetime"),
    @JsonProperty("daterage")
    DateRange("daterange"),
    @JsonProperty("decimal")
    Decimal("decimal"),
    @JsonProperty("int")
    Int("int"),
    @JsonProperty("string")
    String("string"),
    @JsonProperty("time")
    Time("time");

    private final String identifier;

    DataType(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public java.lang.String getIdentifier() {
        return this.identifier;
    }
}
