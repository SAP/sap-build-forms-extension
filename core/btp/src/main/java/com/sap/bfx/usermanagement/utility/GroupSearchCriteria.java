package com.sap.bfx.usermanagement.utility;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.text.MessageFormat;

@Setter
@Getter
public class GroupSearchCriteria implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;
    private String displayName;
    private String description;
    private String zoneId;
    private String[] company;
    private boolean validateGroups;
    private long maxResults = -1;

    public GroupSearchCriteria() {
        super();
    }

    @Override
    public String toString() {
        return MessageFormat.format("GroupSearchCriteria: id={0}, displayName={1}, description={2}, zoneId={3}, company={4}, validateGroups={5}, maxResults={6}", id, displayName,
                description, zoneId, company, validateGroups, maxResults);
    }
}
