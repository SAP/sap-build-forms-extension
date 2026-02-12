package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SelectElementDefinition extends ElementDefinition implements HasValueHelp {
    private ValueHelpOption valueHelp;

    public SelectElementDefinition() {
        super(UIElementType.Select);
    }
}
