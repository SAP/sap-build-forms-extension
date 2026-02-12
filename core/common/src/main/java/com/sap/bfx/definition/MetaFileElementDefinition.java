package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class MetaFileElementDefinition extends ElementDefinition {
    private String path;
    private String mixinName;
    private int version;

    public MetaFileElementDefinition() {
        super(UIElementType.Mixin);
    }
}
