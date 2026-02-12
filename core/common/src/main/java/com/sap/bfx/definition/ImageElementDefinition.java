package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ImageElementDefinition extends ElementDefinition {

    private Dimension size;

    public ImageElementDefinition() {
        super(UIElementType.Image);
    }
}
