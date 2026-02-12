package com.sap.bfx.definition;

import com.fasterxml.jackson.annotation.JsonValue;
import com.sap.bfx.utils.Identifier;

public enum Severity implements Identifier {
    Error("e"),
    Warning("w"),
    Info("i"),
    Success("s"),
    None("_");

    private final String identifier;

    Severity(String identifier) {
        this.identifier = identifier;
    }

    /**
     * @param minSeverity
     * @param msg
     * @return
     */
    public static boolean hasMinSeverity(final Severity minSeverity, final Message msg) {
        switch (minSeverity) {
            case Error:
                return Error.equals(msg.getSeverity());
            case Warning:
                return Error.equals(msg.getSeverity()) || Warning.equals(msg.getSeverity());
            case Info:
                return Error.equals(msg.getSeverity()) || Warning.equals(msg.getSeverity())
                        || Info.equals(msg.getSeverity());
            case Success:
                return Error.equals(msg.getSeverity()) || Warning.equals(msg.getSeverity())
                        || Info.equals(msg.getSeverity()) || Success.equals(msg.getSeverity());
            default:
                return true;
        }
    }

    @JsonValue
    @Override
    public String getIdentifier() {
        return identifier;
    }
}
