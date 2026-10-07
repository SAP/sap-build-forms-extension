package com.sap.bfx.cockpit.service;

import lombok.Data;

import java.time.Instant;

/**
 * Represents a single entry in the process feed (forms_feeds table).
 */
@Data
public class FeedEntry {
    private String id;
    private String parentId;
    private String formId;
    private String userNm;
    private String type;
    private int pos;
    private Instant ts;
    private String text;
}
