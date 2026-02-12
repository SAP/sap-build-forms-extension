package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class LinkElementDefinition extends ElementDefinition {

    public LinkElementDefinition() {
        super(UIElementType.Link);
    }

}