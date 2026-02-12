package com.sap.bfx.session;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sap.bfx.p13n.Settings;
import lombok.Data;

import java.util.Collection;
import java.util.Locale;

@Data
public final class Session {
    private String id;
    private Locale locale;
    private String displayState;
    private Form form;
    @JsonIgnore
    private BackendJournal journal;
    private Collection<Settings> settings;
    private String userName;
    @JsonIgnore
    private String taskInstanceId;
}
