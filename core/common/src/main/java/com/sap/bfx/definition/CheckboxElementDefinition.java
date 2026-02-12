package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CheckboxElementDefinition extends ElementDefinition {

    public CheckboxElementDefinition() {
        super(UIElementType.Checkbox);
    }
}
