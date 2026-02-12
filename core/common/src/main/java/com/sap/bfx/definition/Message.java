package com.sap.bfx.definition;

import com.sap.bfx.utils.EnumUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    public final static Message REQUIRED_ERROR = new Message(Severity.Error, "default.msg.validation.required", null);
    public final static Message MIN_ERROR = new Message(Severity.Error, "default.msg.validation.min", null);

    private Severity severity;
    private String key;
    private Map<String, Object> params;

    public static Severity mapSeverity(String identifier) {
        return EnumUtils.valueById(Severity.class, identifier, Severity.None);
    }
}
