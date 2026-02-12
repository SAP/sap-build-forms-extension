package com.sap.bfx.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sap.bfx.callback.*;
import com.sap.bfx.callback.AccessClass;
import com.sap.bfx.callback.Context;
import com.sap.bfx.callback.RowVisitor;
import com.sap.bfx.definition.*;
import com.sap.bfx.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Predicate;

/**
 * Form related utility methods
 */
@Slf4j
public class FormUtils {

    public static final String NM_CHANGED_BY = "cb";
    public static final String NM_CHANGED_AT = "ca";
    public static final String NM_ID = "id";
    public static final String NM_CATEGORY = "c";
    public static final String NM_CONTENT_TYPE = "ct";
    public static final String NM_DEF_NAME = "dn";
    public static final String NM_DEF_VERSION = "dv";
    public static final String NM_DESCRIPTION = "d";
    public static final String NM_DATERANGE_FROM = "f";
    public static final String NM_DATERANGE_TO = "t";
    public static final String NM_LINK_TEXT = "v";
    public static final String NM_LINK_HREF = "h";
    public static final String NM_LINK_TARGET = "t";
    public static final String NM_LOCALE = "l";
    public static final String NM_STATE = "s";
    public static final String NM_USERNAME = "u";
    public static final String NM_PERSONALIZATIONS = "pe";
    public static final String NM_PERSONALIZATIONS_ID = "pei";
    public static final String NM_PERSONALIZATIONS_USER = "peu";
    public static final String NM_PERSONALIZATIONS_KEY = "pek";
    public static final String NM_PERSONALIZATIONS_APP = "pea";
    public static final String NM_PERSONALIZATIONS_ENCODING = "pee";
    public static final String NM_PERSONALIZATIONS_VALUE = "pev";
    public static final String NM_FORM = "f";
    public static final String NM_POS = "p";
    public static final String NM_REF = "r";
    public static final String NM_ROW_ID = "rid";
    public static final String NM_SELECTED = "s";
    public static final String NM_SIZE = "s";
    public static final String NM_ELEMENTS = "e";
    public static final String NM_KEY = "k";
    public static final String NM_NAME = "n";
    public static final String NM_VISIBLE = "vi";
    public static final String NM_EDITABLE = "ed";
    public static final String NM_REQUIRED = "rq";
    public static final String NM_VALUE = "va";
    public static final String NM_JOURNAL = "journal";
    public static final String NM_MESSAGE = "m";
    public static final String NM_SEVERITY = "s";
    public static final String NM_TASK_INSTANCE_ID = "tid";
    public static final String NM_TEMPLATE = "tn";
    public static final String NM_FORM_ID = "fid";
    public static final String NM_FORM_VERSION = "fv";
    public static final String NM_FORM_SCENARIO_NAME = "fsn";
    public static final String NM_FORM_SCENARIO_VERSION = "fsv";
    public static final String NM_WORKFLOW_ADAPTER = "wkfapt";
    public static final String NM_FINISHED_AT = "fat";
    public static final String NM_FUNCTIONAL_ID = "fid";
    public static final String NM_CREATED_BY = "cby";
    public static final String NM_CREATED_AT = "cat";
    public static final String NM_DETAIL_STATE = "dst";

    /**
     * Create a new form instance based on the scenario definition provided.
     * All elements are created and default values are set. Default values
     * that are expression based are evaluated after all elements are created,
     * so that references to other elements can be used.
     *
     * @param sd        ScenarioDefinition
     * @param journal   Journal
     * @param acFactory AccessClassFactory
     * @param ctx       context
     * @return Form
     */
    public static Form create(final ScenarioDefinition sd, final BackendJournal journal,
                              final AccessClassFactory acFactory, final ContextImpl<? extends AccessClass> ctx) {
        final var form = new Form(sd, journal);
        final var expressions = new ArrayList<Pair<ElementDefinition, Element>>();

        // first, create all elements
        for (var ed : sd.getElements()) {
            createAndAddElement(form.getElements(), ed, expressions, ctx);
        }

        // second, expression based values are evaluated afterward, so that references to other elements
        // can be used. Order of expression evaluation is not determined!
        ctx.setData(acFactory.createAccessClass(sd, form));
        ctx.setDataApi(acFactory.createDataApi(sd, form));
        evalDefaultValueExpressions(expressions, ctx);

        if (log.isDebugEnabled()) {
            try {
                final var om = JsonMapper.builder().addModule(new JavaTimeModule()).build();
//                log.debug("Form.create: {}", om.writeValueAsString(form));
            } catch (Exception e) {
                log.error("Error parsing form ", e);
            }
        }

        return form;
    }

    /**
     * Add a new row to a table element identified by rowId and key.
     * The new row is created based on the element definition of the table
     * and all child elements are created as well. Default values are set
     * and expression based default values are evaluated after all elements
     * are created, so that references to other elements can be used.
     *
     * @param form
     * @param rowId
     * @param key
     * @param ctx
     * @return
     */
    public static ElementRow addRow(final Form form, final String rowId, final String key,
                                    final Context<? extends AccessClass> ctx) {

        final var element = FormUtils.findElementByRowAndKey(form, rowId, key);
        if (element == null) {
            return null;
        }
//        log.debug("Form.addRow: found element '{}' with value of class '{}'",
//                element.getName(), element.getValue().getClass().getName());
        final var ed = form.getSd().findElementByKey(key);
        if (ed == null) {
            return null;
        }
//        log.debug("Form.addRow: found element-definition '{}' with ui-element-type '{}'", ed.getName(), ed.getType());

        final var expressions = new ArrayList<Pair<ElementDefinition, Element>>();
        final var row = new ElementRow();
        row.setRowId(UUID.randomUUID().toString());
        ed.getElements().forEach(it -> createAndAddElement(row.getElements(), it, expressions, ctx));

        // second, expression based values are evaluated afterward, so that references to other elements
        // can be used. Order of expression evaluation is not determined!
        evalDefaultValueExpressions(expressions, ctx);

        // Add new row to form and the whole table to the journal
        ((Table) element.getValue()).addRow(row);
        form.getJournal().addRow(element, rowId, (Table) element.getValue());

        // calculate visual attributes
        row.getElements().keySet().forEach(k ->
                calculateVisualAttributes(form.getSd(), form, row.getElements().get(k), row.getRowId(),
                        true, true, ctx));

        return row;
    }

    /**
     * Delete a row identified by rowId. The root row cannot be deleted.
     *
     * @param form
     * @param rowId
     * @return
     */
    public static ElementRow deleteRow(final Form form, final String rowId) {
        // root row cannot be deleted!
        if (StringUtils.equals(rowId, ElementRow.ROOT)) {
            return null;
        }

        final var rowInfo = FormUtils.findRowById(form, null, rowId);
        if (rowInfo == null) {
            // not found: return null
            return null;
        }

        final var table = (Table) rowInfo.getValue().getValue();
        if (table.deleteRow(rowId)) {
            form.getJournal().deleteRow(rowInfo.getValue(), rowId);
        }
        return rowInfo.getKey();
    }

    /**
     * Delete all rows in a table identified by rowId and key.
     *
     * @param form
     * @param rowId
     * @param key
     */
    public static void deleteAll(final Form form, final String rowId, final String key) {
        final var element = FormUtils.findElementByRowAndKey(form, rowId, key);
        if (element == null) {
            log.error("Cannot find element for rowid '{}' and key '{}'", rowId, key);
            return;
        }

        if (element.getValue() instanceof Table table) {
            table.getRows().clear();
            table.clear();

            // we don't need to mark deletion for each row any more. Just adding the table as updated
            // value is enough.
            form.getJournal().updateValue(element, rowId, table, false);
        }
    }

    /**
     * Get all row IDs of a table identified by rowId and key.
     *
     * @param form
     * @param rowId
     * @param key
     * @return
     */
    public static List<String> getRows(final Form form, final String rowId, final String key) {
        final var element = FormUtils.findElementByRowAndKey(form, rowId, key);
        if (element != null && element.getValue() instanceof Table table) {
            return new ArrayList<String>(table.getRows());
        }
        log.error("Cannot find element for rowid '{}' and key '{}'", rowId, key);
        return null;
    }

    /**
     * Check if a row identified by rowId is selected.
     *
     * @param form
     * @param rowId
     * @return
     */
    public static boolean isSelected(final Form form, final String rowId) {
        final var r = FormUtils.findRowById(form, null, rowId);
        return r == null ? null : r.getLeft().isSelected();
    }

    /**
     * Set the selected state of a row identified by rowId.
     * If the row is part of a single-select table, all other rows
     * are set to non-selected.
     *
     * @param form
     * @param rowId
     * @param value
     * @return
     */
    public static void setSelected(final Form form, final String rowId, final boolean value) {
        final var r = FormUtils.findRowById(form, null, rowId);
        if (r != null) {
            // if a row is selected (value == true) and we have a single-select table
            // we need to set all other rows to non-selected
            if (value) {
                final var def = form.getSd().findElementByKey(r.getRight().getKey());
                if (((TableElementDefinition) def).getSelect() == TableSelectType.Single) {
                    ((Table) r.getRight().getValue()).getData().values().forEach(it -> {
                        if (it.isSelected()) {
                            form.getJournal().updateSelected(it.getRowId(), false);
                            it.setSelected(false);
                        }
                    });
                }
            }
// TODO(ML): Commented out, probably not necessary with new table approach
//            form.getJournal().updateSelected(rowId, value);
            r.getLeft().setSelected(value);
        }
    }

    /**
     * Get all row IDs of a table identified by rowId and key that are selected.
     *
     * @param form
     * @param rowId
     * @param key
     * @return
     */
    public static List<String> getSelected(final Form form, final String rowId, final String key) {
        final var result = new ArrayList<String>();
        final var element = FormUtils.findElementByRowAndKey(form, rowId, key);

        if (element != null && element.getValue() instanceof Table c) {
            c.getData().values().forEach(it -> {
                if (it.isSelected()) {
                    result.add(it.getRowId());
                }
            });
        }

        return result;
    }

    /**
     * Iterate over all row IDs of a table identified by rowId and key.
     *
     * @param form
     * @param rowId
     * @param key
     * @param visitor
     * @param ctx
     */
    public static void forEach(final Form form, final String rowId, final String key, final RowVisitor visitor,
                               final Context<? extends AccessClass> ctx) {
        final var element = FormUtils.findElementByRowAndKey(form, rowId, key);
        if (element != null && element.getValue() instanceof Table c) {
            for (var id : c.getRows()) {
                if (!visitor.visit(id, ctx)) {
                    break;
                }
            }
        }
    }

    /**
     * @param form
     * @param rowId
     * @param key
     * @param predicate
     */
    public static void filter(final Form form, final String rowId, final String key, final Predicate<String> predicate) {
        final var element = FormUtils.findElementByRowAndKey(form, rowId, key);
        if (element == null) {
            log.error("Cannot find element for key '{}' in row '{}'!", key, rowId);
            return;
        }

        if (element.getValue() instanceof Table table) {
            var found = false;
            for (var row : table.getRows()) {
                if (!predicate.test(row)) {
                    table.deleteRow(row);
                    found = true;
                }
            }
            if (found) {
                form.getJournal().updateValue(element, rowId, table, false);
            }
        }
    }

    /**
     * Read elements from json node and return the element identified by rowId and key.
     *
     * @param sd   Scenario definition to be used
     * @param node JSON node to be read
     * @return ElementRow instance
     */
    public static ElementRow readElementRow(ScenarioDefinition sd, JsonNode node) {
        final var row = new ElementRow();

        row.setRowId(node.get(NM_ROW_ID).asText());
        row.setSelected(node.get(NM_SELECTED).asBoolean());

        node.get(NM_ELEMENTS).iterator().forEachRemaining(it -> {
            final var element = new Element();
            element.setKey(it.get(NM_KEY).asText());
            element.setName(it.get(NM_NAME).asText());
            element.setVisible(it.get(NM_VISIBLE).asBoolean());
            element.setEditable(it.get(NM_EDITABLE).asBoolean());
            element.setRequired(it.get(NM_REQUIRED).asBoolean());

            final var ed = sd.findElementByKey(element.getKey());
            element.setValue(readElementData(sd, ed, it));

            if (it.get(NM_MESSAGE) != null) {
                final var msgNode = it.get(NM_MESSAGE);
                final var msg = new Message();
                msg.setSeverity(Message.mapSeverity(msgNode.get(NM_SEVERITY).asText()));
                msg.setKey(msgNode.get(NM_KEY).asText());
                element.setMessage(msg);
            }

            row.getElements().put(element.getKey(), element);
        });

        return row;
    }

    /**
     * @param sd
     * @param ed
     * @param node
     * @return
     */
    public static Object readElementData(final ScenarioDefinition sd, final ElementDefinition ed, final JsonNode node) {

        final var dt = ElementDefinition.getDataTypeClass(ed);
        final var v = node.get(NM_VALUE);

        if (v == null || v.isNull()) {
            return null;
        }

        if (dt == Integer.class) {
            return v.asInt();
        } else if (dt == LocalDate.class) {
            return LocalDate.parse(v.asText());
        } else if (dt == LocalDateTime.class) {
            return LocalDateTime.parse(v.asText());
        } else if (dt == LocalTime.class) {
            return LocalTime.parse(v.asText());
        } else if (dt == BigDecimal.class) {
            return new BigDecimal(v.asText());
        } else if (dt == String.class) {
            return v.asText();
        } else if (dt == Boolean.class) {
            return v.asBoolean();
        } else if (dt == Table.class) {
            final var table = new Table((TableElementDefinition) ed);
            Optional.ofNullable(v.get("sf")).ifPresent(it -> table.setSortField(it.asText()));
            Optional.ofNullable(v.get("sd")).ifPresent(it -> table.setSortOrder(
                    Table.fromCode(it.asText())));

            if (v.has("r")) {
                v.get("r").iterator().forEachRemaining(child -> table.getRows().add(child.asText()));
            }
            if (v.has("d")) {
                v.get("d").fields().forEachRemaining(entry ->
                        table.getData().put(entry.getKey(), readElementRow(sd, entry.getValue())));
            }
            table.setPos(v.get("p").asInt(0));
            table.setPageSize(v.get("ps").asInt(10));
            return table;
        } else if (dt == Attachments.class) {
            final var atts = new Attachments();
            v.iterator().forEachRemaining(child -> {
                var att = new Attachment();
                att.setId(child.get(NM_ID).asText());
                att.setPos(child.get(NM_POS).asInt());
                att.setFileName(child.get(NM_NAME).asText());
                att.setContentType(child.get(NM_CONTENT_TYPE).asText());
                att.setSize(child.get(NM_SIZE).asLong());
                att.setRef(child.get(NM_REF).asText());
                var text = child.get(NM_CATEGORY);
                if (text != null && !text.isNull()) {
                    att.setCategory(text.asText());
                }
                text = child.get(NM_DESCRIPTION);
                if (text != null && !text.isNull()) {
                    att.setDescription(text.asText());
                }
                atts.add(att);
            });
            return atts;
        } else if (dt == DateRange.class) {
            final var d = new DateRange();
            d.setFrom(LocalDate.parse(v.get(NM_DATERANGE_FROM).asText()));
            d.setTo(LocalDate.parse(v.get(NM_DATERANGE_TO).asText()));
            return d;
        } else if (dt == LinkData.class) {
            final var l = new LinkData();
            l.setText(v.get(NM_LINK_TEXT).asText());
            l.setHRef(v.get(NM_LINK_HREF).asText());
            l.setTarget(v.get(NM_LINK_TARGET).asText());
            return l;
        }

        throw new BadRequestException("unhandled datatype " + dt.getName() + "in readElementData");
    }

    /**
     * @param node
     * @param clz
     * @param <T>
     * @return
     */
    public static <T> T readElementData(final JsonNode node, final Class<T> clz) {
        if (clz.equals(Boolean.class) || clz.equals(boolean.class)) {
            return (T) Boolean.valueOf(node.get(NM_VALUE).asBoolean());
        } else if (clz.equals(Integer.class)
                || clz.equals(int.class)) {
            return (T) Integer.valueOf(node.get(NM_VALUE).asInt());
        } else if (clz.equals(String.class)) {
            return (T) node.get(NM_VALUE).asText();
        }

        throw new BadRequestException("unkown class " + clz.getName().toString() + " in FormUtils.readElementData");
    }

    /**
     * @param sd
     * @param root
     * @param globalEditable
     * @param isInitial
     * @param ctx
     */
    public static void calculateVisualAttributes(final ScenarioDefinition sd, final ElementRow root,
                                                 final boolean globalEditable, boolean isInitial,
                                                 final Context<? extends AccessClass> ctx) {

        final var element = FormUtils.findElementByRowAndKey(root, ElementRow.ROOT, sd.getRootElementKey());
        calculateVisualAttributes(sd, root, element, ElementRow.ROOT, globalEditable, isInitial, ctx);
    }

    /**
     * @param elements
     * @param ed
     * @param expressions
     * @param ctx
     */
    private static void createAndAddElement(ElementMap elements, ElementDefinition ed,
                                            final Collection<Pair<ElementDefinition, Element>> expressions,
                                            final Context<? extends AccessClass> ctx) {

        final var element = createElement(ed, expressions, ctx);
        // add element to the element map provided by parent call
        elements.put(ed.getKey(), element);

        // iterate over all child elements but avoid collection types (because these are handled differently)
        ed.getChildren().stream()
                .filter(it1 -> !ed.isCollection() || it1 != ed.getElements())
                .forEach(it1 -> it1.forEach(it2 -> createAndAddElement(elements, it2, expressions, ctx)));
    }

    /**
     * @param expressions
     * @param ctx
     */
    private static void evalDefaultValueExpressions(final Collection<Pair<ElementDefinition, Element>> expressions,
                                                    final Context<? extends AccessClass> ctx) {
        expressions.forEach(it -> {
            try {
                final Object value = it.getLeft().getDefaultValueEvaluator().eval(ctx, true,
                        it.getValue().getValue());
                it.getRight().setValue(value);
            } catch (Exception e) {
                log.error("Error during expression evaluation for element '" + it.getLeft().getName() + "'", e);
            }
        });
    }

    /**
     * @param sd
     * @param root
     * @param element
     * @param rowId
     * @param globalEditable
     * @param isInitial
     * @param ctx
     */
    private static void calculateVisualAttributes(final ScenarioDefinition sd,
                                                  final ElementRow root,
                                                  final Element element,
                                                  final String rowId,
                                                  final boolean globalEditable,
                                                  final boolean isInitial,
                                                  final Context<? extends AccessClass> ctx) {

        final var ed = sd.findElementByKey(element.getKey());

        if (isInitial) {
            element.setVisible(!ed.isVisualDependent() && ed.getVisibleEvaluator().eval(ctx, isInitial,
                    element.isVisible()));
            element.setEditable(globalEditable && ed.getEditableEvaluator().eval(ctx, isInitial, element.isEditable()));
            element.setRequired(ed.getRequiredEvaluator().eval(ctx, isInitial, element.isRequired()));
        }

        // handling of children within a collections, meaning rows
        if (element.getValue() instanceof Table table) {
            table.getRows().forEach(id -> ed.getElements().forEach(childDef -> {
                final var child = table.getData().get(id).getElements().get(childDef.getKey());
                calculateVisualAttributes(sd, root, child, id, globalEditable, isInitial, ctx);
            }));
        }

        // TODO(ML) Check the inner findElementByRowAndKey if we can avoid this call and direct access the element?!
        // handling of "structural" children but within the same row
        ed.getChildren().stream()
                .filter(it1 -> !ed.isCollection() || it1 != ed.getElements())
                .forEach(it1 -> it1.forEach(it2 -> {
                    final var child = FormUtils.findElementByRowAndKey(root, rowId, it2.getKey());
                    calculateVisualAttributes(sd, root, child, rowId, globalEditable, isInitial, ctx);
                    if (ed.isVisualDependent()) {
                        element.setVisible(element.isVisible() || child.isVisible());
                    }
                }));
    }

    /**
     * @param root
     * @param rowId
     * @param key
     * @return
     */
    public static Element findElementByRowAndKey(final ElementRow root, final String rowId, final String key) {
        final var row = findRowById(root, null, rowId);
        if (row == null) {
            log.error("Cannot find row for rowId '" + rowId + "'");
            return null;
        }
        return row.getLeft().getElements().get(key);
    }

    /**
     * @param root
     * @param parent
     * @param rowId
     * @return
     */
    public static Pair<ElementRow, Element> findRowById(final ElementRow root, final Element parent, final String rowId) {
        // check if we already have the right element
        if (StringUtils.equals(root.getRowId(), rowId)) {
            return new ImmutablePair<>(root, parent);
        }

        for (var element : root.getElements().values()) {
            if (element.getValue() instanceof Table table) {
                // quick check -> is the row in the data attribute as key?
                final var row = table.getData().get(rowId);
                if (row != null) {
                    return new ImmutablePair<>(row, element);
                }

                // if it's not directly in the data map, then it might be below as a table in one of the rows...
                for (var child : table.getData().values()) {
                    var elementRow = FormUtils.findRowById(child, element, rowId);
                    if (elementRow != null) {
                        return elementRow;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Find the parent element of an element identified by rowId and key.
     *
     * @param rowId
     * @param key
     * @param row
     * @param parentRowId
     * @param parentElement
     * @return
     */
    public static Pair<String, Element> findParent(final String rowId, final String key,
                                                   final ElementRow row, final String parentRowId,
                                                   final Element parentElement) {
        if (row.getRowId().equals(rowId) && row.getElements().containsKey(key)) {
            // found it, return parent rowId and the e
            return new ImmutablePair<>(parentRowId, parentElement);
        }

        // not directly found, we go through all children of parent-element and check if we find it there
        for (var element : row.getElements().values()) {
            if (element.getValue() instanceof Table table) {
                for (var childRow : table.getData().values()) {
                    var result = FormUtils.findParent(rowId, key, childRow, row.getRowId(), element);
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        // not found anywhere
        return null;
    }


    /**
     * @param ed
     * @param expressions
     * @param ctx
     * @return
     */
    private static Element createElement(final ElementDefinition ed,
                                         final Collection<Pair<ElementDefinition, Element>> expressions,
                                         final Context<? extends AccessClass> ctx) {
        final var element = new Element();
        element.setKey(ed.getKey());
        element.setName(ed.getName());
        element.setVisible(false);
        element.setEditable(false);
        element.setRequired(false);

        // static initialization of the value
        if (ElementDefinition.isExpression(ed.getDefaultValue())) {
            expressions.add(new ImmutablePair<>(ed, element));
        } else {
            element.setValue(ed.getDefaultValueEvaluator().eval(ctx, true, element.getValue()));
        }

        return element;
    }
}
