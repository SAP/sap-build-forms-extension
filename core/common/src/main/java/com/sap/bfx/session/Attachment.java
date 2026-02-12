package com.sap.bfx.session;

import lombok.Data;

@Data
public class Attachment {
    private String id;
    private int pos;
    private String fileName;
    private String contentType;
    private long size;
    private String ref;
    private String category;
    private String description;
}
