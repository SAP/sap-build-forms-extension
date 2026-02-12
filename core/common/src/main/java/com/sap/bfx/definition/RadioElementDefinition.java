package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class RadioElementDefinition extends ElementDefinition implements HasValueHelp {
    private ValueHelpOption valueHelp;

    public RadioElementDefinition() {
        super(UIElementType.Radio);
    }
}
