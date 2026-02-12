package com.sap.bfx.definition;

import com.sap.bfx.utils.Identifier;
import lombok.Data;

import java.time.Instant;

/**
 * Represents a log entry with various attributes such as severity, action, timestamp, user, and message details.
 */
@Data
public class LogEntry {
    private String id;
    private String formId;
    private Severity severity;
    private Action action;
    private Instant timestamp;
    private String user;
    private String messageId;
    private String messageText;
    private Object messageData;

    /**
     * Action enum representing different types of actions associated with a log entry.
     */
    public enum Action implements Identifier {
        Info("i"),
        Save("s"),
        Workflow("w");

        private final String identifier;

        Action(String identifier) {
            this.identifier = identifier;
        }

        @Override
        public String getIdentifier() {
            return identifier;
        }
    }
}
