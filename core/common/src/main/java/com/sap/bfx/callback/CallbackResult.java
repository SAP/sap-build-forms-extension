package com.sap.bfx.callback;

import com.sap.bfx.callback.operation.FrontendOperation;
import com.sap.bfx.definition.Severity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * CallbackResult is a class that represents the result of a callback operation. It contains information about the
 * validation status, whether to stop processing, header and page titles, messages, and value helps.
 */
@Data
public class CallbackResult {

    private boolean validate = false;
    private boolean stopProcessing = false;
    private String headerTitle;
    private String pageTitle;
    @Setter(AccessLevel.NONE)
    private Collection<Message> messages = new LinkedList<>();
    @Setter(AccessLevel.NONE)
    private Map<String, Map<String, String>> valueHelps = new HashMap<>();
    private FrontendOperation operation;

    /**
     * Adds a message to the messages collection. The message will be displayed in the UI with the specified style and
     * severity.
     *
     * @param style    the style of the message (e.g., Toast, Dialog)
     * @param severity the severity of the message (e.g., Info, Warning, Error)
     * @param key      the key for the message text (used for localization)
     * @param params   a map of parameters to be used in the message text
     */
    public void addMessage(MessageStyle style, Severity severity, String key, Map<String, Object> params) {
        messages.add(new Message(style, severity, key, params));
    }

    /**
     * Adds a message to the messages collection. The message will be displayed in the UI with the specified style.
     *
     * @param style the style of the message (e.g., Toast, Dialog)
     * @param msg   the message object containing severity, key, and parameters
     */
    public void addMessage(MessageStyle style, com.sap.bfx.definition.Message msg) {
        messages.add(new Message(style, msg.getSeverity(), msg.getKey(), msg.getParams()));
    }

    /**
     * Adds a text message to the messages collection. The message will be displayed in the UI with the specified style
     * and severity.
     *
     * @param style    the style of the message (e.g., Toast, Dialog)
     * @param severity the severity of the message (e.g., Info, Warning, Error)
     * @param text     the text content of the message
     */
    public void addTextMessage(final MessageStyle style, final Severity severity, final String text) {
        final var params = new HashMap<String, Object>();
        params.put("text", text);

        messages.add(new Message(style, severity, "default.msg.text", params));
    }

    /**
     * Clears the messages collection, after calling this the message structre is empty and no vaalues will be
     * returned to the UI (only if added after this call).
     */
    public void clearMessages() {
        messages.clear();
    }

    /**
     * Adds a value help to the valueHelpVersions map. The value help is identified by a key and contains a map of values.
     *
     * @param key    the key for the value help
     * @param values a map of values associated with the value help
     */
    public void addValuehelp(String key, Map<String, String> values) {
        valueHelps.put(key, values);
    }

    /**
     * Finds a value help in the valueHelpVersions map based on the provided key.
     *
     * @param key the key for the value help to find
     * @return a map of values associated with the value help, or null if not found
     */
    public Map<String, String> findValuehelp(String key) {
        return valueHelps.get(key);
    }

    /**
     * Checks if a value help exists in the valueHelpVersions map based on the provided key.
     *
     * @param key the key for the value help to check
     * @return true if the value help exists, false otherwise
     */
    public boolean hasValuehelp(String key) {
        return valueHelps.containsKey(key);
    }

    /**
     * Deletes a value help from the valueHelpVersions map based on the provided key.
     *
     * @param key the key for the value help to delete
     */
    public void deleteValuehelp(String key) {
        valueHelps.remove(key);
    }

    /**
     * Clears all value helps from the valueHelpVersions map, after calling this the value help structure is empty and no
     * values will be returned to the UI (only if added after this call).
     */
    public void cleanValuehelps() {
        valueHelps.clear();
    }

    /**
     * Enum representing the style of a message. It can be either a Toast message or a Dialog message.
     */
    public enum MessageStyle {
        Toast,
        Dialog
    }

    /**
     * Message class represents a message with a specific style, severity, key, and parameters. It extends the
     * com.sap.bfx.definition.Message class.
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class Message extends com.sap.bfx.definition.Message {
        private MessageStyle style;

        /**
         * @param style
         * @param severity
         * @param key
         * @param params
         */
        public Message(MessageStyle style, Severity severity, String key, Map<String, Object> params) {
            super(severity, key, params);
            this.style = style;
        }
    }
}
