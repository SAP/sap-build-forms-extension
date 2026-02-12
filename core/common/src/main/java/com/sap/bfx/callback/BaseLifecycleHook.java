package com.sap.bfx.callback;

public abstract class BaseLifecycleHook<AC extends AccessClass> implements LifecycleHook<AC> {

    private final LifecycleHookType type;
    private final VersionSelector versionSelector;
    private final Priority order;

    protected BaseLifecycleHook(final LifecycleHookType type, final Priority order, final VersionSelector versionSelector) {
        this.type = type;
        this.versionSelector = versionSelector;
        this.order = order;
    }

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
    public CallbackResult before(final Context<AC> ctx, final CallbackResult previous) {
        return previous;
    }

    @Override
    public CallbackResult after(final Context<AC> ctx, final CallbackResult previous) {
        return previous;
    }

    @Override
    public LifecycleHookType getType() {
        return this.type;
    }
}
