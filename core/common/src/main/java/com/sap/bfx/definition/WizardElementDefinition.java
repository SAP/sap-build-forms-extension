package com.sap.bfx.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collection;

@EqualsAndHashCode(callSuper = true)
@Data
public class WizardElementDefinition extends ElementDefinition {
    private ToolbarElementDefinition footer;

    public WizardElementDefinition() {
        super(UIElementType.Wizard);
    }

    @Override
    @JsonIgnore
    public Collection<Collection<ElementDefinition>> getChildren() {
        final var result = super.getChildren();
        final var c = new ArrayList<ElementDefinition>();

        if (footer != null) {
            c.add(footer);
        }
//        if (headerSegment != null) {
//            c.add(headerSegment);
//        }

        if (!c.isEmpty()) {
            result.add(c);
        }
        return result;
    }
}
