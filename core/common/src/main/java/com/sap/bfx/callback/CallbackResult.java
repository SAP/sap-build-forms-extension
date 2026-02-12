package com.sap.bfx.callback;

import com.sap.bfx.definition.Severity;
import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Setter;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

@Data
public class CallbackResult {

    private boolean validate = false;
    private boolean stopProcessing = false;
    private String headerTitle;
    private String pageTitle;
    @Setter(AccessLevel.NONE)
    private Collection<Message> messages = new LinkedList<>();

    /**
     * @param style
     * @param severity
     * @param key
     * @param params
     */
    public void addMessage(MessageStyle style, Severity severity, String key, Map<String, Object> params) {
        messages.add(new Message(style, severity, key, params));
    }

    /**
     * @param style
     * @param msg
     */
    public void addMessage(MessageStyle style, com.sap.bfx.definition.Message msg) {
        messages.add(new Message(style, msg.getSeverity(), msg.getKey(), msg.getParams()));
    }

    /**
     * @param style
     * @param severity
     * @param text
     */
    public void addTextMessage(final MessageStyle style, final Severity severity, final String text) {
        final var params = new HashMap<String, Object>();
        params.put("text", text);

        messages.add(new Message(style, severity, "default.msg.text", params));
    }

    /**
     *
     */
    public void clearMessages() {
        messages = null;
    }

    /**
     *
     */
    public enum MessageStyle {
        Toast, Dialog;
    }

    /**
     *
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
