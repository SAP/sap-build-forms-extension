package com.sap.bfx.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ParameterItem {
    @JsonProperty("key")
    String key;
    @JsonProperty("value")
    String value;
    @JsonProperty("values")
    List<String> values;
    @JsonProperty("itemsMap")
    List<MapItem> itemsMap;
}
