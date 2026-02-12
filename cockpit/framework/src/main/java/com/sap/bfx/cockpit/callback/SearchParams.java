package com.sap.bfx.cockpit.callback;

import lombok.Data;

@Data
public class SearchParams {
    private String language;
    private String[] searchParameters;
    private String descriptionType;
    private String descriptionValue;
    private String functionalIdType;
    private String functionalIdValue;
    private String[] status;
    private String additionalInformationType;
    private String additionalInformationValue;
    private String user;
    private String[] roleUser;
    private String startedBy;
    private String endedOn;
    private String scenario;
}
