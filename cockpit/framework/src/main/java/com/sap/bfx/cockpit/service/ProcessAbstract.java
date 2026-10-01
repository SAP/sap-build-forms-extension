package com.sap.bfx.cockpit.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.bfx.definition.FormAttributes;
import com.sap.bfx.definition.ProcessState;
import lombok.Data;

import java.time.Instant;

@Data
public class ProcessAbstract implements FormAttributes {
    @JsonIgnore
    private String changedBy;
    @JsonIgnore
    private Instant changedAt;
    private String description;
    private Instant finishedAt;
    private String additionalInformation;
    private String functionalId;
    private String id;
    private String refId;
    private String scenarioName;
    private int scenarioVersion;
    private String scenarioUrl;
    private String startedBy;
    private Instant startedAt;
    @JsonIgnore
    private ProcessState state;
    private String detailState;

    /** Serialises {@code state} as its numeric identifier (e.g. {@code "0"}, {@code "20"}) so the
     *  frontend {@code PROCESS_STATES} lookup by ID works correctly. */
    @JsonProperty("state")
    public String getStateIdentifier() {
        return state != null ? state.getIdentifier() : null;
    }
    @JsonIgnore
    private String templateName;
    @JsonIgnore
    private long version;
    @JsonIgnore
    private String workflowAdapter;
    private boolean cancelable;
    private String showState;

}
