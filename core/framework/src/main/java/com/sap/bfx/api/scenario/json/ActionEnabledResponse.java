package com.sap.bfx.api.scenario.json;

/**
 * Response body for action-enabled check endpoints (cancelable, templatable).
 */
public record ActionEnabledResponse(boolean enabled) {
}
