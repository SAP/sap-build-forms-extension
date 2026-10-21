package com.sap.bfx.callback;

import com.sap.bfx.definition.EventType;
import com.sap.bfx.utils.Identifier;

import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Fluent builder for defining per-identifier event-type bindings used by {@link BaseEventHandler}.
 *
 * <p>Each identifier can be bound to one or more {@link EventType}s independently of every other
 * identifier. A handler constructed with an {@code EventBindings} instance fires only for the
 * exact (identifier, event type) pairs that were explicitly registered — there is no cross-product
 * between identifiers and event types.
 *
 * <p>Example — one handler that reacts to an {@code Action} on a Save button and to both
 * {@code Change} and {@code EnterFocus} on a name input field:
 *
 * <pre>{@code
 * public MyHandler() {
 *     super(EventBindings.of()
 *             .bind(MyFields.SaveButton,    EventType.Action)
 *             .bind(MyFields.NameInput,     EventType.Change, EventType.EnterFocus));
 * }
 * }</pre>
 *
 * <p>Calling {@link #bind} with an identifier that was already added merges the new event types
 * into the existing set for that identifier rather than replacing them.
 */
public final class EventBindings {

    /** Insertion-ordered so the log output is predictable. */
    private final LinkedHashMap<String, EnumSet<EventType>> map = new LinkedHashMap<>();

    private EventBindings() {}

    /**
     * Creates a new, empty {@code EventBindings} builder.
     *
     * @return a fresh builder
     */
    public static EventBindings of() {
        return new EventBindings();
    }

    /**
     * Binds the given identifier to one or more event types.
     *
     * <p>If this identifier has already been bound, the supplied types are <em>merged</em> into
     * the existing set (union), not replaced.
     *
     * @param identifier the form-element identifier to bind
     * @param first      the first event type (required)
     * @param rest       any additional event types
     * @return {@code this}, for chaining
     * @throws IllegalArgumentException if {@code identifier} or {@code first} is {@code null}
     */
    public EventBindings bind(final Identifier identifier, final EventType first,
                              final EventType... rest) {
        if (identifier == null) {
            throw new IllegalArgumentException("identifier must not be null");
        }
        if (first == null) {
            throw new IllegalArgumentException("at least one EventType must be specified");
        }
        final var key = identifier.getIdentifier();
        final var existing = map.computeIfAbsent(key, k -> EnumSet.noneOf(EventType.class));
        existing.add(first);
        if (rest != null) {
            for (final EventType t : rest) {
                if (t != null) {
                    existing.add(t);
                }
            }
        }
        return this;
    }

    /**
     * Returns whether this builder has at least one binding.
     *
     * @return {@code true} if at least one {@link #bind} call was made
     */
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * Returns an unmodifiable view of the internal bindings map.
     * Keys are element-identifier strings; values are unmodifiable {@link EnumSet} copies.
     *
     * <p>Intended for use by {@link BaseEventHandler} only.
     *
     * @return unmodifiable bindings map
     */
    Map<String, Set<EventType>> asMap() {
        final LinkedHashMap<String, Set<EventType>> result = new LinkedHashMap<>(map.size());
        for (final var entry : map.entrySet()) {
            result.put(entry.getKey(), Collections.unmodifiableSet(entry.getValue()));
        }
        return Collections.unmodifiableMap(result);
    }
}
