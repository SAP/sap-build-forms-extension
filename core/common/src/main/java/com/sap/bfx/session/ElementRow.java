package com.sap.bfx.session;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
public class ElementRow {

    public static final String ROOT = "_";
    public static final String SELECTED_SEPARATOR = ";";

    @JsonProperty("id")
    private String rowId;
    @JsonProperty("values")
    private ElementMap elements = new ElementMap();
    @JsonProperty("s")
    private boolean selected = false;
}
