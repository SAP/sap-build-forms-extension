package com.sap.bfx.usermanagement.utility.query.scim;

import java.util.Arrays;
import java.util.Date;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;

import com.sap.bfx.usermanagement.utility.Group;
import com.sap.openapi.scim.model.ScimGroup;

/**
 * mapper for SCIM group
 */
public final class ScimGroupMapper {

    private static final ScimGroupMapper INSTANCE = new ScimGroupMapper();

    public static final String SCIM_ATTRIBUTE_ID = "id";                                // "id"
    public static final String SCIM_ATTRIBUTE_DISPLAYNAME = "displayName";              // "displayName";
    public static final String SCIM_ATTRIBUTE_DESCRIPTION = "description";              // "description";
    public static final String SCIM_ATTRIBUTE_ZONE_ID = "zoneId";                       // "zoneId"
    public static final String SCIM_ATTRIBUTE_MODIFYTIMESTAMP = "MODIFY_DATE";          // "whenChanged";
    public static final String SCIM_ATTRIBUTE_CREATETIMESTAMP = "CREATION_DATE";        // "whenCreated";

    private ScimGroupMapper() {
    }

    public static ScimGroupMapper getInstance() {
        return INSTANCE;
    }

    public String[] getAttributesForGroup() {
        String[] attributes = getAttributes();
        return Arrays.copyOf(attributes, attributes.length);
    }

    private String[] getAttributes() {
        return new String[] { SCIM_ATTRIBUTE_ID, SCIM_ATTRIBUTE_DISPLAYNAME, SCIM_ATTRIBUTE_DESCRIPTION, SCIM_ATTRIBUTE_ZONE_ID, SCIM_ATTRIBUTE_MODIFYTIMESTAMP,
                SCIM_ATTRIBUTE_CREATETIMESTAMP };
    }

    public Group mapToGroup(ScimGroup scimGroup) {
        Group group = new Group();
        group.setId(scimGroup.getId());
        group.setDisplayName(scimGroup.getDisplayName());
        group.setDescription(scimGroup.getDescription());
        group.setModifyTimestamp(null);
        group.setCreateTimestamp(null);
        return group;
    }

    private Date parseTimestamp(String[] timestamp) {
        if (null == timestamp || timestamp.length == 0) {
            return null;
        }
        try {
            return (timestamp.length > 0) ? new Date(Long.parseLong(timestamp[0])) : null;
        } catch (Exception e) {
            return null;
        }

    }

    private String getAttributeValue(String[] attributes) {
        if (null == attributes || attributes.length == 0) {
            return null;
        }
        return (attributes.length > 0) ? attributes[0] : null;
    }

    @SuppressWarnings("unused")
    private String getAttributeValue(Attributes attributes, String scimName) throws NamingException {
        String value = null;
        if (attributes.get(scimName) == null) {
            return null;
        }
        NamingEnumeration<?> values = ((BasicAttribute) attributes.get(scimName)).getAll();
        if (values.hasMore()) {
            Object next = values.next();
            value = next == null ? null : String.valueOf(next);
        }
        return value;
    }
}
