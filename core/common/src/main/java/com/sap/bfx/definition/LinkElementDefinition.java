package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@EqualsAndHashCode(callSuper = true)
@Data
@Getter
public class LinkElementDefinition extends ElementDefinition {
    private LinkData linkData;

    public LinkElementDefinition() {
        super(UIElementType.Link);
    }

}