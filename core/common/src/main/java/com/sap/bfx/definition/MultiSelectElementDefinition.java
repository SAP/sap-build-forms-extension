package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MultiSelectElementDefinition extends ElementDefinition  implements HasValueHelp {
    private ValueHelpOption valueHelp;

    public MultiSelectElementDefinition() {
        super(UIElementType.MultiSelect);
    }
}
