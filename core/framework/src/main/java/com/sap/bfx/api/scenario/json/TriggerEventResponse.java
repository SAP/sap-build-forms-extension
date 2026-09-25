package com.sap.bfx.api.scenario.json;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
final public class TriggerEventResponse {
    String statusCode;
    String statusMessage;
    Set<ParameterItem<Object>> parameters;
    List<FieldResponse<Object>> fields;
}
