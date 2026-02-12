package com.sap.bfx.definition;

import com.sap.bfx.utils.EnumUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class InputElementDefinition extends ElementDefinition {

    private InputType inputType;

    public InputElementDefinition() {
        super(UIElementType.Input);
    }

    public static InputType mapType(String identifier) {
        return EnumUtils.valueById(InputType.class, identifier, InputType.Text);
    }
}
