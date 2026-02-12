package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class DateRangeElementDefinition extends ElementDefinition {

    public DateRangeElementDefinition() {
        super(UIElementType.DateRangePicker);
    }
}
