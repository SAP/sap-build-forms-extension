package com.sap.bfx.valuehelp;

import com.sap.bfx.valuehelp.model.ValueHelpType;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class ValueHelpData {
    private String id;
    private String locale;
    private long version;
    private String keyKey;
    private List<String> valueKeys = new ArrayList<>();
    private String formatTemplate;
    private List<Map<String, String>> values = new ArrayList<>();
    private ValueHelpType type;
}


