package com.sap.bfx.usermanagement.utility;

import org.apache.commons.lang3.builder.CompareToBuilder;

public class GroupComperator extends AComparator<Group> {

    public GroupComperator(SortOrder sortOrder) {
        super(sortOrder);
    }

    @Override
    protected int internalCompare(Group o1, Group o2) {
        CompareToBuilder ctb = new CompareToBuilder();
        ctb.append(o1.getId(), o2.getId());
        return ctb.toComparison();
    }

}
