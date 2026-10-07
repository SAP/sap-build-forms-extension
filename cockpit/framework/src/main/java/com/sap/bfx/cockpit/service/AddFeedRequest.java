package com.sap.bfx.cockpit.service;

import com.sap.bfx.callback.FeedApi;
import com.sap.bfx.exception.BadRequestException;

import java.util.Set;

/**
 * Request body for POST /api/v1/processes/{id}/feeds.
 *
 * @param text     Feed text (required)
 * @param type     One of: COMMENT, INFO, QUESTION, ANSWER (required)
 * @param parentId ID of the parent feed entry for threading; null for top-level entries
 */
public record AddFeedRequest(String text, String type, String parentId) {

    private static final Set<String> VALID_TYPES = Set.of(
            FeedApi.TYPE_COMMENT, FeedApi.TYPE_INFO, FeedApi.TYPE_QUESTION, FeedApi.TYPE_ANSWER);

    public AddFeedRequest {
        if (type != null && !VALID_TYPES.contains(type)) {
            throw new BadRequestException("invalid feed type '" + type + "'; must be one of: "
                    + String.join(", ", VALID_TYPES));
        }
    }
}
