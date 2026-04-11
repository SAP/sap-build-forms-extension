package com.sap.bfx.valuehelp;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Data
public class GetValueHelpResponse {
    private String id;
    private String locale;
    private long version;
    private String keyKey;
    private List<String> valueKeys = new ArrayList<>();
    private String formatTemplate;
    private List<Map<String, String>> values = new ArrayList<>();
}
