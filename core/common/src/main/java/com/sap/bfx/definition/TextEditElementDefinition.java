package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TextEditElementDefinition extends ElementDefinition {

    public TextEditElementDefinition() {
        super(UIElementType.TextEdit);
    }
}
