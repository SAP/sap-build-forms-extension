package com.sap.bfx.callback;

/**
 * API for adding feed entries to a process instance from within scenario handlers and lifecycle hooks.
 * <p>
 * Obtain via {@code ctx.getApi(FeedApi.class)}.
 * <p>
 * Example:
 * <pre>
 *     FeedApi feedApi = ctx.getApi(FeedApi.class);
 *     feedApi.addFeed("Process was approved.", FeedApi.TYPE_INFO, null);
 * </pre>
 */
public interface FeedApi extends Api {

    String TYPE_COMMENT  = "COMMENT";
    String TYPE_INFO     = "INFO";
    String TYPE_QUESTION = "QUESTION";
    String TYPE_ANSWER   = "ANSWER";

    /**
     * Adds a new top-level feed entry to the current process.
     *
     * @param text   the feed text
     * @param type   one of {@link #TYPE_COMMENT}, {@link #TYPE_INFO}, {@link #TYPE_QUESTION}, {@link #TYPE_ANSWER}
     * @param author the username to record as author; pass {@code null} to use "system"
     */
    void addFeed(String text, String type, String author);
}
