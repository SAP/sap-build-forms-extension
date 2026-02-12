package com.sap.bfx.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Definition of a form element.
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FormElementDefinition extends ElementDefinition {
    private ToolbarElementDefinition footer;
    private SegmentElementDefinition headerSegment;

    /**
     * Default constructor.
     */
    public FormElementDefinition() {
        super(UIElementType.Form);
    }

    /**
     * Get the children of the form element.
     */
    @Override
    @JsonIgnore
    public Collection<Collection<ElementDefinition>> getChildren() {
        final var result = super.getChildren();
        final var c = new ArrayList<ElementDefinition>();

        if (footer != null) {
            c.add(footer);
        }
        if (headerSegment != null) {
            c.add(headerSegment);
        }

        if (!c.isEmpty()) {
            result.add(c);
        }
        return result;
    }
}
