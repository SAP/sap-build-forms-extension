package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class CurrencyElementDefinition extends ElementDefinition implements HasValueHelp {
    private ValueHelpOption valueHelp;

    public CurrencyElementDefinition() {
        super(UIElementType.Currency);
    }
}
