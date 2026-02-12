package com.sap.bfx.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Data
public class TaskInstance {
    @JsonProperty("activityId")
    String activityId;
    @JsonProperty("claimedAt")
    OffsetDateTime claimedAt;
    @JsonProperty("completedAt")
    OffsetDateTime completedAt;
    @JsonProperty("createdAt")
    OffsetDateTime createdAt;
    @JsonProperty("description")
    String description;
    @JsonProperty("id")
    String id;
    @JsonProperty("processor")
    String processor;
    @JsonProperty("recipientUsers")
    Set<String> recipientUsers;
    @JsonProperty("recipientGroups")
    Set<String> recipientGroups;
    @JsonProperty("status")
    String status;
    @JsonProperty("subject")
    String subject;
    @JsonProperty("workflowDefinitionId")
    String workflowDefinitionId;
    @JsonProperty("workflowInstanceId")
    String workflowInstanceId;
    @JsonProperty("priority")
    String priority;
    @JsonProperty("dueDate")
    OffsetDateTime dueDate;
    @JsonProperty("createdBy")
    String createdBy;
    @JsonProperty("definitionId")
    String definitionId;
    @JsonProperty("lastChangedAt")
    OffsetDateTime lastChangedAt;
    @JsonProperty("applicationScope")
    String applicationScope;
    @JsonProperty("userInterfaceUri")
    String userInterfaceUri;
    @JsonProperty("attributes")
    List<Attributes> attributes;

    @Data
    @AllArgsConstructor
    public static class Attributes {
        @JsonProperty("id")
        private String id;
        @JsonProperty("label")
        private String label;
        @JsonProperty("value")
        private String value;
    }
}
