package com.sap.bfx.callback;

import com.sap.bfx.definition.EventType;
import com.sap.bfx.utils.Identifier;
import org.apache.commons.lang3.StringUtils;

public abstract class BaseEventHandler<AC extends AccessClass> implements EventHandler<AC> {

    private final String key;
    private final EventType type;
    private final VersionSelector versionSelector;
    private final Priority order;
    private final boolean validating;

    /**
     * @param identifier
     * @param type
     * @param order
     * @param validating
     * @param versionSelector
     */
    protected BaseEventHandler(final Identifier identifier, final EventType type, final Priority order,
                               final boolean validating, final VersionSelector versionSelector) {

        this.key = identifier.getIdentifier();
        this.type = type;
        this.order = order;
        this.validating = validating;
        this.versionSelector = versionSelector;
    }

    /**
     * @param identifier
     * @param type
     */
    protected BaseEventHandler(final Identifier identifier, EventType type) {
        this(identifier, type, Priority.DEFAULT, false, VersionSelector.all());
    }

    /**
     * @param identifier
     * @param type
     * @param validating
     */
    protected BaseEventHandler(final Identifier identifier, EventType type, boolean validating) {
        this(identifier, type, Priority.DEFAULT, validating, VersionSelector.all());
    }

    @Override
    public boolean match(final String key, final EventType type, final int version) {
        var matches = StringUtils.equals(this.key, key) && this.type == type;
        if (version != VersionSelector.IGNORE) {
            matches = matches && this.versionSelector.match(version);
        }
        return matches;
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
    public EventType getType() {
        return this.type;
    }

    @Override
    public String getKey() {
        return this.key;
    }

    @Override
    public boolean validating() {
        return this.validating;
    }
}
