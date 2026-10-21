package com.sap.bfx.callback;

import com.sap.bfx.definition.EventType;
import com.sap.bfx.utils.Identifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base implementation of {@link EventHandler} that provides common functionality for matching
 * events based on per-identifier event-type bindings, as well as default implementations for
 * the {@code before} and {@code after} phases.
 *
 * <h2>Binding model</h2>
 * <p>Internally the handler stores a {@code Map<String, Set<EventType>>} — each element
 * identifier carries its own set of event types. A handler fires only when both the incoming
 * source key <em>and</em> the incoming event type are found together in that map, so there is
 * no implicit cross-product between identifiers and event types.
 *
 * <h2>Backward-compatible constructors</h2>
 * <p>All constructors that existed before this change are preserved verbatim and still work as
 * documented. For single-identifier or single-type handlers their behaviour is unchanged.
 *
 * <h2>Per-identifier multi-type binding (new)</h2>
 * <p>Use the {@link EventBindings} fluent builder to express different type sets per identifier:
 * <pre>{@code
 * public MyHandler() {
 *     super(EventBindings.of()
 *             .bind(MyFields.SaveButton, EventType.Action)
 *             .bind(MyFields.NameInput,  EventType.Change, EventType.EnterFocus));
 * }
 * }</pre>
 *
 * <p>Inside the phase methods, {@link Context#getSource()} and {@link Context#getEventType()}
 * together identify exactly which binding fired:
 * <pre>{@code
 * @Override
 * public CallbackResult on(Context<MyForm> ctx, CallbackResult previous) {
 *     switch (ctx.getEventType()) {
 *         case Action     -> handleSave(ctx, previous);
 *         case Change     -> handleNameChange(ctx, previous);
 *         case EnterFocus -> handleNameFocus(ctx, previous);
 *     }
 *     return previous;
 * }
 * }</pre>
 *
 * @param <AC> the type of access class used in the event context
 */
public abstract class BaseEventHandler<AC extends AccessClass> implements EventHandler<AC> {

    /**
     * Core binding map: element-identifier string → the event types it is bound to.
     * Values are always unmodifiable {@link EnumSet} instances.
     */
    private final Map<String, Set<EventType>> bindings;

    private final VersionSelector versionSelector;
    private final Priority order;
    private final boolean validating;

    // =========================================================================
    // Original constructors — preserved verbatim for backward compatibility
    // =========================================================================

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
        this.bindings = singleBinding(identifier, type);
        this.order = order;
        this.validating = validating;
        this.versionSelector = versionSelector;
    }

    /**
     * Creates a handler bound to a single element identifier with default priority, no
     * validation, and matching all form definition versions.
     *
     * @param identifier identifies the form element this handler is registered for
     * @param type       the event type that triggers this handler
     */
    protected BaseEventHandler(final Identifier identifier, final EventType type) {
        this(identifier, type, Priority.DEFAULT, false, VersionSelector.all());
    }

    /**
     * Creates a handler bound to a single element identifier with default priority, matching
     * all form definition versions.
     *
     * @param identifier identifies the form element this handler is registered for
     * @param type       the event type that triggers this handler
     * @param validating {@code true} if this handler participates in validation processing
     */
    protected BaseEventHandler(final Identifier identifier, final EventType type,
                               final boolean validating) {
        this(identifier, type, Priority.DEFAULT, validating, VersionSelector.all());
    }

    /**
     * Creates a handler bound to multiple element identifiers with full control over all
     * options. All identifiers share the same single event type.
     *
     * @param type            the event type that triggers this handler
     * @param order           the execution priority relative to other handlers for the same event
     * @param validating      {@code true} if this handler participates in validation processing
     * @param versionSelector restricts which form definition versions this handler applies to
     * @param identifiers     one or more form element identifiers this handler is registered for
     */
    protected BaseEventHandler(final EventType type, final Priority order, final boolean validating,
                               final VersionSelector versionSelector,
                               final Identifier... identifiers) {
        this.bindings = uniformBinding(type, identifiers);
        this.order = order;
        this.validating = validating;
        this.versionSelector = versionSelector;
    }

    /**
     * Creates a handler bound to multiple element identifiers with default priority, no
     * validation, and matching all form definition versions. All identifiers share the same
     * single event type.
     *
     * @param type        the event type that triggers this handler
     * @param identifiers one or more form element identifiers this handler is registered for
     */
    protected BaseEventHandler(final EventType type, final Identifier... identifiers) {
        this(type, Priority.DEFAULT, false, VersionSelector.all(), identifiers);
    }

    // =========================================================================
    // Multi-type constructors (single identifier) — added in sf-multi-event-handler
    // =========================================================================

    /**
     * Creates a handler bound to a single element identifier that responds to multiple event
     * types, with full control over all options.
     *
     * @param identifier      identifies the form element this handler is registered for
     * @param order           the execution priority relative to other handlers for the same event
     * @param validating      {@code true} if this handler participates in validation processing
     * @param versionSelector restricts which form definition versions this handler applies to
     * @param types           two or more event types that trigger this handler
     */
    protected BaseEventHandler(final Identifier identifier, final Priority order,
                               final boolean validating, final VersionSelector versionSelector,
                               final EventType... types) {
        this.bindings = singleBinding(identifier, types);
        this.order = order;
        this.validating = validating;
        this.versionSelector = versionSelector;
    }

    /**
     * Creates a handler bound to a single element identifier that responds to two or more event
     * types, with default priority, no validation, and matching all form definition versions.
     *
     * <pre>{@code super(MyFields.MyButton, EventType.Action, EventType.Change); }</pre>
     *
     * @param identifier identifies the form element this handler is registered for
     * @param type1      first event type
     * @param type2      second event type
     * @param additional any additional event types
     */
    protected BaseEventHandler(final Identifier identifier, final EventType type1,
                               final EventType type2, final EventType... additional) {
        this(identifier, Priority.DEFAULT, false, VersionSelector.all(),
                prepend(type1, type2, additional));
    }

    /**
     * Creates a handler bound to a single element identifier that responds to two or more event
     * types, with default priority and matching all form definition versions.
     *
     * @param identifier identifies the form element this handler is registered for
     * @param validating {@code true} if this handler participates in validation processing
     * @param type1      first event type
     * @param type2      second event type
     * @param additional any additional event types
     */
    protected BaseEventHandler(final Identifier identifier, final boolean validating,
                               final EventType type1, final EventType type2,
                               final EventType... additional) {
        this(identifier, Priority.DEFAULT, validating, VersionSelector.all(),
                prepend(type1, type2, additional));
    }

    // =========================================================================
    // Per-identifier multi-type constructors (EventBindings) — new
    // =========================================================================

    /**
     * Creates a handler with per-identifier event-type bindings using the {@link EventBindings}
     * fluent builder, with default priority, no validation, and matching all form definition
     * versions.
     *
     * <pre>{@code
     * super(EventBindings.of()
     *         .bind(MyFields.SaveButton, EventType.Action)
     *         .bind(MyFields.NameInput,  EventType.Change, EventType.EnterFocus));
     * }</pre>
     *
     * @param bindings the event bindings; must not be empty
     * @throws IllegalArgumentException if {@code bindings} is empty
     */
    protected BaseEventHandler(final EventBindings bindings) {
        this(bindings, Priority.DEFAULT, false, VersionSelector.all());
    }

    /**
     * Creates a handler with per-identifier event-type bindings, with default priority and
     * matching all form definition versions.
     *
     * @param bindings   the event bindings; must not be empty
     * @param validating {@code true} if this handler participates in validation processing
     * @throws IllegalArgumentException if {@code bindings} is empty
     */
    protected BaseEventHandler(final EventBindings bindings, final boolean validating) {
        this(bindings, Priority.DEFAULT, validating, VersionSelector.all());
    }

    /**
     * Creates a handler with per-identifier event-type bindings with full control over all
     * options.
     *
     * @param bindings        the event bindings; must not be empty
     * @param order           the execution priority relative to other handlers for the same event
     * @param validating      {@code true} if this handler participates in validation processing
     * @param versionSelector restricts which form definition versions this handler applies to
     * @throws IllegalArgumentException if {@code bindings} is empty
     */
    protected BaseEventHandler(final EventBindings bindings, final Priority order,
                               final boolean validating, final VersionSelector versionSelector) {
        if (bindings == null || bindings.isEmpty()) {
            throw new IllegalArgumentException("EventBindings must not be null or empty");
        }
        this.bindings = bindings.asMap();
        this.order = order;
        this.validating = validating;
        this.versionSelector = versionSelector;
    }

    // =========================================================================
    // Matching
    // =========================================================================

    /**
     * Returns {@code true} when this handler should be invoked for the given element key, event
     * type, and form definition version.
     *
     * <p>The key must be registered in this handler's bindings <em>and</em> the type must be
     * in the type set for that specific key. This ensures that per-identifier type restrictions
     * (expressed via {@link EventBindings}) are respected and no cross-product matching occurs.
     */
    @Override
    public boolean match(final String key, final EventType type, final int version) {
        final var typesForKey = bindings.get(key);
        var matches = typesForKey != null && typesForKey.contains(type);
        if (version != VersionSelector.IGNORE) {
            matches = matches && versionSelector.match(version);
        }
        return matches;
    }

    @Override
    public Priority order() {
        return order;
    }

    // =========================================================================
    // Default phase implementations (no-op pass-through)
    // =========================================================================

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

    // =========================================================================
    // Accessors
    // =========================================================================

    /**
     * Returns the union of all event types across all bindings.
     *
     * <p>For handlers using {@link EventBindings} with different type sets per identifier, this
     * is the union of those sets. Use {@link #getTypes(String)} to query the types for a
     * specific key.
     *
     * @return unmodifiable union of all bound event types
     */
    @Override
    public Set<EventType> getTypes() {
        if (bindings.size() == 1) {
            return bindings.values().iterator().next();
        }
        final var union = EnumSet.noneOf(EventType.class);
        bindings.values().forEach(union::addAll);
        return Collections.unmodifiableSet(union);
    }

    /**
     * Returns the event types bound to a specific element-identifier key.
     *
     * @param key the element-identifier string
     * @return unmodifiable set of event types for that key, or an empty set if not registered
     */
    @Override
    public Set<EventType> getTypes(final String key) {
        return bindings.getOrDefault(key, Collections.emptySet());
    }

    /**
     * Returns the set of element identifiers this handler is registered for.
     *
     * @return unmodifiable view of the registered keys
     */
    @Override
    public Set<String> getKeys() {
        return bindings.keySet();
    }

    /**
     * Returns whether the event-handler triggers validation.
     *
     * @return {@code true} if this handler participates in validation processing
     */
    @Override
    public boolean validating() {
        return validating;
    }

    // =========================================================================
    // Internal helpers
    // =========================================================================

    /** Single identifier → single or multiple types. */
    private static Map<String, Set<EventType>> singleBinding(final Identifier identifier,
                                                              final EventType... types) {
        if (types == null || types.length == 0) {
            throw new IllegalArgumentException("At least one EventType must be specified");
        }
        final var set = EnumSet.of(types[0], types);
        return Collections.singletonMap(identifier.getIdentifier(),
                Collections.unmodifiableSet(set));
    }

    /** Single identifier → exactly one type (kept for original single-type constructors). */
    private static Map<String, Set<EventType>> singleBinding(final Identifier identifier,
                                                              final EventType type) {
        return Collections.singletonMap(identifier.getIdentifier(),
                Collections.unmodifiableSet(EnumSet.of(type)));
    }

    /** Multiple identifiers → same single type for all (original multi-identifier constructors). */
    private static Map<String, Set<EventType>> uniformBinding(final EventType type,
                                                               final Identifier... identifiers) {
        if (identifiers == null || identifiers.length == 0) {
            throw new IllegalArgumentException("At least one Identifier must be specified");
        }
        final var typeSet = Collections.unmodifiableSet(EnumSet.of(type));
        final LinkedHashMap<String, Set<EventType>> map = new LinkedHashMap<>(identifiers.length);
        Arrays.stream(identifiers).forEach(id -> map.put(id.getIdentifier(), typeSet));
        return Collections.unmodifiableMap(map);
    }

    private static EventType[] prepend(final EventType first, final EventType second,
                                       final EventType... rest) {
        final EventType[] all = Arrays.copyOf(rest, rest.length + 2);
        System.arraycopy(rest, 0, all, 2, rest.length);
        all[0] = first;
        all[1] = second;
        return all;
    }
}
