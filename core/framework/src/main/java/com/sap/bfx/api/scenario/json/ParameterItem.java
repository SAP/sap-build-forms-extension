package com.sap.bfx.api.scenario.json;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParameterItem<T> {
    String key;
    T value;
}
