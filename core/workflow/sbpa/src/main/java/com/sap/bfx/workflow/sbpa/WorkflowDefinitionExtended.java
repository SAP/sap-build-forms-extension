package com.sap.bfx.workflow.sbpa;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.openapi.sbpaworkflow.model.WorkflowDefinition;
import lombok.Getter;
import lombok.Setter;

public class WorkflowDefinitionExtended extends WorkflowDefinition {

    @Setter
    @Getter
    @JsonProperty("projectId")
    private String projectId;

    @Setter
    @Getter
    @JsonProperty("projectVersion")
    private String projectVersion;

    @Setter
    @Getter
    @JsonProperty("environmentId")
    private String environmentId;

}
