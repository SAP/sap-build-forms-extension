package com.sap.bfx.usermanagement.utility;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Setter
@Getter
public class Group implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String id;                          // unique id
    private String displayName;                 // display name
    private String description;                 // description
    private String zoneId;                      // zone id
    private OffsetDateTime modifyTimestamp;     // when Changed
    private OffsetDateTime createTimestamp;     // when Created

    public Group() {
    }

    public Group(Group other) {
        this.id = other.id;
        this.displayName = other.displayName;
        this.description = other.description;
        this.zoneId = other.zoneId;
        this.modifyTimestamp = other.modifyTimestamp;
        this.createTimestamp = other.createTimestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != this.getClass()) {
            return false;
        }
        EqualsBuilder eb = new EqualsBuilder();
        Group other = (Group) obj;
        eb.append(this.getId(), other.getId());
        return eb.isEquals();
    }

    @Override
    public int hashCode() {
        HashCodeBuilder hb = new HashCodeBuilder();
        hb.append(this.getId());
        return hb.toHashCode();
    }

    @Override
    public String toString() {
        return this.getDisplayName() + " (" + this.getId() + ")";
    }
}
