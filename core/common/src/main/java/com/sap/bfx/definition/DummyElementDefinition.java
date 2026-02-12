package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DummyElementDefinition extends ElementDefinition {

    public DummyElementDefinition() {
        super(UIElementType.Dummy);
    }
}
