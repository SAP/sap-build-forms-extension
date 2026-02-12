package com.sap.bfx.api.scenario.json;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
final public class FieldResponse<T> {
    @JsonIgnore
    String scenarioFieldName;
    T fieldValue;
}
