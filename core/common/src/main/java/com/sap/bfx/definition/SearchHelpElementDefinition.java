package com.sap.bfx.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collection;

@Data
@EqualsAndHashCode(callSuper = true)
public class SearchHelpElementDefinition extends ElementDefinition {
    private String dialogKey;
    private ToolbarElementDefinition footer;
    private Dimension size;

    @Override
    @JsonIgnore
    public Collection<Collection<ElementDefinition>> getChildren() {
        final var result = super.getChildren();

        if (footer != null) {
            final var c = new ArrayList<ElementDefinition>();
            c.add(footer);
            result.add(c);
        }

        return result;
    }

    public SearchHelpElementDefinition() {
        super(UIElementType.SearchHelp);
    }
}
