package com.sap.bfx.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class ToolbarElementDefinition extends ElementDefinition {
    private List<ElementDefinition> leftElements = new ArrayList<>();
    private List<ElementDefinition> rightElements = new ArrayList<>();

    public ToolbarElementDefinition() {
        super(UIElementType.Toolbar);
    }

    @Override
    @JsonIgnore
    public Collection<Collection<ElementDefinition>> getChildren() {
        final var result = super.getChildren();

        if (leftElements != null && !leftElements.isEmpty()) {
            result.add(leftElements);
        }
        if (rightElements != null && !rightElements.isEmpty()) {
            result.add(rightElements);
        }

        return result;
    }
}
