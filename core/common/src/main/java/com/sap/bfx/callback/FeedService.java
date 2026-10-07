package com.sap.bfx.callback;

/**
 * Service contract for persisting feed entries on a process instance.
 * Implemented by the cockpit module and injected into {@link ApiFactory}
 * so that {@link FeedApi} can be resolved like any other hardcoded API.
 */
@FunctionalInterface
public interface FeedService {
    void addFeed(String formId, String userNm, String text, String type, String parentId);
}
