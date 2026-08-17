package com.sap.bfx.callback;

import com.sap.bfx.definition.EventType;

import java.util.Set;

/**
 * Event handler interface.
 *
 * @param <AC> the access class type
 */
public interface EventHandler<AC extends AccessClass> {

    /**
     * Check if this handler matches the given key, type and version.
     *
     * @param key     the event key
     * @param type    the event type
     * @param version the event version
     * @return true if this handler matches the given key, type and version
     */
    boolean match(final String key, final EventType type, final int version);

    /**
     * Indicates if this handler is for validation events.
     *
     * @return true if this handler is for validation events
     */
    boolean validating();

    /**
     * Get the priority of this handler.
     *
     * @return the priority of this handler
     */
    Priority order();

    /**
     * Called before the main event processing.
     *
     * @param ctx    the context
     * @param result the result of the previous handler
     * @return the result of this handler
     */
    CallbackResult before(final Context<AC> ctx, final CallbackResult result);

    /**
     * Called during the main event processing.
     *
     * @param ctx    the context
     * @param result the result of the previous handler
     * @return the result of this handler
     */
    CallbackResult on(final Context<AC> ctx, final CallbackResult result);

    /**
     * Called after the main event processing.
     *
     * @param ctx    the context
     * @param result the result of the previous handler
     * @return the result of this handler
     */
    CallbackResult after(final Context<AC> ctx, final CallbackResult result);

    /**
     * Get the event type this handler is for.
     *
     * @return the event type
     */
    EventType getType();

    /**
     * Get the event keys this handler is for.
     *
     * @return the event keys
     */
    Set<String> getKeys();
}
