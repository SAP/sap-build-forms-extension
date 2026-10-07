package com.sap.bfx.callback;

/**
 * Converts between the logical feed type strings used in the API and the single-character
 * values stored in the {@code forms_feeds.type CHAR(1)} column.
 * <p>
 * Mapping: COMMENT↔C, INFO↔I, QUESTION↔Q, ANSWER↔A
 */
public final class FeedTypeConverter {

    private FeedTypeConverter() {}

    public static String toChar(String type) {
        return switch (type) {
            case "INFO"     -> "I";
            case "QUESTION" -> "Q";
            case "ANSWER"   -> "A";
            default         -> "C"; // COMMENT
        };
    }

    public static String fromChar(String c) {
        if (c == null) return "COMMENT";
        return switch (c.trim()) {
            case "I" -> "INFO";
            case "Q" -> "QUESTION";
            case "A" -> "ANSWER";
            default  -> "COMMENT";
        };
    }
}
