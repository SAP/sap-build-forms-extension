package com.sap.bfx.definition;

import com.sap.bfx.utils.Identifier;

public enum AttachmentDesignType implements Identifier {
    FileUploader("fileUploader"),
    UploadCollection("uploadCollection");

    private final String identifier;

    AttachmentDesignType(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }
}
