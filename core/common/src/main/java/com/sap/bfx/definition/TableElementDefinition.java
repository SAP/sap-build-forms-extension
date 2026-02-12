package com.sap.bfx.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sap.bfx.utils.EnumUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.ArrayList;
import java.util.Collection;

@EqualsAndHashCode(callSuper = true)
@Data
public class TableElementDefinition extends ElementDefinition {

    public static int FULL_PAGE_SIZE = -1;

    private TableSelectType select = TableSelectType.Single;
    private TableStyleType style = TableStyleType.Dialog;
    private ToolbarElementDefinition toolbar;
    private int pageSize;

    public TableElementDefinition() {
        super(UIElementType.Table);
    }

    public static TableSelectType mapSelectType(String identifier) {
        return EnumUtils.valueById(TableSelectType.class, identifier, TableSelectType.None);
    }

    public static TableStyleType mapStyleType(String identifier) {
        return EnumUtils.valueById(TableStyleType.class, identifier, TableStyleType.Dialog);
    }

    @Override
    @JsonIgnore
    public Collection<Collection<ElementDefinition>> getChildren() {
        final var result = super.getChildren();

        if (toolbar != null) {
            final var c = new ArrayList<ElementDefinition>();
            c.add(toolbar);
            result.add(c);
        }

        return result;
    }
}
