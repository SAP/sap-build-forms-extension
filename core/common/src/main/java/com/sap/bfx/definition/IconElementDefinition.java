package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class IconElementDefinition extends ElementDefinition {
    private String icon;

    public IconElementDefinition() {
        super(UIElementType.Icon);
    }
}
