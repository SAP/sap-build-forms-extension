package com.sap.bfx.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class TaskOutputContext {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("selectedAction")
    String selectedAction;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("parameters")
    List<ParameterItem> parameters;
}
