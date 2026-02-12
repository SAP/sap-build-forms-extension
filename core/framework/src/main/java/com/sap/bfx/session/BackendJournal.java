package com.sap.bfx.session;

import com.sap.bfx.definition.Message;
import com.sap.bfx.definition.ScenarioDefinition;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * The `BackendJournal` class is responsible for tracking changes made to form elements,
 * including their values, visibility, editability, and other properties. It also manages
 * initial table rows and provides methods to update and retrieve changes.
 */
@Slf4j
public class BackendJournal {

    @Getter
    private final ScenarioDefinition sd;
    @Getter
    private final Map<String, Map<String, ChangeElement>> changes = new HashMap<>();
    @Getter
    private final Map<Position, List<String>> initialTableRows = new HashMap<>();

    /**
     * Constructs a `BackendJournal` with the given scenario definition.
     *
     * @param sd the scenario definition to associate with this journal
     */
    public BackendJournal(final ScenarioDefinition sd) {
        this.sd = sd;
    }

    /**
     * Updates the value of an element if it has changed or if forced.
     *
     * @param element the element to update
     * @param rowId   the row identifier
     * @param value   the new value to set
     * @param forced  whether to force the update even if the value hasn't changed
     */
    public void updateValue(@NonNull Element element, String rowId, Object value, boolean forced) {
        if (forced || !Objects.equals(element.getValue(), value)) {
            addUpdated(rowId, element, ChangePropertyType.Value, value);
        }
    }

    /**
     * Updates the editable property of an element if it has changed.
     *
     * @param element the element to update
     * @param rowId   the row identifier
     * @param value   the new editable state
     */
    public void updateEditable(Element element, String rowId, boolean value) {
        if (element.isEditable() != value) {
            this.addUpdated(rowId, element, ChangePropertyType.Editable, value);
        }
    }

    /**
     * Updates the visible property of an element if it has changed.
     *
     * @param element the element to update
     * @param rowId   the row identifier
     * @param value   the new visibility state
     */
    public void updateVisible(Element element, String rowId, boolean value) {
        if (element.isVisible() != value) {
            this.addUpdated(rowId, element, ChangePropertyType.Visible, value);
        }
    }

    /**
     * Updates the required property of an element if it has changed.
     *
     * @param element the element to update
     * @param rowId   the row identifier
     * @param value   the new required state
     */
    public void updateRequired(Element element, String rowId, boolean value) {
        if (element.isRequired() != value) {
            this.addUpdated(rowId, element, ChangePropertyType.Required, value);
        }
    }

    /**
     * Updates the selected state of a row.
     *
     * @param rowId the row identifier
     * @param value the new selected state
     * @return true (always returns true as the update is unconditional)
     */
    public boolean updateSelected(final String rowId, final boolean value) {
        this.addUpdated(rowId, null, ChangePropertyType.Selected, value);
        return true;
    }

    /**
     * Updates the message of an element if it has changed.
     *
     * @param element the element to update
     * @param rowId   the row identifier
     * @param msg     the new message to set
     */
    public void updateMessage(Element element, String rowId, Message msg) {
        if ((msg == null && element.getMessage() != null) ||
                (msg != element.getMessage()) ||
                (element.getMessage() != null && !element.getMessage().equals(msg)) ||
                (msg != null && !msg.equals(element.getMessage()))) {
            this.addUpdated(rowId, element, ChangePropertyType.Message, msg);
        }
    }

    /**
     * Adds a new row to the journal for a specific element.
     *
     * @param element the element associated with the row
     * @param rowId   the parent row identifier
     * @param table   the table containing the row
     */
    public void addRow(final Element element, final String rowId, final Table table) {
        addUpdated(rowId, element, ChangePropertyType.Value, table);
    }

    /**
     * Deletes a row from the journal for a specific element.
     *
     * @param element the element associated with the row
     * @param rowId   the row identifier to delete
     */
    public void deleteRow(final Element element, final String rowId) {
        addUpdated(rowId, element, ChangePropertyType.Value, element.getValue());
    }

    /**
     * Retrieves the rows that are necessary to be updated based on the current and initial rows.
     *
     * @param rowId the parent row identifier
     * @param key   the table key
     * @param table the table containing the rows
     * @return a list of row identifiers that need to be updated
     */
    public List<String> getNecessaryRows(final String rowId, final String key, final @NonNull Table table) {
        final var currentRows = table.getCurrentRows();
        final var initialRows = initialTableRows.get(new Position(rowId, key));
        // no initial-rows => return current
        if (initialRows == null) {
            return currentRows;
        }

        // if there are initial-rows => check what we already have on frontend
        final List<String> result = new ArrayList<>();
        for (var row : currentRows) {
            final var opt = initialRows.stream().filter(it -> StringUtils.equals(it, row)).findAny();
            if (opt.isEmpty()) {
                result.add(row);
            }
        }

        return result;
    }

    /**
     * Copies the initial table rows from the root element row.
     *
     * @param root the root element row
     */
    public void copyInitialTableRowsRoot(final ElementRow root) {
        initialTableRows.clear();
        copyInitialTableRows(root);
    }

    /**
     * Recursively copies the initial table rows from the given element row.
     *
     * @param row the element row to copy from
     */
    private void copyInitialTableRows(final ElementRow row) {
        row.getElements().values().forEach(element -> {
            if (element.getValue() instanceof Table table) {
                // save the current displayed rows
                initialTableRows.put(new Position(row.getRowId(), element.getKey()), table.getCurrentRows());
                // check for all rows in the table
                table.getData().values().forEach(this::copyInitialTableRows);
            }
        });
    }

    /**
     * Adds or updates a change for a specific element and property type in the journal.
     *
     * @param rowId   the row identifier
     * @param element the element being changed
     * @param type    the type of property change (e.g., value, visible, editable)
     * @param value   the new value for the property
     */
    public void addUpdated(final String rowId, final Element element, final ChangePropertyType type,
                           final Object value) {
        // check if the element has already been changed, retrieve correct change-element
        // check if the element has already been changed, retrieve correct change-element
        var ce = findChangeElement(changes, rowId, element.getKey());
        if (ce == null) {
            ce = createChangeElement(rowId, element.getKey());
        }

        // set the change with according type
        ce.getChanges().put(type, value);
        // For visible, we need to add all attributes of the given elements because it can be the case that this
        // element was not yet transferred to frontend and so frontend has no data at all!
        if (type == ChangePropertyType.Visible && Boolean.TRUE.equals(value)) {
            ce.getChanges().put(ChangePropertyType.Editable, element.isEditable());
            ce.getChanges().put(ChangePropertyType.Required, element.isRequired());
            ce.getChanges().put(ChangePropertyType.Message, element.getMessage());
            ce.getChanges().put(ChangePropertyType.Value, element.getValue());
        }
    }

    /**
     * Removes a row identifier from all initial table rows in the journal.
     *
     * @param rowId the row identifier to remove
     * @return Position of the table that the removed row belongs to, or null if not found
     */
    public Position removeFromInitialRows(final String rowId) {
        for (var pos : initialTableRows.keySet()) {
            final var rows = initialTableRows.get(pos);
            if (rows.removeIf(r -> StringUtils.equals(r, rowId))) {
                return pos;
            }
        }
        return null;
    }

    /**
     * Finds a change element in the journal for a specific row and key.
     *
     * @param changes the map of changes
     * @param rowId   the row identifier
     * @param key     the key of the element
     * @return the change element, or null if not found
     */
    private ChangeElement findChangeElement(Map<String, Map<String, ChangeElement>> changes, String rowId, String key) {
        var rowChanges = changes.get(rowId);
        if (rowChanges != null) {
            return rowChanges.get(key);
        }
        return null;
    }

    /**
     * Creates a new change element in the journal for a specific row and element.
     *
     * @param rowId the row identifier
     * @param key   the element key to create a change for
     * @return the newly created change element
     */
    private ChangeElement createChangeElement(final String rowId, final String key) {
        var rowChanges = changes.computeIfAbsent(rowId, k -> new HashMap<>());
        var ce = rowChanges.get(key);
        if (ce == null) {
            ce = new ChangeElement(rowId, key);
            rowChanges.put(key, ce);
        }
        return ce;
    }

    /**
     * Represents a change made to an element in the journal.
     */
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class ChangeElement extends Position {
        @Getter
        private Map<ChangePropertyType, Object> changes = new HashMap<>();

        /**
         * Constructs a new `ChangeElement` with the given row ID and key.
         *
         * @param rowId the row identifier
         * @param key   the key of the element
         */
        public ChangeElement(final String rowId, final String key) {
            super(rowId, key);
        }
    }
}
