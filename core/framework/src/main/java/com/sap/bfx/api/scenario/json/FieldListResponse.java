package com.sap.bfx.api.scenario.json;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
final public class FieldListResponse {
    String scenarioFieldNames;
    List<FieldResponse<Object>> fieldList;
}
