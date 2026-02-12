package com.sap.bfx.definition;

import com.sap.bfx.utils.EnumUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class AlertElementDefinition extends ElementDefinition {
    private SeverityDesignType design = SeverityDesignType.Info;
    private String icon;

    public AlertElementDefinition() {
        super(UIElementType.Alert);
    }

    public static SeverityDesignType mapDesignType(String identifier) {
        return EnumUtils.valueById(SeverityDesignType.class, identifier, SeverityDesignType.Info);
    }
}
