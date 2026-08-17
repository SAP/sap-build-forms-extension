package com.sap.bfx.callback;

/**
 * Lifecycle hook interface.
 * Defines methods for matching versions, ordering, and handling lifecycle events.
 * The interface is generic and works with a specified access class type.
 * The lifecycle events include before, on, and after, each accepting a context and a previous callback result.
 *
 * @param <AC> the access class type
 */
public interface LifecycleHook<AC extends AccessClass> {

    /**
     * Matches the given version.
     *
     * @param version the version to match
     * @return true if the version matches, false otherwise
     */
    boolean match(int version);

    /**
     * Defines the order of execution for the lifecycle hook.
     *
     * @return the priority order
     */
    Priority order();

    /**
     * Handles the 'before' lifecycle event.
     *
     * @param ctx    the context of the lifecycle event
     * @param result the previous callback result
     * @return the callback result after handling the event
     */
    CallbackResult before(final Context<AC> ctx, final CallbackResult result);

    /**
     * Handles the 'on' lifecycle event.
     *
     * @param ctx    the context of the lifecycle event
     * @param result the previous callback result
     * @return the callback result after handling the event
     */
    CallbackResult on(final Context<AC> ctx, final CallbackResult result);

    /**
     * Handles the 'after' lifecycle event.
     *
     * @param ctx    the context of the lifecycle event
     * @param result the previous callback result
     * @param result the previous callback result
     * @return the callback result after handling the event
     */
    CallbackResult after(final Context<AC> ctx, final CallbackResult result);

    /**
     * Gets the type of the lifecycle hook.
     *
     * @return the lifecycle hook type
     */
    LifecycleHookType getType();
}
