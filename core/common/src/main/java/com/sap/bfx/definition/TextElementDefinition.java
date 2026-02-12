package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class TextElementDefinition extends ElementDefinition {

    public TextElementDefinition() {
        super(UIElementType.Text);
    }
}
