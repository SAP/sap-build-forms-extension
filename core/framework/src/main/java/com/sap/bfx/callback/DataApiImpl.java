package com.sap.bfx.callback;

import com.sap.bfx.definition.Message;
import com.sap.bfx.definition.Severity;
import com.sap.bfx.exception.FormsCoreException;
import com.sap.bfx.session.*;
import com.sap.bfx.utils.Identifier;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Predicate;

/**
 * Implementation of the DataApi interface, providing access and mutation methods for form data,
 * visual attributes, messages, and table operations.
 */
@Slf4j
public class DataApiImpl implements DataApi {
    private final Form form;
    private final Map<Identifier, Map<Identifier, Identifier>> mixinMap;

    /**
     * Constructs a new DataApiImpl.
     *
     * @param form     the form instance to operate on
     * @param mixinMap the mixin mapping for nested/mixin keys
     */
    public DataApiImpl(final Form form, final Map<Identifier, Map<Identifier, Identifier>> mixinMap) {
        this.form = form;
        this.mixinMap = mixinMap;
    }

    //-----------------------------------------------------------------------------------------------------------------
    // Value Getters

    /**
     * Gets the value for a field as an Optional.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @return Optional containing the value, or empty if not present
     */
    @Override
    public Optional<Object> getOptVal(final String rowId, final Identifier id) {
        return getOptVal(rowId, id.getIdentifier());
    }

    /**
     * Gets the value for a field as an Optional.
     *
     * @param rowId the row identifier
     * @param key   the field key
     * @return Optional containing the value, or empty if not present
     */
    @Override
    public Optional<Object> getOptVal(final String rowId, final String key) {
        final var opt = form.getValue(rowId, key);
        if (opt.isPresent() && opt.get() instanceof Table) {
            throw new FormsCoreException("cannot access table element directly!");
        }
        return opt;
    }

    /**
     * Gets the value for a field as an Optional of the specified type.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @param clz   the class of the expected type
     * @param <T>   the type parameter
     * @return Optional containing the value, or empty if not present
     */
    @Override
    public <T> Optional<T> getOptVal(final String rowId, final Identifier id, final Class<T> clz) {
        final var opt = form.getValue(rowId, id.getIdentifier(), clz);
        if (opt.isPresent() && opt.get() instanceof Table) {
            throw new FormsCoreException("cannot access table element directly!");
        }
        return opt;
    }

    /**
     * Gets the value for a mixin field as an Optional of the specified type.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param clz      the class of the expected type
     * @param <T>      the type parameter
     * @return Optional containing the value, or empty if not present
     */
    @Override
    public <T> Optional<T> getOptVal(String rowId, Identifier prefixId, Identifier id, final Class<T> clz) {
        return getOptVal(rowId, mixinMap.get(prefixId).get(id), clz);
    }

    /**
     * Gets the value for a mixin field as an Optional.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @return Optional containing the value, or empty if not present
     */
    @Override
    public Optional<Object> getOptVal(String rowId, Identifier prefixId, Identifier id) {
        return getOptVal(rowId, mixinMap.get(prefixId).get(id));
    }

    /**
     * Gets the value for a field.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @return the value, or null if not present
     */
    @Override
    public Object getValue(final String rowId, final Identifier id) {
        var opt = getOptVal(rowId, id.getIdentifier());
        return opt.orElse(null);
    }

    /**
     * Gets the value for a field.
     *
     * @param rowId the row identifier
     * @param key   the field key
     * @return the value, or null if not present
     */
    @Override
    public Object getValue(final String rowId, final String key) {
        final var opt = getOptVal(rowId, key);
        return opt.orElse(null);
    }

    /**
     * Gets the value for a field as the specified type.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @param clz   the class of the expected type
     * @param <T>   the type parameter
     * @return the value, or null if not present
     */
    @Override
    public <T> T getValue(final String rowId, final Identifier id, final Class<T> clz) {
        final var opt = getOptVal(rowId, id, clz);
        return opt.orElse(null);
    }

    /**
     * Gets the value for a mixin field as the specified type.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param clz      the class of the expected type
     * @param <T>      the type parameter
     * @return the value, or null if not present
     */
    @Override
    public <T> T getValue(final String rowId, final Identifier prefixId, final Identifier id, final Class<T> clz) {
        return getValue(rowId, mixinMap.get(prefixId).get(id), clz);
    }

    /**
     * Gets the value for a mixin field.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @return the value, or null if not present
     */
    @Override
    public Object getValue(final String rowId, final Identifier prefixId, final Identifier id) {
        return getValue(rowId, mixinMap.get(prefixId).get(id));
    }

    /**
     * Gets the value for a field as an Optional.
     *
     * @param id the field identifier
     * @return Optional containing the value, or empty if not present
     */
    @Override
    public Optional<Object> getOptVal(Identifier id) {
        return getOptVal(ElementRow.ROOT, id);
    }

    /**
     * Gets the value for a field as an Optional of the specified type.
     *
     * @param id  the field identifier
     * @param clz the class of the expected type
     * @param <T> the type parameter
     * @return Optional containing the value, or empty if not present
     */
    @Override
    public <T> Optional<T> getOptVal(Identifier id, final Class<T> clz) {
        return getOptVal(ElementRow.ROOT, id, clz);
    }

    /**
     * Gets the value for a mixin field as an Optional of the specified type.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param clz      the class of the expected type
     * @param <T>      the type parameter
     * @return Optional containing the value, or empty if not present
     */
    @Override
    public <T> Optional<T> getOptVal(Identifier prefixId, Identifier id, final Class<T> clz) {
        return getOptVal(ElementRow.ROOT, prefixId, id, clz);
    }

    /**
     * Gets the value for a mixin field as an Optional.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @return Optional containing the value, or empty if not present
     */
    @Override
    public Optional<Object> getOptVal(Identifier prefixId, Identifier id) {
        return getOptVal(ElementRow.ROOT, prefixId, id);
    }

    /**
     * Gets the value for a field.
     *
     * @param id the field identifier
     * @return the value, or null if not present
     */
    @Override
    public Object getValue(Identifier id) {
        return getValue(ElementRow.ROOT, id);
    }

    /**
     * Gets the value for a mixin field.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @return the value, or null if not present
     */
    @Override
    public Object getValue(Identifier prefixId, Identifier id) {
        return getValue(ElementRow.ROOT, prefixId, id);
    }

    /**
     * Gets the value for a field as the specified type.
     *
     * @param id  the field identifier
     * @param clz the class of the expected type
     * @param <T> the type parameter
     * @return the value, or null if not present
     */
    @Override
    public <T> T getValue(Identifier id, final Class<T> clz) {
        return getValue(ElementRow.ROOT, id, clz);
    }

    /**
     * Gets the value for a mixin field as the specified type.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param clz      the class of the expected type
     * @param <T>      the type parameter
     * @return the value, or null if not present
     */
    @Override
    public <T> T getValue(Identifier prefixId, Identifier id, final Class<T> clz) {
        return getValue(ElementRow.ROOT, prefixId, id, clz);
    }

    //-----------------------------------------------------------------------------------------------------------------
    // Value Setters

    /**
     * Sets the value for a field.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @param value the value to set
     */
    @Override
    public void setValue(String rowId, Identifier id, Object value) {
        form.setValue(rowId, id.getIdentifier(), value);
    }

    /**
     * Sets the value for a mixin field.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param value    the value to set
     */
    @Override
    public void setValue(String rowId, Identifier prefixId, Identifier id, Object value) {
        form.setValue(rowId, mixinMap.get(prefixId).get(id).getIdentifier(), value);
    }

    /**
     * Sets the value for a field.
     *
     * @param id    the field identifier
     * @param value the value to set
     */
    @Override
    public void setValue(Identifier id, Object value) {
        form.setValue(ElementRow.ROOT, id.getIdentifier(), value);
    }

    /**
     * Sets the value for a mixin field.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param value    the value to set
     */
    @Override
    public void setValue(Identifier prefixId, Identifier id, Object value) {
        form.setValue(ElementRow.ROOT, mixinMap.get(prefixId).get(id).getIdentifier(), value);
    }

    //-----------------------------------------------------------------------------------------------------------------
    // Visual attributes

    /**
     * Checks if a field is required.
     *
     * @param rowId the row identifier
     * @param key   the field key
     * @return true if required, false otherwise
     */
    @Override
    public boolean isRequired(final String rowId, final String key) {
        var opt = form.getRequired(rowId, key);
        return opt != null && opt.isPresent() && opt.get();
    }

    /**
     * Checks if a field is required.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @return true if required, false otherwise
     */
    @Override
    public boolean isRequired(final String rowId, final Identifier id) {
        return isRequired(rowId, id.getIdentifier());
    }

    /**
     * Checks if a field is required.
     *
     * @param id the field identifier
     * @return true if required, false otherwise
     */
    @Override
    public boolean isRequired(Identifier id) {
        return isRequired(ElementRow.ROOT, id.getIdentifier());
    }

    /**
     * Checks if a mixin field is required.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @return true if required, false otherwise
     */
    @Override
    public boolean isRequired(Identifier prefixId, Identifier id) {
        return isRequired(ElementRow.ROOT, mixinMap.get(prefixId).get(id));
    }

    /**
     * Checks if a mixin field is required.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @return true if required, false otherwise
     */
    @Override
    public boolean isRequired(String rowId, Identifier prefixId, Identifier id) {
        return isRequired(rowId, mixinMap.get(prefixId).get(id));
    }

    /**
     * Sets the required attribute for a field.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @param value true if required, false otherwise
     */
    @Override
    public void setRequired(String rowId, Identifier id, boolean value) {
        form.setRequired(rowId, id.getIdentifier(), value);
    }

    /**
     * Sets the required attribute for a mixin field.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param value    true if required, false otherwise
     */
    @Override
    public void setRequired(String rowId, Identifier prefixId, Identifier id, boolean value) {
        setRequired(rowId, mixinMap.get(prefixId).get(id), value);
    }

    /**
     * Sets the required attribute for a field.
     *
     * @param id    the field identifier
     * @param value true if required, false otherwise
     */
    @Override
    public void setRequired(Identifier id, boolean value) {
        setRequired(ElementRow.ROOT, id, value);
    }

    /**
     * Sets the required attribute for a mixin field.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param value    true if required, false otherwise
     */
    @Override
    public void setRequired(Identifier prefixId, Identifier id, boolean value) {
        setRequired(ElementRow.ROOT, mixinMap.get(prefixId).get(id), value);
    }

    /**
     * Checks if a field is visible.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @return true if visible, false otherwise
     */
    @Override
    public boolean isVisible(String rowId, Identifier id) {
        var opt = form.getVisible(rowId, id.getIdentifier());
        return opt != null && opt.isPresent() && opt.get();
    }

    /**
     * Checks if a mixin field is visible.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @return true if visible, false otherwise
     */
    @Override
    public boolean isVisible(String rowId, Identifier prefixId, Identifier id) {
        return isVisible(rowId, mixinMap.get(prefixId).get(id));
    }

    /**
     * Checks if a field is visible.
     *
     * @param id the field identifier
     * @return true if visible, false otherwise
     */
    @Override
    public boolean isVisible(Identifier id) {
        return isVisible(ElementRow.ROOT, id);
    }

    /**
     * Checks if a mixin field is visible.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @return true if visible, false otherwise
     */
    @Override
    public boolean isVisible(Identifier prefixId, Identifier id) {
        return isVisible(ElementRow.ROOT, mixinMap.get(prefixId).get(id));
    }

    /**
     * Sets the visible attribute for a field.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @param value true if visible, false otherwise
     */
    @Override
    public void setVisible(String rowId, Identifier id, boolean value) {
        setVisible(rowId, id.getIdentifier(), value);
    }

    /**
     * Sets the visible attribute for a field.
     *
     * @param rowId the row identifier
     * @param key   the field key
     * @param value true if visible, false otherwise
     */
    void setVisible(String rowId, String key, boolean value) {
        form.setVisible(rowId, key, value);
    }

    /**
     * Sets the visible attribute for a mixin field.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param value    true if visible, false otherwise
     */
    @Override
    public void setVisible(String rowId, Identifier prefixId, Identifier id, boolean value) {
        setVisible(rowId, mixinMap.get(prefixId).get(id), value);
    }

    /**
     * Sets the visible attribute for a mixin field.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param value    true if visible, false otherwise
     */
    @Override
    public void setVisible(Identifier prefixId, Identifier id, boolean value) {
        setVisible(ElementRow.ROOT, mixinMap.get(prefixId).get(id), value);
    }

    /**
     * Sets the visible attribute for a field.
     *
     * @param id    the field identifier
     * @param value true if visible, false otherwise
     */
    @Override
    public void setVisible(Identifier id, boolean value) {
        setVisible(ElementRow.ROOT, id, value);
    }

    /**
     * Sets the visible attribute for a field and all its children recursively.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @param value true if visible, false otherwise
     */
    @Override
    public void setVisibleCascading(final String rowId, final Identifier id, final boolean value) {
        setVisibleCascading(rowId, id.getIdentifier(), value);
    }

    /**
     * Sets the visible attribute for a field and all its children recursively.
     *
     * @param id    the field identifier
     * @param value true if visible, false otherwise
     */
    @Override
    public void setVisibleCascading(Identifier id, boolean value) {
        setVisibleCascading(ElementRow.ROOT, id, value);
    }

    /**
     * Sets the visible attribute for a mixin field and all its children recursively.
     *
     * @param prefixId the mixin prefix identifier
     * @param rowId    the row identifier
     * @param id       the field identifier
     * @param value    true if visible, false otherwise
     */
    @Override
    public void setVisibleCascading(Identifier prefixId, String rowId, Identifier id, boolean value) {
        setVisibleCascading(rowId, mixinMap.get(prefixId).get(id), value);
    }

    /**
     * Sets the visible attribute for a mixin field and all its children recursively.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param value    true if visible, false otherwise
     */
    @Override
    public void setVisibleCascading(Identifier prefixId, Identifier id, boolean value) {
        setVisibleCascading(ElementRow.ROOT, mixinMap.get(prefixId).get(id), value);
    }

    /**
     * Sets the visible attribute for a field and all its children recursively.
     *
     * @param rowId the row identifier
     * @param key   the field key
     * @param value true if visible, false otherwise
     */
    void setVisibleCascading(final String rowId, final String key, final boolean value) {
        setVisible(rowId, key, value);
        final var ed = form.getSd().findElementByKey(key);
        ed.getChildren().forEach(it -> it.forEach(
                e -> setVisibleCascading(rowId, e.getKey(), value)));
    }

    /**
     * Checks if a field is editable.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @return true if editable, false otherwise
     */
    @Override
    public boolean isEditable(String rowId, Identifier id) {
        var opt = form.getEditable(rowId, id.getIdentifier());
        return opt != null && opt.isPresent() && opt.get();
    }

    /**
     * Checks if a mixin field is editable.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @return true if editable, false otherwise
     */
    @Override
    public boolean isEditable(String rowId, Identifier prefixId, Identifier id) {
        return isEditable(rowId, mixinMap.get(prefixId).get(id));
    }

    /**
     * Checks if a field is editable.
     *
     * @param id the field identifier
     * @return true if editable, false otherwise
     */
    @Override
    public boolean isEditable(Identifier id) {
        return isEditable(ElementRow.ROOT, id);
    }

    /**
     * Checks if a mixin field is editable.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @return true if editable, false otherwise
     */
    @Override
    public boolean isEditable(Identifier prefixId, Identifier id) {
        return isEditable(ElementRow.ROOT, mixinMap.get(prefixId).get(id));
    }

    /**
     * Sets the editable attribute for a mixin field.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param value    true if editable, false otherwise
     */
    @Override
    public void setEditable(Identifier prefixId, Identifier id, boolean value) {
        setEditable(ElementRow.ROOT, mixinMap.get(prefixId).get(id), value);
    }

    /**
     * Sets the editable attribute for a field.
     *
     * @param id    the field identifier
     * @param value true if editable, false otherwise
     */
    @Override
    public void setEditable(Identifier id, boolean value) {
        setEditable(ElementRow.ROOT, id, value);
    }

    /**
     * Sets the editable attribute for a field.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @param value true if editable, false otherwise
     */
    @Override
    public void setEditable(String rowId, Identifier id, boolean value) {
        form.setEditable(rowId, id.getIdentifier(), value);
    }

    /**
     * Sets the editable attribute for a mixin field.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param value    true if editable, false otherwise
     */
    @Override
    public void setEditable(String rowId, Identifier prefixId, Identifier id, boolean value) {
        setEditable(rowId, mixinMap.get(prefixId).get(id), value);
    }

    //-----------------------------------------------------------------------------------------------------------------
    // Message handling

    /**
     * Gets the message for a field as an Optional.
     *
     * @param rowId the row identifier
     * @param id    the field identifier
     * @return Optional containing the message, or empty if not present
     */
    @Override
    public Optional<Message> getMessage(String rowId, Identifier id) {
        return form.getMessage(rowId, id.getIdentifier());
    }

    /**
     * Gets the message for a field as an Optional.
     *
     * @param id the field identifier
     * @return Optional containing the message, or empty if not present
     */
    @Override
    public Optional<Message> getMessage(Identifier id) {
        return getMessage(ElementRow.ROOT, id);
    }

    /**
     * Gets the message for a mixin field as an Optional.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @return Optional containing the message, or empty if not present
     */
    @Override
    public Optional<Message> getMessage(String rowId, Identifier prefixId, Identifier id) {
        return getMessage(rowId, mixinMap.get(prefixId).get(id));
    }

    /**
     * Gets the message for a mixin field as an Optional.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @return Optional containing the message, or empty if not present
     */
    @Override
    public Optional<Message> getMessage(Identifier prefixId, Identifier id) {
        return getMessage(ElementRow.ROOT, prefixId, id);
    }

    /**
     * Sets the message for a field.
     *
     * @param rowId   the row identifier
     * @param key     the field key
     * @param message the message to set
     */
    @Override
    public void setMessage(String rowId, String key, Message message) {
        form.setMessage(rowId, key, message);
    }

    /**
     * Sets the message for a field.
     *
     * @param rowId   the row identifier
     * @param id      the field identifier
     * @param message the message to set
     */
    @Override
    public void setMessage(String rowId, Identifier id, Message message) {
        setMessage(rowId, id.getIdentifier(), message);
    }

    /**
     * Sets the message for a mixin field.
     *
     * @param rowId    the row identifier
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param message  the message to set
     */
    @Override
    public void setMessage(String rowId, Identifier prefixId, Identifier id, Message message) {
        setMessage(rowId, mixinMap.get(prefixId).get(id), message);
    }

    /**
     * Sets the message for a mixin field.
     *
     * @param prefixId the mixin prefix identifier
     * @param id       the field identifier
     * @param message  the message to set
     */
    @Override
    public void setMessage(Identifier prefixId, Identifier id, Message message) {
        setMessage(ElementRow.ROOT, prefixId, id, message);
    }

    /**
     * Sets the message for a field.
     *
     * @param id      the field identifier
     * @param message the message to set
     */
    @Override
    public void setMessage(Identifier id, Message message) {
        setMessage(ElementRow.ROOT, id, message);
    }

    /**
     * Gets all field positions with a message of at least the given severity.
     *
     * @param rowId       the row identifier
     * @param minSeverity the minimum severity
     * @return collection of positions with messages
     */
    @Override
    public Collection<Position> getFieldsWithMessage(String rowId, Severity minSeverity) {
        final var coll = new ArrayList<Position>();
        final var row = FormUtils.findRowById(form, null, rowId);

        if (row != null && row.getLeft() != null) {
            iterateFindFieldsWithMessage(row.getLeft(), coll, minSeverity);
        }

        return coll;
    }

    /**
     * Recursively finds fields with messages of at least the given severity.
     *
     * @param row         the element row
     * @param coll        the collection to add positions to
     * @param minSeverity the minimum severity
     */
    private void iterateFindFieldsWithMessage(final ElementRow row, final Collection<Position> coll,
                                              final Severity minSeverity) {
        for (var element : row.getElements().values()) {
            if (element.getMessage() != null && Severity.hasMinSeverity(minSeverity, element.getMessage())) {
                coll.add(new Position(row.getRowId(), element.getKey()));
            }
            if (element.getValue() instanceof Table) {
                for (var it : ((Table) element.getValue()).getData().values()) {
                    iterateFindFieldsWithMessage(it, coll, minSeverity);
                }
            }
        }
    }

    /**
     * Checks if there are any messages of at least the given severity in the row.
     *
     * @param rowId       the row identifier
     * @param minSeverity the minimum severity
     * @return true if such messages exist, false otherwise
     */
    @Override
    public boolean hasMessages(String rowId, Severity minSeverity) {
        final var row = FormUtils.findRowById(form, null, rowId);
        if (row != null) {
            return iterateHasMessages(row.getLeft(), minSeverity);
        }
        return false;
    }

    /**
     * Recursively checks for messages of at least the given severity.
     *
     * @param row         the element row
     * @param minSeverity the minimum severity
     * @return true if such messages exist, false otherwise
     */
    private boolean iterateHasMessages(final ElementRow row, final Severity minSeverity) {
        if (row != null) {
            for (var element : row.getElements().values()) {
                if (element.getMessage() != null && Severity.hasMinSeverity(minSeverity, element.getMessage())) {
                    return true;
                }
                if (element.getValue() instanceof Table) {
                    for (var it : ((Table) element.getValue()).getData().values()) {
                        if (iterateHasMessages(it, minSeverity)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checks if there are any messages of at least the given severity in the form.
     *
     * @param minSeverity the minimum severity
     * @return true if such messages exist, false otherwise
     */
    @Override
    public boolean hasMessages(Severity minSeverity) {
        return hasMessages(ElementRow.ROOT, minSeverity);
    }

    /**
     * Checks if there are any error messages in the form.
     *
     * @return true if errors exist, false otherwise
     */
    @Override
    public boolean hasErrors() {
        return hasMessages(Severity.Error);
    }

    /**
     * Checks if there are any warning or error messages in the form.
     *
     * @return true if warnings or errors exist, false otherwise
     */
    @Override
    public boolean hasWarningsOrErrors() {
        return hasMessages(Severity.Warning);
    }

    //-----------------------------------------------------------------------------------------------------------------

    /**
     * Iterates over all elements in the form using the given visitor and context.
     *
     * @param visitor the element visitor
     * @param ctx     the context
     */
    @Override
    public void forEach(ElementVisitor visitor, Context<? extends AccessClass> ctx) {
        form.forEach(form, visitor, ctx);
    }

    //-----------------------------------------------------------------------------------------------------------------
    // table handling

    /**
     * Adds a new row to a table field.
     *
     * @param id  the table field identifier
     * @param ctx the context
     * @return the new row identifier, or null if not added
     */
    @Override
    public String addRow(final Identifier id, final Context<? extends AccessClass> ctx) {
        return addRow(ElementRow.ROOT, id, ctx);
    }

    /**
     * Adds a new row to a table field.
     *
     * @param rowId the parent row identifier
     * @param id    the table field identifier
     * @param ctx   the context
     * @return the new row identifier, or null if not added
     */
    @Override
    public String addRow(final String rowId, final Identifier id, final Context<? extends AccessClass> ctx) {
        final var row = FormUtils.addRow(form, rowId, id.getIdentifier(), ctx);
        return row == null ? null : row.getRowId();
    }

    /**
     * Deletes a row from the form.
     *
     * @param rowId the row identifier
     */
    @Override
    public void deleteRow(String rowId) {
        FormUtils.deleteRow(form, rowId);
    }

    /**
     * Deletes all rows from a table field.
     *
     * @param id the table field identifier
     */
    @Override
    public void deleteAll(Identifier id) {
        this.deleteAll(ElementRow.ROOT, id);
    }

    /**
     * Deletes all rows from a table field.
     *
     * @param rowId the parent row identifier
     * @param id    the table field identifier
     */
    @Override
    public void deleteAll(String rowId, Identifier id) {
        FormUtils.deleteAll(form, rowId, id.getIdentifier());
    }

    /**
     * Filters rows in a table field using the given predicate.
     *
     * @param id        the table field identifier
     * @param predicate the predicate to filter rows
     */
    @Override
    public void filter(final Identifier id, final Predicate<String> predicate) {
        filter(ElementRow.ROOT, id, predicate);
    }

    /**
     * Filters rows in a table field using the given predicate.
     *
     * @param rowId     the parent row identifier
     * @param id        the table field identifier
     * @param predicate the predicate to filter rows
     */
    @Override
    public void filter(final String rowId, final Identifier id, final Predicate<String> predicate) {
        FormUtils.filter(form, rowId, id.getIdentifier(), predicate);
    }

    /**
     * Gets the list of row identifiers for a table field.
     *
     * @param rowId the parent row identifier
     * @param id    the table field identifier
     * @return list of row identifiers
     */
    @Override
    public List<String> getRows(String rowId, Identifier id) {
        return FormUtils.getRows(form, rowId, id.getIdentifier());
    }

    /**
     * Gets the list of row identifiers for a table field.
     *
     * @param id the table field identifier
     * @return list of row identifiers
     */
    @Override
    public List<String> getRows(Identifier id) {
        return FormUtils.getRows(form, ElementRow.ROOT, id.getIdentifier());
    }

    /**
     * Checks if a row is selected.
     *
     * @param rowId the row identifier
     * @return true if selected, false otherwise
     */
    @Override
    public boolean isSelected(String rowId) {
        return FormUtils.isSelected(form, rowId);
    }

    /**
     * Sets the selected state for a row.
     *
     * @param rowId the row identifier
     * @param value true if selected, false otherwise
     */
    @Override
    public void setSelected(String rowId, final boolean value) {
        FormUtils.setSelected(form, rowId, value);
    }

    /**
     * Gets the list of selected row identifiers for a table field.
     *
     * @param rowId the parent row identifier
     * @param id    the table field identifier
     * @return list of selected row identifiers
     */
    @Override
    public List<String> getSelected(String rowId, Identifier id) {
        return FormUtils.getSelected(form, rowId, id.getIdentifier());
    }

    /**
     * Gets the list of selected row identifiers for a table field.
     *
     * @param id the table field identifier
     * @return list of selected row identifiers
     */
    @Override
    public List<String> getSelected(Identifier id) {
        return FormUtils.getSelected(form, ElementRow.ROOT, id.getIdentifier());
    }

    /**
     * Iterates over all rows in a table field using the given visitor and context.
     *
     * @param rowId   the parent row identifier
     * @param id      the table field identifier
     * @param visitor the row visitor
     * @param ctx     the context
     */
    @Override
    public void forEach(String rowId, Identifier id, RowVisitor visitor, Context<? extends AccessClass> ctx) {
        FormUtils.forEach(form, rowId, id.getIdentifier(), visitor, ctx);
    }

    /**
     * Iterates over all rows in a table field using the given visitor and context.
     *
     * @param id      the table field identifier
     * @param visitor the row visitor
     * @param ctx     the context
     */
    @Override
    public void forEach(Identifier id, RowVisitor visitor, Context<? extends AccessClass> ctx) {
        FormUtils.forEach(form, ElementRow.ROOT, id.getIdentifier(), visitor, ctx);
    }

    /**
     * Gets the current page for a table field.
     *
     * @param rowId
     * @param id
     * @return
     */
    @Override
    public int getPage(String rowId, Identifier id) {
        final var element = FormUtils.findElementByRowAndKey(form, rowId, id.getIdentifier());
        if (element != null && element.getValue() instanceof Table table) {
            return table.getPos();
        }
        throw new FormsCoreException("Element not found or not a table: " + id.getIdentifier());
    }

    /**
     * Gets the page size for a table field.
     *
     * @param rowId
     * @param id
     * @return
     */
    @Override
    public int getPageSize(String rowId, Identifier id) {
        final var element = FormUtils.findElementByRowAndKey(form, rowId, id.getIdentifier());
        if (element != null && element.getValue() instanceof Table table) {
            return table.getPageSize();
        }
        throw new FormsCoreException("Element not found or not a table: " + id.getIdentifier());
    }

    /**
     * Sets the page for a table field.
     *
     * @param rowId
     * @param id
     * @param page
     */
    @Override
    public void setPage(String rowId, Identifier id, int page) {
        final var element = FormUtils.findElementByRowAndKey(form, rowId, id.getIdentifier());
        if (element != null && element.getValue() instanceof Table table) {
            table.setPos(page);
            form.getJournal().updateValue(element, rowId, table, true);
        } else {
            throw new FormsCoreException("Element not found or not a table: " + id.getIdentifier());
        }
    }

    /**
     * Sets the page size for a table field.
     *
     * @param rowId
     * @param id
     * @param pageSize
     */
    @Override
    public void setPageSize(String rowId, Identifier id, int pageSize) {
        final var element = FormUtils.findElementByRowAndKey(form, rowId, id.getIdentifier());
        if (element != null && element.getValue() instanceof Table table) {
            table.setPageSize(pageSize);
            form.getJournal().updateValue(element, rowId, table, true);
        } else {
            throw new FormsCoreException("Element not found or not a table: " + id.getIdentifier());
        }
    }
}