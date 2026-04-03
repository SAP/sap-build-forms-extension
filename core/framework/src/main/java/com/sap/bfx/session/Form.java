package com.sap.bfx.session;

import com.sap.bfx.callback.AccessClass;
import com.sap.bfx.callback.Context;
import com.sap.bfx.callback.ElementVisitor;
import com.sap.bfx.definition.*;
import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.exception.FormsCoreException;
import com.sap.bfx.exception.NotFoundException;
import com.sap.bfx.utils.EnumUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

/**
 * Form class representing a form instance with its elements and associated metadata.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class Form extends ElementRow implements FormAttributes {

    private BackendJournal journal;
    private ScenarioDefinition sd;

    private String changedBy;
    private Instant changedAt;
    private String description;
    private Instant finishedAt;
    private String functionalId;
    private String id;
    private String refId;
    private String scenarioName;
    private int scenarioVersion;
    private String startedBy;
    private Instant startedAt;
    private ProcessState state;
    private String detailState;
    private String templateName;
    private long version;
    private String workflowAdapter;

    /**
     * Root row id constant
     */
    public Form() {
        this(null, null);
    }

    /**
     * Constructor for the Form class.
     *
     * @param sd      the scenario definition to use for the form
     * @param journal the journal to use for the form
     */
    public Form(final ScenarioDefinition sd, final BackendJournal journal) {
        super();

        this.setRowId(ROOT);
        this.init(sd, journal);
    }

    /**
     * Constructor for the Form class with additional parameters.
     *
     * @param sd           the scenario definition to use for the form
     * @param journal      the journal to use for the form
     * @param id           the id of the form
     * @param version      the version of the form
     * @param scenarioName the name of the scenario
     * @param refId        the reference id of the form
     */
    public Form(final ScenarioDefinition sd, final BackendJournal journal, final String id, final Long version,
                final String scenarioName, final int scenarioVersion, final String refId) {
        super();

        this.setRowId(ROOT);
        this.init(sd, journal, id, version, scenarioName, scenarioVersion, refId);
    }

    /**
     * Initializes the form with the given scenario definition and journal.
     *
     * @param sd      the scenario definition to use for the form
     * @param journal the journal to use for the form
     */
    public final void init(final ScenarioDefinition sd, final BackendJournal journal) {
        this.sd = sd;
        if (sd != null) {
            this.setScenarioVersion(sd.getVersion());
            this.setScenarioName(sd.getName());
        }
        this.journal = journal;
        this.state = ProcessState.Draft;
    }

    /**
     * Initializes the form with the given parameters.
     *
     * @param sd              the scenario definition to use for the form
     * @param journal         the journal to use for the form
     * @param id              the id of the form
     * @param version         the version of the form
     * @param scenarioName    the name of the scenario
     * @param scenarioVersion the version of the scenario
     * @param refId
     */
    public final void init(final ScenarioDefinition sd, final BackendJournal journal, final String id,
                           final Long version, final String scenarioName, final int scenarioVersion,
                           final String refId) {
        this.init(sd, journal);
        this.id = id;
        this.version = version;
        this.scenarioName = scenarioName;
        this.scenarioVersion = scenarioVersion;
        this.refId = refId;
    }

    /**
     * Finds the root element of the form.
     *
     * @return the root element of the form, which is the first element in the form
     */
    public Element findRootElement() {
        return FormUtils.findElementByRowAndKey(this, ROOT, sd.getRootElementKey());
    }

    /**
     * Returns the value of the specified element in the form.
     *
     * @param rowId rowId of the element
     * @param key   key of the element
     * @return an Optional containing the value of the element, or an empty Optional if the element is not
     * found or has no value
     */
    public Optional<Object> getValue(final String rowId, final String key) {
        final var element = FormUtils.findElementByRowAndKey(this, rowId, key);
        if (element == null) {
            log.error("Cannot find element for rowId '" + rowId + "' and key '" + key + "'");
            return Optional.empty();
        }
        if (element.getValue() == null) {
            return Optional.empty();
        }
        return Optional.of(element.getValue());
    }

    /**
     * Returns the value of the specified element in the form, cast to the specified class.
     *
     * @param rowId rowId of the element
     * @param key   key of the element
     * @param clz   class to cast the value to
     * @param <T>   the type of the value
     * @return an Optional containing the value of the element cast to the specified class,
     * or an empty Optional if the element is not found or has no value
     */
    public <T> Optional<T> getValue(final String rowId, final String key, final Class<T> clz) {
        var opt = getValue(rowId, key);
        if (opt.isEmpty()) {
            return Optional.empty();
        }
        //noinspection unchecked
        return Optional.of((T) opt.get());
    }

    /**
     * Sets the value of the specified element in the form.
     *
     * @param rowId rowId of the element
     * @param key   key of the element
     * @param value value to set
     */
    public void setValue(final String rowId, final String key, final Object value) {
        final var element = FormUtils.findElementByRowAndKey(this, rowId, key);
        if (element == null) {
            log.error("Cannot find element for rowid '{}' and key '{}'", rowId, key);
            return;
        }
        journal.updateValue(element, rowId, value, false);
        element.setValue(value);
    }

    /**
     * Returns the visibility of the specified element in the form.
     *
     * @param rowId rowId of the element
     * @param key   key of the element
     * @return an Optional containing the visibility of the element, or an empty Optional if the element is not
     * found
     */
    public Optional<Boolean> getVisible(final String rowId, final String key) {
        final var element = FormUtils.findElementByRowAndKey(this, rowId, key);
        if (element == null) {
            log.error("Cannot find element for rowId '" + rowId + "' and key '" + key + "'");
            return Optional.empty();
        }
        return Optional.of(element.isVisible());
    }

    /**
     * Sets the visibility of the specified element in the form.
     *
     * @param rowId rowId of the element
     * @param key   key of the element
     * @param value visibility value to set
     */
    public void setVisible(final String rowId, final String key, final boolean value) {
        final var element = FormUtils.findElementByRowAndKey(this, rowId, key);
        if (element == null) {
            log.error("Cannot find element for rowid '{}' and key '{}'", rowId, key);
            return;
        }
        journal.updateVisible(element, rowId, value);
        element.setVisible(value);
    }

    /**
     * Returns the editability of the specified element in the form.
     *
     * @param rowId rowId of the element
     * @param key   key of the element
     * @return an Optional containing the editability of the element, or an empty Optional if the element is not
     * found
     */
    public Optional<Boolean> getEditable(final String rowId, final String key) {
        final var element = FormUtils.findElementByRowAndKey(this, rowId, key);
        if (element == null) {
            log.error("Cannot find element for rowId '" + rowId + "' and key '" + key + "'");
            return Optional.empty();
        }
        return Optional.of(element.isEditable());
    }

    /**
     * Sets the editability of the specified element in the form.
     *
     * @param rowId rowId of the element
     * @param key   key of the element
     * @param value editability value to set
     */
    public void setEditable(final String rowId, final String key, final boolean value) {
        final var element = FormUtils.findElementByRowAndKey(this, rowId, key);
        if (element == null) {
            log.error("Cannot find element for rowid '{}' and key '{}'", rowId, key);
            return;
        }
        journal.updateEditable(element, rowId, value);
        element.setEditable(value);
    }

    /**
     * Returns the required status of the specified element in the form.
     *
     * @param rowId rowId of the element
     * @param key   key of the element
     * @return an Optional containing the required status of the element, or an empty Optional if the element is not
     * found
     */
    public Optional<Boolean> getRequired(final String rowId, final String key) {
        final var element = FormUtils.findElementByRowAndKey(this, rowId, key);
        if (element == null) {
            log.error("Cannot find element for rowId '" + rowId + "' and key '" + key + "'");
            return Optional.empty();
        }
        return Optional.of(element.isRequired());
    }

    /**
     * Sets the required status of the specified element in the form.
     *
     * @param rowId rowId of the element
     * @param key   key of the element
     * @param value required status value to set
     */
    public void setRequired(final String rowId, final String key, final boolean value) {
        final var element = FormUtils.findElementByRowAndKey(this, rowId, key);
        if (element == null) {
            log.error("Cannot find element for rowid '{}' and key '{}'", rowId, key);
            return;
        }
        journal.updateRequired(element, rowId, value);
        element.setRequired(value);
    }

    /**
     * Returns the selected status of the specified row in the form.
     *
     * @param rowId rowId of the element
     * @return an Optional containing the selected status of the row, or an empty Optional if the row is not
     * found
     */
    public Optional<Boolean> getSelected(final String rowId) {
        final var row = FormUtils.findRowById(this, null, rowId);
        if (row == null) {
            log.error("Cannot find row for rowid '{}'", rowId);
            return Optional.empty();
        }
        return Optional.of(row.getLeft().isSelected());
    }

    /**
     * Sets the selected status of the specified row in the form.
     *
     * @param rowId    rowId of the element
     * @param isSingle indicates if single selection is enforced
     * @param value    selected status value to set
     */
    public void setSelected(final String rowId, final boolean isSingle, final boolean value) {
        final var row = FormUtils.findRowById(this, null, rowId);
        if (row == null) {
            log.error("Cannot find row for rowid '{}'", rowId);
            return;
        }
        if (row.getRight() == null) {
            // we have no parent! This means we on the root element, and we don't select on this
            return;
        }

        if (isSingle) {
            for (var it : ((Table) row.getRight().getValue()).getData().values()) {
                if (!StringUtils.equals(it.getRowId(), rowId) && it.isSelected()) {
                    journal.updateSelected(it.getRowId(), false);
                } else if (StringUtils.equals(it.getRowId(), rowId) && it.isSelected() != value) {
                    journal.updateSelected(rowId, value);
                }
            }
        } else {
            for (var it : ((Table) row.getRight().getValue()).getData().values()) {
                if (StringUtils.equals(it.getRowId(), rowId) && it.isSelected() != value) {
                    journal.updateSelected(rowId, value);
                }
            }
        }
    }

    /**
     * Returns the message of the specified element in the form.
     *
     * @param rowId rowId of the element
     * @param key   key of the element
     * @return an Optional containing the message of the element, or an empty Optional if the element is not
     * found
     */
    public Optional<Message> getMessage(final String rowId, final String key) {
        final var element = FormUtils.findElementByRowAndKey(this, rowId, key);
        if (element == null) {
            log.error("Cannot find element for rowId '" + rowId + "' and key '" + key + "'");
            return Optional.empty();
        }
        // in case the element has currently no message
        if (element.getMessage() == null) {
            return Optional.empty();
        }
        return Optional.of(element.getMessage());
    }

    /**
     * Sets the message of the specified element in the form.
     *
     * @param rowId   rowId of the element
     * @param key     key of the element
     * @param message message to set
     */
    public void setMessage(final String rowId, final String key, final Message message) {
        final var element = FormUtils.findElementByRowAndKey(this, rowId, key);
        if (element == null) {
            log.error("Cannot find element for rowid '{}' and key '{}'", rowId, key);
            return;
        }
        journal.updateMessage(element, rowId, message);
        element.setMessage(message);
    }

    /**
     * Traverse all elements in the given row and its child rows, calling the visitor for each element.
     *
     * @param row     row to traverse
     * @param visitor visitor to call for each element
     * @param ctx     context for the traversal
     */
    public void forEach(final ElementRow row, final ElementVisitor visitor, Context<? extends AccessClass> ctx) {
        for (var element : row.getElements().values()) {
            final var ed = sd.findElementByKey(element.getKey());
            if (!visitor.visit(ed, row.getRowId(), ctx)) {
                break;
            }
            if (element.getValue() instanceof Table) {
                for (var it : ((Table) element.getValue()).getData().values()) {
                    forEach(it, visitor, ctx);
                }
            }
        }
    }

    /**
     * find attachment by id
     *
     * @param key key of the attachment element
     * @param id  id of the attachment
     * @return optional pair of attachment and attachments container
     */
    public Optional<Pair<Attachment, Attachments>> findAttachmentById(final String key, final String id) {
        return this.findAttachmentById(this.getElements(), key, id);
    }

    /**
     * Recursive method to find an attachment by its ID within the given elements.
     *
     * @param elements elements to search
     * @param key      key of the attachment element
     * @param id       id of the attachment
     * @return optional pair of attachment and attachments container
     */
    private Optional<Pair<Attachment, Attachments>> findAttachmentById(final ElementMap elements, final String key,
                                                                       final String id) {

        if (elements.containsKey(key)) {
            final var attachments = (Attachments) elements.get(key).getValue();
            for (var it : attachments) {
                if (StringUtils.equals(it.getId(), id)) {
                    return Optional.of(new ImmutablePair<>(it, attachments));
                }
            }
        }

        for (var element : elements.values()) {
            final var ed = sd.findElementByKey(element.getKey());
            if (ed.isCollection()) {
                for (var row : ((Table) element.getValue()).getData().values()) {
                    var result = findAttachmentById(row.getElements(), key, id);
                    if (result.isPresent()) {
                        return result;
                    }
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Applies the changes from the given FrontendJournal to the form.
     *
     * @param fj             the FrontendJournal containing the changes to apply
     * @param backendJournal the BackendJournal to record changes that need to be processed server-side
     */
    public void apply(final FrontendJournal fj, BackendJournal backendJournal) {
        // we skip add-rows and delete-rows as currently frontend cannot add or delete rows

        // updates from the journal.updated structure
        fj.getUpdated().forEach(itUpdate -> {
            final var element = FormUtils.findElementByRowAndKey(this, itUpdate.getRowId(), itUpdate.getKey());
            final var def = sd.findElementByKey(itUpdate.getKey());
            var addToBackendJournal = false;

            for (var itChange : itUpdate.getChanges()) {
                if (itChange.getProp() == ChangePropertyType.Selected) {
                    var r = FormUtils.findRowById(this, null, itUpdate.getRowId());
                    if (r == null || r.getLeft() == null) {
                        throw new NotFoundException("Cannot find row='" + itUpdate.getRowId() + "'");
                    }
                    r.getLeft().setSelected((Boolean) itChange.getValue());
                } else {
                    if (element == null) {
                        throw new NotFoundException(
                                "Cannot find element with row='" + itUpdate.getRowId() + "' and key='" +
                                        itUpdate.getKey() + "'");
                    }
                    if (itChange.getProp() == ChangePropertyType.Value) {
                        final var value = parseValue(this.sd, itUpdate.getKey(), itChange.getValue());

                        if (def.getType() == UIElementType.Form || def.getType() == UIElementType.Wizard ||
                                def.getType() == UIElementType.Group) {
                            // values of these elements are just for display so we can set them always without checking
                            element.setValue(value);
                        } else if (def.getType() == UIElementType.DocForm) {
                            // here only the selected tab because docUrl should not be changed in frontend
                            ((DocFormData) element.getValue()).setSelectedTab(((DocFormData) value).getSelectedTab());
                        } else {
                            // check if the element is editable, if not, we throw an exception
                            if (!element.isEditable()) {
                                throw new FormsCoreException(
                                        "Not allowed to modify value on row='" + itUpdate.getRowId() + "' and key='" +
                                                itUpdate.getKey() + "'");
                            }
                            element.setValue(value);
                        }
                    } else if (itChange.getProp() == ChangePropertyType.Visible) {
                        final var value = (boolean) itChange.getValue();
                        if (value) {
                            throw new FormsCoreException(
                                    "Not allowed to set visible on row='" + itUpdate.getRowId() + "' and key='" +
                                            itUpdate.getKey() + "' to true");
                        }
                        element.setVisible(false);
                    } else if (itChange.getProp() == ChangePropertyType.Position) {
                        ((Table) element.getValue()).setPos((Integer) itChange.getValue());
                        addToBackendJournal = true;
                    } else if (itChange.getProp() == ChangePropertyType.PageSize) {
                        ((Table) element.getValue()).setPageSize((Integer) itChange.getValue());
                        addToBackendJournal = true;
                    } else if (itChange.getProp() == ChangePropertyType.SortField) {
                        ((Table) element.getValue()).setSortField((String) itChange.getValue());
                        addToBackendJournal = true;
                    } else if (itChange.getProp() == ChangePropertyType.SortOrder) {
                        ((Table) element.getValue()).setSortOrder(
                                EnumUtils.valueById(SortOrder.class, (String) itChange.getValue(),
                                        SortOrder.ASCENDING));
                        addToBackendJournal = true;
                    }
                }
            }
            // some fields just need to be passed through as changes, e.g. everything with browsing in table
            if (addToBackendJournal) {
                backendJournal.updateValue(element, itUpdate.getRowId(), element.getValue(), true);
            }
        });
        // compute the information from journal.deleted structure (indicating deleted rows in tables
        fj.getDeleted().forEach(it -> {
            final var element = FormUtils.findElementByRowAndKey(this, it.getRowId(), it.getKey());
            if (element == null) {
                throw new NotFoundException(
                        "Cannot find element with row='" + it.getRowId() + "' and key='" + it.getKey() + "'");
            }

            final var table = (Table) element.getValue();
            for (var delId : it.getIds()) {
                // remove row from the rows list
                table.getRows().removeIf(row -> StringUtils.equals(row, delId));
                // remove the row from the map
                table.getData().remove(delId);
                // we cannot have a position higher than the number of rows. In this case correct the value
                if (table.getPos() >= table.getData().size()) {
                    table.setPos(Math.max(table.getData().size() - table.getPageSize(), 0));
                }
            }
        });
    }

    /**
     * Parses the given source object into the appropriate data type based on the scenario definition and key.
     *
     * @param sd     the scenario definition
     * @param key    the key of the element
     * @param source the source object to parse
     * @return the parsed object
     */
    private Object parseValue(final ScenarioDefinition sd, final String key, final Object source) {
        final var dt = ElementDefinition.getDataTypeClass(sd.findElementByKey(key));

        try {
            if (dt == Integer.class) {
                return source;
            } else if (dt == LocalDateTime.class) {
                return source;
            } else if (dt == LocalTime.class) {
                return source;
            } else if (dt == LocalDate.class) {
                return source;
            } else if (dt == BigDecimal.class) {
                return source;
            } else if (dt == String.class) {
                return source;
            } else if (dt == Boolean.class) {
                return source;
            } else if (dt == Table.class) {
                return source;
            } else if (dt == Attachments.class) {
                throw new FormsCoreException(
                        "Updates from applying journal should not occur because this is always done server-side");
            } else if (dt == DateRange.class) {
                return source;
            } else if (dt == MoneyAmount.class) {
                return source;
            } else if (dt == DocFormData.class) {
                return source;
            }
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }

        throw new FormsCoreException("Unhandled type '" + dt.getName() + "'");
    }
}
