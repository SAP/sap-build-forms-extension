package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AutoCompleteElementDefinition extends ElementDefinition {

    public AutoCompleteElementDefinition() {
        super(UIElementType.AutoComplete);
    }
}
