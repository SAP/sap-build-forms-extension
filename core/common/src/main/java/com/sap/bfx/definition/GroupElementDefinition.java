package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class GroupElementDefinition extends ElementDefinition {

    public GroupElementDefinition() {
        super(UIElementType.Group);
    }
}
