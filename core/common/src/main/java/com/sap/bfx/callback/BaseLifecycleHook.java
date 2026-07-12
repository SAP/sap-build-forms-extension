package com.sap.bfx.callback;

/**
 * Base class for lifecycle hooks.
 * Implements the LifecycleHook interface and provides default implementations for the before and after methods.
 * The class is generic and works with a specified access class type.
 *
 * @param <AC> the access class type
 */
public abstract class BaseLifecycleHook<AC extends AccessClass> implements LifecycleHook<AC> {

    private final LifecycleHookType type;
    private final VersionSelector versionSelector;
    private final Priority order;

    /**
     * Creates a lifecycle hook with the specified type, order, and version selector.
     *
     * @param type            the lifecycle hook type
     * @param order           the execution priority relative to other hooks
     * @param versionSelector restricts which form definition versions this hook applies to
     */
    protected BaseLifecycleHook(final LifecycleHookType type, final Priority order,
                                final VersionSelector versionSelector) {
        this.type = type;
        this.versionSelector = versionSelector;
        this.order = order;
    }

    /**
     * Creates a lifecycle hook with the specified type and default order and version selector.
     *
     * @param type the lifecycle hook type
     */
    protected BaseLifecycleHook(LifecycleHookType type) {
        this(type, Priority.DEFAULT, VersionSelector.all());
    }
    
    @Override
    public boolean match(int version) {
        return versionSelector.match(version);
    }

    @Override
    public Priority order() {
        return order;
    }

    @Override
    public CallbackResult before(final Context<AC> ctx, final CallbackResult result) {
        return result;
    }

    @Override
    public CallbackResult after(final Context<AC> ctx, final CallbackResult result) {
        return result;
    }

    @Override
    public LifecycleHookType getType() {
        return this.type;
    }
}
