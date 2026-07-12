package com.sap.bfx.session;

import com.sap.bfx.p13n.Settings;
import lombok.Data;

import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Data
public final class Session {
    private String id;
    private Locale locale;
    private String displayState;
    private Form form;
    private BackendJournal journal;
    private Collection<Settings> settings;
    private String userName;
    private String taskInstanceId;
    private Map<String, Object> additionalData = new HashMap<>();
}
