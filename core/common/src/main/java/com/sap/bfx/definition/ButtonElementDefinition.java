package com.sap.bfx.definition;

import com.sap.bfx.utils.EnumUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ButtonElementDefinition extends ElementDefinition {
    private ButtonDesignType design = ButtonDesignType.Default;
    private String icon;

    public ButtonElementDefinition() {
        super(UIElementType.Button);
    }

    public static ButtonDesignType mapDesignType(String identifier) {
        return EnumUtils.valueById(ButtonDesignType.class, identifier, ButtonDesignType.Default);
    }
}
