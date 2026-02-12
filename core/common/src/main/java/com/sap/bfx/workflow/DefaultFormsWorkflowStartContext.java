package com.sap.bfx.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class DefaultFormsWorkflowStartContext {
    @JsonProperty("formsprocessid")
    String formsProcessId;
}
