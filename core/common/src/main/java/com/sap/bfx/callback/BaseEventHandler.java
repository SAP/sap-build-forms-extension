package com.sap.bfx.callback;

import com.sap.bfx.definition.EventType;
import com.sap.bfx.utils.Identifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Base implementation of {@link EventHandler} that provides common functionality for matching events
 * based on element identifiers and event type, as well as default implementations for the
 * {@code before} and {@code after} phases.
 *
 * <p>Subclasses can extend this base class to implement specific event handling logic while
 * leveraging the built-in matching and execution flow. The constructors allow for flexible
 * configuration of which events the handler should respond to, including support for multiple
 * identifiers and version-based filtering.</p>
 *
 * @param <AC> the type of access class used in the event context
 */
public abstract class BaseEventHandler<AC extends AccessClass> implements EventHandler<AC> {

    private final Set<String> keys = new HashSet<>();
    private final EventType type;
    private final VersionSelector versionSelector;
    private final Priority order;
    private final boolean validating;

    /**
     * Creates a handler bound to a single element identifier with full control over all options.
     *
     * @param identifier      identifies the form element this handler is registered for
     * @param type            the event type that triggers this handler
     * @param order           the execution priority relative to other handlers for the same event
     * @param validating      {@code true} if this handler participates in validation processing
     * @param versionSelector restricts which form definition versions this handler applies to
     */
    protected BaseEventHandler(final Identifier identifier, final EventType type, final Priority order,
                               final boolean validating, final VersionSelector versionSelector) {

        this.keys.add(identifier.getIdentifier());
        this.type = type;
        this.order = order;
        this.validating = validating;
        this.versionSelector = versionSelector;
    }

    /**
     * Creates a handler bound to a single element identifier with default priority, no validation,
     * and matching all form definition versions.
     *
     * @param identifier identifies the form element this handler is registered for
     * @param type       the event type that triggers this handler
     */
    protected BaseEventHandler(final Identifier identifier, EventType type) {
        this(identifier, type, Priority.DEFAULT, false, VersionSelector.all());
    }

    /**
     * Creates a handler bound to a single element identifier with default priority, matching all
     * form definition versions.
     *
     * @param identifier identifies the form element this handler is registered for
     * @param type       the event type that triggers this handler
     * @param validating {@code true} if this handler participates in validation processing
     */
    protected BaseEventHandler(final Identifier identifier, EventType type, boolean validating) {
        this(identifier, type, Priority.DEFAULT, validating, VersionSelector.all());
    }

    /**
     * Creates a handler bound to multiple element identifiers with full control over all options.
     *
     * @param type            the event type that triggers this handler
     * @param order           the execution priority relative to other handlers for the same event
     * @param validating      {@code true} if this handler participates in validation processing
     * @param versionSelector restricts which form definition versions this handler applies to
     * @param identifiers     one or more form element identifiers this handler is registered for
     */
    protected BaseEventHandler(EventType type, Priority order, boolean validating, VersionSelector versionSelector,
                               final Identifier... identifiers) {

        this.keys.addAll(Arrays.stream(identifiers).map(Identifier::getIdentifier).toList());
        this.type = type;
        this.order = order;
        this.validating = validating;
        this.versionSelector = versionSelector;
    }

    /**
     * Creates a handler bound to multiple element identifiers with default priority, no validation,
     * and matching all form definition versions.
     *
     * @param type        the event type that triggers this handler
     * @param identifiers one or more form element identifiers this handler is registered for
     */
    protected BaseEventHandler(EventType type, Identifier... identifiers) {
        this(type, Priority.DEFAULT, false, VersionSelector.all(), identifiers);
    }

    @Override
    public boolean match(final String key, final EventType type, final int version) {
        var matches = this.keys.contains(key) && this.type == type;
        if (version != VersionSelector.IGNORE) {
            matches = matches && this.versionSelector.match(version);
        }
        return matches;
    }

    @Override
    public Priority order() {
        return order;
    }

    /**
     * Called before the main {@code on} processing phase for the matched event.
     * <p>
     * The default implementation is a no-op that passes {@code previous} through unchanged.
     * Subclasses can override this method to execute logic that must run before the primary
     * event handler, for example pre-populating fields or enforcing preconditions.
     * </p>
     *
     * @param ctx    the event context providing access to form data, APIs, and user information
     * @param result the {@link CallbackResult} produced by the preceding handler in the chain,
     *               or the initial result if this is the first handler
     * @return the (possibly modified) {@link CallbackResult} to pass to the next handler
     */
    @Override
    public CallbackResult before(final Context<AC> ctx, final CallbackResult result) {
        return result;
    }

    /**
     * Called after the main {@code on} processing phase for the matched event.
     * <p>
     * The default implementation is a no-op that passes {@code previous} through unchanged.
     * Subclasses can override this method to execute follow-up logic once the primary handler
     * has finished, for example auditing, cleanup, or appending additional messages to the result.
     * </p>
     *
     * @param ctx    the event context providing access to form data, APIs, and user information
     * @param result the {@link CallbackResult} produced by the preceding handler in the chain,
     *               typically the result returned by the {@code on} phase
     * @return the (possibly modified) {@link CallbackResult} to pass to the next handler
     */
    @Override
    public CallbackResult after(final Context<AC> ctx, final CallbackResult result) {
        return result;
    }

    /**
     * Gets the {@link VersionSelector} that determines which form definition versions this handler applies to.
     *
     * @return the version selector for this handler
     */
    @Override
    public EventType getType() {
        return this.type;
    }

    /**
     * Gets the set of element identifiers this handler is registered for.
     *
     * @return a set of element identifier strings
     */
    @Override
    public Set<String> getKeys() {
        return this.keys;
    }

    /**
     * Returns if the event-hanlder validates (executes the validation rules defined in definition + required) or not
     *
     * @return boolean value if handler validates or not
     */
    @Override
    public boolean validating() {
        return this.validating;
    }
}
