package com.sap.bfx.definition;

import com.sap.bfx.utils.EnumUtils;
import com.sap.bfx.utils.Identifier;

/**
 * Selection mode for attachment controls (file uploader / upload collection).
 */
public enum AttachmentSelectType implements Identifier {
    None("none"),
    Single("single"),
    Multiple("multiple");

    private final String identifier;

    AttachmentSelectType(String identifier) {
        this.identifier = identifier;
    }

    public static AttachmentSelectType mapSelectType(String identifier) {
        return EnumUtils.valueById(AttachmentSelectType.class, identifier, AttachmentSelectType.None);
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }
}

