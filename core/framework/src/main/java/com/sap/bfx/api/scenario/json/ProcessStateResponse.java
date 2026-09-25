package com.sap.bfx.api.scenario.json;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
final public class ProcessStateResponse {
    String statusCode;
    String statusMessage;
}
