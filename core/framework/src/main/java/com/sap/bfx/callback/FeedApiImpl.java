package com.sap.bfx.callback;

/**
 * Implementation of {@link FeedApi} that delegates to {@link FeedService}.
 */
class FeedApiImpl implements FeedApi {

    private final FeedService feedService;
    private final String formId;

    FeedApiImpl(FeedService feedService, String formId) {
        this.feedService = feedService;
        this.formId = formId;
    }

    @Override
    public void addFeed(String text, String type, String author) {
        feedService.addFeed(formId, author != null ? author : "system", text, type, null);
    }
}
