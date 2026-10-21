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
     * Get the set of event types this handler is registered for.
     *
     * <p>When the handler was constructed with an {@link EventBindings} or with multiple
     * identifiers carrying different type sets, this method returns the <em>union</em> of all
     * event types across every binding. To query the types bound to a specific identifier use
     * {@link #getTypes(String)}.
     *
     * <p>The current event type that triggered a specific invocation is available via
     * {@link Context#getEventType()}.
     *
     * @return the union of all event types across all bindings
     */
    Set<EventType> getTypes();

    /**
     * Get the event types bound to a specific element-identifier key.
     *
     * <p>For handlers that bind different event types per identifier (via {@link EventBindings}),
     * this returns only the types registered for {@code key}. For handlers with a flat type set
     * (single identifier, or multiple identifiers all sharing the same types) the default
     * implementation returns the same set as {@link #getTypes()}.
     *
     * @param key the element-identifier string (as returned by {@link com.sap.bfx.utils.Identifier#getIdentifier()})
     * @return the event types bound to that key, or an empty set if the key is not registered
     */
    default Set<EventType> getTypes(final String key) {
        return getTypes();
    }

    /**
     * Get the (primary) event type this handler is for.
     *
     * @return the first event type in the set returned by {@link #getTypes()}
     * @deprecated Use {@link #getTypes()} instead. This method exists for backward
     *             compatibility and returns the first element of the types set.
     */
    @Deprecated
    default EventType getType() {
        return getTypes().iterator().next();
    }

    /**
     * Get the event keys this handler is for.
     *
     * @return the event keys
     */
    Set<String> getKeys();
}
