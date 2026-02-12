package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class SegmentElementDefinition extends ElementDefinition {

    public SegmentElementDefinition() {
        super(UIElementType.Segment);
    }
}
