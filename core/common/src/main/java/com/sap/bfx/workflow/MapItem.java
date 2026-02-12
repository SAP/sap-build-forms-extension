package com.sap.bfx.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MapItem {
    @JsonProperty("key")
    String key;
    @JsonProperty("value")
    String value;
}
