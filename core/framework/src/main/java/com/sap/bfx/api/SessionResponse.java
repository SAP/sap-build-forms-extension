package com.sap.bfx.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.callback.CallbackResult;
import com.sap.bfx.callback.CallbackService;
import com.sap.bfx.definition.*;
import com.sap.bfx.session.*;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.tomcat.util.bcel.classfile.ElementValue;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.sap.bfx.definition.DefinitionNames.*;

/**
 * SessionResponse is a data class that represents the response of a session in the application.
 * It contains information about the session, including its ID, locale, values, scenario definition,
 * value helps, backend journal, form, callback service, page title, header title, and messages.
 */
@Data
class SessionResponse {
    private String sessionId;
    private Locale locale;
    private ElementMap values;
    private ScenarioDefinition def;
    private Map<String, Long> valueHelpVersions;
    private BackendJournal journal;
    private Form form;
    private CallbackService callbackService;
    private String pageTitle;
    private String headerTitle;
    private Collection<CallbackResult.Message> messages;
    private Map<String, Map<String, String>> dynamicValuehelps;

    /**
     * Constructs a new SessionResponse with the specified session ID, callback result, form, and backend journal.
     *
     * @param sessionId the unique identifier of the session
     * @param result    the CallbackResult containing information about the session's state
     * @param form      the Form associated with the session
     * @param journal   the BackendJournal containing changes made during the session
     */
    public SessionResponse(final String sessionId, final CallbackResult result, final Form form,
                           final BackendJournal journal) {
        this.sessionId = sessionId;
        this.setResult(result);
        this.journal = journal;
        this.form = form;
    }

    /**
     * Sets the result of the session response based on the provided CallbackResult.
     * If the result is null, no changes are made to the session response.
     *
     * @param result the CallbackResult containing information about the session's state
     */
    public final void setResult(final CallbackResult result) {
        if (result == null) {
            return;
        }

        pageTitle = result.getPageTitle();
        headerTitle = result.getHeaderTitle();
        messages = result.getMessages();
        dynamicValuehelps = result.getValueHelps();
    }

    /**
     * SessionResponseSerializer is a custom serializer for the SessionResponse class.
     * It defines how to serialize the SessionResponse object into JSON format.
     */
    static class SessionResponseSerializer extends StdSerializer<SessionResponse> {
        final static DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        final static DateTimeFormatter D_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        /**
         * Constructs a new SessionResponseSerializer for the SessionResponse class.
         */
        SessionResponseSerializer() {
            super(SessionResponse.class);
        }

        @Override
        public void serialize(SessionResponse value, JsonGenerator gen, SerializerProvider provider)
                throws IOException {

            gen.writeStartObject();

            gen.writeStringField("id", value.sessionId);
            if (value.locale != null) {
                gen.writeStringField("locale", value.locale.toString());
            }
            if (value.valueHelpVersions != null) {
                gen.writeObjectField("vhs", value.valueHelpVersions);
            }
            if (value.def != null) {
                gen.writeFieldName("def");
                this.serializeDef(value.def, value.locale, value.callbackService, gen, provider);
            }
            if (value.values != null) {
                gen.writeFieldName("values");
                this.serializeElementMap(value.values, gen, provider);
            }
            if (value.journal != null) {
                this.serializeJournal(value.form, value.journal, gen, provider);
            }
            if (value.pageTitle != null) {
                gen.writeStringField("pageTitle", value.pageTitle);
            }
            if (value.headerTitle != null) {
                gen.writeStringField("headerTitle", value.headerTitle);
            }
            if (value.messages != null && !value.messages.isEmpty()) {
                gen.writeObjectField("msg", value.messages);
            }
            if (value.dynamicValuehelps != null && !value.dynamicValuehelps.isEmpty()) {
                gen.writeObjectField("dvhs", value.dynamicValuehelps);
            }

            gen.writeEndObject();
        }

        /**
         * Serializes the ScenarioDefinition object into JSON format.
         *
         * @param sd              the ScenarioDefinition to be serialized
         * @param locale          the locale for which to serialize the texts
         * @param callbackService the CallbackService used to find event handlers
         * @param gen             the JsonGenerator used for writing JSON content
         * @param provider        the SerializerProvider used for serialization
         * @throws IOException if an I/O error occurs during serialization
         */
        private void serializeDef(final ScenarioDefinition sd, final Locale locale,
                                  final CallbackService callbackService, JsonGenerator gen, SerializerProvider provider)
                throws IOException {

            gen.writeStartObject();
            // texts
            gen.writeObjectField("texts", sd.getTexts().get(locale));
            // root
            gen.writeStringField("root", sd.getRootElementName());
            // root-key
            gen.writeStringField("rootKey", sd.getRootElementKey());
            // elements
            this.serializeElementsDef("elements", sd.getElements(),
                    callbackService.findEventHandlersByVersion(sd.getVersion()), gen, provider, new ArrayList<>());

            gen.writeEndObject();
        }

        /**
         * Serializes a list of ElementDefinition objects into JSON format.
         *
         * @param fieldName        the name of the field to be used in the JSON output
         * @param elements         the list of ElementDefinition objects to be serialized
         * @param eventHandlersMap a map of event handlers associated with the elements
         * @param gen              the JsonGenerator used for writing JSON content
         * @param provider         the SerializerProvider used for serialization
         * @param parents          a list of parent ElementDefinition objects for context
         * @throws IOException if an I/O error occurs during serialization
         */
        private void serializeElementsDef(final String fieldName, final List<ElementDefinition> elements,
                                          final Map<String, CallbackService.EventHandlerInfo> eventHandlersMap,
                                          JsonGenerator gen, SerializerProvider provider,
                                          ArrayList<ElementDefinition> parents) throws IOException {

            if (!elements.isEmpty()) {
                gen.writeFieldName(fieldName);
                gen.writeStartArray();

                for (var it : elements) {
                    parents.add(it);
                    this.serializeElementDef(it, eventHandlersMap, gen, provider, parents);
                }

                gen.writeEndArray();
            }
        }

        /**
         * Serializes an ElementDefinition object into JSON format.
         *
         * @param element          the ElementDefinition to be serialized
         * @param eventHandlersMap a map of event handlers associated with the elements
         * @param gen              the JsonGenerator used for writing JSON content
         * @param provider         the SerializerProvider used for serialization
         * @param parentElements   a list of parent ElementDefinition objects for context
         * @throws IOException if an I/O error occurs during serialization
         */
        private void serializeElementDef(final ElementDefinition element,
                                         final Map<String, CallbackService.EventHandlerInfo> eventHandlersMap,
                                         JsonGenerator gen, SerializerProvider provider,
                                         ArrayList<ElementDefinition> parentElements) throws IOException {
            gen.writeStartObject();
            // Element data itself
            gen.writeStringField("id", element.getName());
            gen.writeStringField("key", element.getKey());
            gen.writeStringField("uiElement", element.getType().getIdentifier());
            if (element.getDataType() != null && element.getDataType() != DataType.Auto) {
                gen.writeStringField("dataType", element.getDataType().getIdentifier());
            }
            if (StringUtils.isNotBlank(element.getCol())) {
                gen.writeStringField("col", element.getCol());
            }
            if (StringUtils.isNotBlank(element.getCss())) {
                gen.writeStringField("css", element.getCss());
            }
            gen.writeBooleanField("showLabel", element.isShowLabel());
            if (element.isShowHelp()) {
                gen.writeBooleanField("showHelp", true);
            }
            if (element.isLineBreak()) {
                gen.writeBooleanField("lineBreak", true);
            }

            boolean isRootWizard = parentElements.stream().anyMatch(e -> e.getType() == UIElementType.Wizard);
            boolean isChildOfTable = parentElements.stream().anyMatch(e -> e.getType() == UIElementType.Table);

            if (isRootWizard && element.getWizardFormatOptions() != null) {
                gen.writeObjectFieldStart(NM_WIZARD_FORMAT_OPTIONS);
                gen.writeBooleanField(NM_SKIP_IN_SUMMARY, element.getWizardFormatOptions().isSkipInSummary());
                gen.writeBooleanField(NM_SKIP_IN_FORM, element.getWizardFormatOptions().isSkipInForm());
                gen.writeEndObject();

            }

            if ((isChildOfTable || element.getType() == UIElementType.Table) && element.getColumnOptions() != null) {
                gen.writeObjectFieldStart(NM_COLUMN_OPTIONS);
                gen.writeStringField(NM_MIN_COLUMN_WIDTH, element.getColumnOptions().getMinColumnWidth());
                gen.writeStringField(NM_MAX_COLUMN_WIDTH, element.getColumnOptions().getMaxColumnWidth());
                gen.writeEndObject();
            }

            if (element instanceof HasValueHelp) {
                if (((HasValueHelp) element).getValueHelp() != null) {
                    gen.writeObjectFieldStart("vh");
                    gen.writeStringField(NM_NAME, ((HasValueHelp) element).getValueHelp().getName());
                    gen.writeBooleanField(NM_VALIDATE, ((HasValueHelp) element).getValueHelp().isValidate());
                    gen.writeBooleanField(NM_EMPTY_SELECTION,
                            ((HasValueHelp) element).getValueHelp().isEmptySelection());
                    gen.writeStringField(NM_DISPLAY_FORMAT, ((HasValueHelp) element).getValueHelp().getDisplayFormat());
                    gen.writeEndObject();
                }
            }
            if (element.isShowAsColumn()) {
                gen.writeBooleanField("showAsColumn", true);
            }
            // event handlers
            if (eventHandlersMap != null && eventHandlersMap.containsKey(element.getKey())) {
                var events = eventHandlersMap.get(element.getKey()).getHandlers().stream()
                                             .map(it -> it.getType().getIdentifier()).toArray(String[]::new);
                gen.writeFieldName("events");
                gen.writeArray(events, 0, events.length);
            }
            // render fields depending on dedicated types
            switch (element.getType()) {
                case Alert:
                    if (((AlertElementDefinition) element).getDesign() != null) {
                        gen.writeStringField(NM_DESIGN, ((AlertElementDefinition) element).getDesign().getIdentifier());
                    }
                    if (StringUtils.isNotBlank(((AlertElementDefinition) element).getIcon())) {
                        gen.writeStringField(NM_ICON, ((AlertElementDefinition) element).getIcon());
                    }
                    break;
                case Attachment:
                    if (((AttachmentElementDefinition) element).getCardinality() != null) {
                        gen.writeStringField(NM_TYPE,
                                ((AttachmentElementDefinition) element).getCardinality().getIdentifier());
                    }
                    if (((AttachmentElementDefinition) element).getSelect() != null) {
                        gen.writeStringField(NM_SELECT,
                                ((AttachmentElementDefinition) element).getSelect().getIdentifier());
                    }
                    if (((AttachmentElementDefinition) element).getValueHelp() != null) {
                        gen.writeObjectFieldStart("vh");
                        gen.writeStringField(NM_NAME, ((AttachmentElementDefinition) element).getValueHelp().getName());
                        gen.writeBooleanField(NM_VALIDATE,
                                ((AttachmentElementDefinition) element).getValueHelp().isValidate());
                        gen.writeBooleanField(NM_EMPTY_SELECTION,
                                ((AttachmentElementDefinition) element).getValueHelp().isEmptySelection());
                        gen.writeStringField(NM_DISPLAY_FORMAT,
                                ((AttachmentElementDefinition) element).getValueHelp().getDisplayFormat());
                        gen.writeEndObject();
                    }
                    if (StringUtils.isNotBlank(((AttachmentElementDefinition) element).getFileTypes())) {
                        gen.writeStringField(NM_FILE_TYPES, ((AttachmentElementDefinition) element).getFileTypes());
                    }
                    if (((AttachmentElementDefinition) element).getDesign() != null) {
                        gen.writeStringField(NM_DESIGN,
                                ((AttachmentElementDefinition) element).getDesign().getIdentifier());
                    }
                    gen.writeArrayFieldStart(NM_CATEGORIES);
                    for (CategoryOptions categoryOptions : ((AttachmentElementDefinition) element).getCategories()) {
                        gen.writeStartObject();
                        gen.writeStringField(NM_LABEL, categoryOptions.getLabel());
                        gen.writeObjectFieldStart(NM_HV_OPT);
                        gen.writeStringField(NM_NAME, categoryOptions.getHvOpt().getName());
                        gen.writeBooleanField(NM_VALIDATE, categoryOptions.getHvOpt().isValidate());
                        gen.writeBooleanField(NM_EMPTY_SELECTION, categoryOptions.getHvOpt().isEmptySelection());
                        gen.writeStringField(NM_DISPLAY_FORMAT, categoryOptions.getHvOpt().getDisplayFormat());
                        gen.writeEndObject();
                        gen.writeEndObject();
                    }
                    gen.writeEndArray();
                    gen.writeBooleanField(NM_HAS_DESCRIPTION,
                            ((AttachmentElementDefinition) element).isHasDescription());
                    break;
                case Button:
                    if (((ButtonElementDefinition) element).getDesign() != null) {
                        gen.writeStringField(NM_DESIGN,
                                ((ButtonElementDefinition) element).getDesign().getIdentifier());
                    }
                    if (StringUtils.isNoneBlank(((ButtonElementDefinition) element).getIcon())) {
                        gen.writeStringField(NM_ICON, ((ButtonElementDefinition) element).getIcon());
                    }
                    if (StringUtils.isNotBlank(((ButtonElementDefinition) element).getLinkHRef())) {
                        gen.writeStringField("linkHRef", ((ButtonElementDefinition) element).getLinkHRef());
                    }
                    if (StringUtils.isNotBlank(((ButtonElementDefinition) element).getTooltip())) {
                        gen.writeStringField(NM_TOOLTIP, ((ButtonElementDefinition) element).getTooltip());
                    }
                    if (StringUtils.isNotBlank(((ButtonElementDefinition) element).getShortcut())) {
                        gen.writeStringField(NM_SHORTCUT, ((ButtonElementDefinition) element).getShortcut());
                    }
                    break;
                case Icon:
                    if (StringUtils.isNotBlank(((IconElementDefinition) element).getIcon())) {
                        gen.writeStringField(NM_ICON, ((IconElementDefinition) element).getIcon());
                    }
                    if (StringUtils.isNotBlank(((IconElementDefinition) element).getTooltip())) {
                        gen.writeStringField(NM_TOOLTIP, ((IconElementDefinition) element).getTooltip());
                    }
                    break;
                case Dialog:
                    if (((DialogElementDefinition) element).getSize() != null) {
                        gen.writeObjectFieldStart(NM_SIZE);
                        gen.writeStringField(NM_HEIGHT, ((DialogElementDefinition) element).getSize().getHeight());
                        gen.writeStringField(NM_WIDTH, ((DialogElementDefinition) element).getSize().getWidth());
                        gen.writeEndObject();
                    }
                    if (((DialogElementDefinition) element).getFooter() != null) {
                        gen.writeFieldName("footer");
                        ArrayList<ElementDefinition> temp = new ArrayList<>(parentElements);
                        temp.add(element);
                        temp.add(((DialogElementDefinition) element).getFooter());
                        this.serializeElementDef(((DialogElementDefinition) element).getFooter(), eventHandlersMap, gen,
                                provider, temp);
                    }
                    break;
                case DocForm:
                    if (((DocFormElementDefinition) element).getFooter() != null) {
                        gen.writeFieldName("footer");
                        ArrayList<ElementDefinition> temp = new ArrayList<>(parentElements);
                        temp.add(element);
                        temp.add(((DocFormElementDefinition) element).getFooter());
                        this.serializeElementDef(((DocFormElementDefinition) element).getFooter(), eventHandlersMap,
                                gen, provider, temp);
                    }
                    if (((DocFormElementDefinition) element).getHeaderSegment() != null) {
                        gen.writeFieldName("header");
                        ArrayList<ElementDefinition> temp = new ArrayList<>(parentElements);
                        temp.add(element);
                        temp.add(((DocFormElementDefinition) element).getHeaderSegment());
                        this.serializeElementDef(((DocFormElementDefinition) element).getHeaderSegment(),
                                eventHandlersMap, gen, provider, temp);
                    }
                    break;

                case Form:
                    if (((FormElementDefinition) element).getFooter() != null) {
                        gen.writeFieldName("footer");
                        ArrayList<ElementDefinition> temp = new ArrayList<>(parentElements);
                        temp.add(element);
                        temp.add(((FormElementDefinition) element).getFooter());
                        this.serializeElementDef(((FormElementDefinition) element).getFooter(), eventHandlersMap, gen,
                                provider, temp);
                    }
                    if (((FormElementDefinition) element).getHeaderSegment() != null) {
                        gen.writeFieldName("header");
                        ArrayList<ElementDefinition> temp = new ArrayList<>(parentElements);
                        temp.add(element);
                        temp.add(((FormElementDefinition) element).getHeaderSegment());
                        this.serializeElementDef(((FormElementDefinition) element).getHeaderSegment(), eventHandlersMap,
                                gen, provider, temp);
                    }
                    break;
                case Image:
                    if (((ImageElementDefinition) element).getSize() != null) {
                        gen.writeObjectFieldStart(NM_SIZE);
                        gen.writeStringField(NM_HEIGHT, ((ImageElementDefinition) element).getSize().getHeight());
                        gen.writeStringField(NM_WIDTH, ((ImageElementDefinition) element).getSize().getWidth());
                        gen.writeEndObject();
                    }
                    break;
                case Input:
                    if (((InputElementDefinition) element).getInputType() != null) {
                        gen.writeStringField(NM_INPUT_TYPE,
                                ((InputElementDefinition) element).getInputType().getIdentifier());
                    }
                    break;
                case SearchHelp:
                    if (((SearchHelpElementDefinition) element).getSize() != null) {
                        gen.writeObjectFieldStart(NM_SIZE);
                        gen.writeStringField(NM_HEIGHT, ((SearchHelpElementDefinition) element).getSize().getHeight());
                        gen.writeStringField(NM_WIDTH, ((SearchHelpElementDefinition) element).getSize().getWidth());
                        gen.writeEndObject();
                    }
                    break;
                case Table:
                    gen.writeStringField("type", ((TableElementDefinition) element).getStyle().getIdentifier());
                    gen.writeStringField("select", ((TableElementDefinition) element).getSelect().getIdentifier());
                    if (((TableElementDefinition) element).getToolbar() != null) {
                        gen.writeFieldName("toolbar");
                        ArrayList<ElementDefinition> temp = new ArrayList<>(parentElements);
                        temp.add(element);
                        temp.add(((TableElementDefinition) element).getToolbar());
                        this.serializeElementDef(((TableElementDefinition) element).getToolbar(), eventHandlersMap, gen,
                                provider, temp);
                    }
                    break;
                case Toolbar:
                    if (((ToolbarElementDefinition) element).getRightElements() != null) {
                        ArrayList<ElementDefinition> temp = new ArrayList<>(parentElements);
                        temp.add(element);
                        this.serializeElementsDef("rightElements",
                                ((ToolbarElementDefinition) element).getRightElements(), eventHandlersMap, gen,
                                provider, temp);
                    }
                    if (((ToolbarElementDefinition) element).getLeftElements() != null) {
                        ArrayList<ElementDefinition> temp = new ArrayList<>(parentElements);
                        temp.add(element);
                        this.serializeElementsDef("leftElements",
                                ((ToolbarElementDefinition) element).getLeftElements(), eventHandlersMap, gen, provider,
                                temp);
                    }
                    break;
                case Wizard:
                    if (((WizardElementDefinition) element).getFooter() != null) {
                        gen.writeFieldName("footer");
                        ArrayList<ElementDefinition> temp = new ArrayList<>(parentElements);
                        temp.add(((WizardElementDefinition) element).getFooter());
                        this.serializeElementDef(((WizardElementDefinition) element).getFooter(), eventHandlersMap, gen,
                                provider, temp);
                    }

                    // TODO(ML) Check all other element-types and add according handling of additional fields here!!
            }
            // child elements
            ArrayList<ElementDefinition> temp = new ArrayList<>(parentElements);
            temp.add(element);
            this.serializeElementsDef("elements", element.getElements(), eventHandlersMap, gen, provider, temp);
            // that all, close the object
            gen.writeEndObject();
        }

        /**
         * Serializes an ElementMap object into JSON format.
         *
         * @param values   the ElementMap to be serialized
         * @param gen      the JsonGenerator used for writing JSON content
         * @param provider the SerializerProvider used for serialization
         * @throws IOException if an I/O error occurs during serialization
         */
        private void serializeElementMap(final ElementMap values, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            gen.writeStartObject();

            for (var rowId : values.keySet()) {
                final var value = values.get(rowId);

                if (!value.isVisible()) {
                    continue;
                }
                gen.writeFieldName(value.getKey());
                gen.writeStartObject();
                gen.writeStringField("key", value.getKey());
                gen.writeStringField("nm", value.getName());
                if (value.getMessage() != null) {
                    gen.writeObjectField("msg", value.getMessage());
                }
                gen.writeBooleanField("ed", value.isEditable());
                gen.writeBooleanField("rq", value.isRequired());
                gen.writeBooleanField("vi", value.isVisible());
                gen.writeFieldName("va");
                this.serializeValue(rowId, value.getKey(), value.getValue(), null, gen, provider);

                gen.writeEndObject();
            }

            gen.writeEndObject();
        }

        /**
         * Serializes an ElementRow object into JSON format.
         *
         * @param row      the ElementRow to be serialized
         * @param gen      the JsonGenerator used for writing JSON content
         * @param provider the SerializerProvider used for serialization
         * @throws IOException if an I/O error occurs during serialization
         */
        private void serializeElementRow(final ElementRow row, JsonGenerator gen, SerializerProvider provider)
                throws IOException {

            gen.writeStartObject();
            gen.writeStringField("id", row.getRowId());
            gen.writeBooleanField("sel", row.isSelected());
            gen.writeFieldName("values");
            this.serializeElementMap(row.getElements(), gen, provider);
            gen.writeEndObject();
        }

        /**
         * Serializes a value based on its type into JSON format.
         *
         * @param rowId    the ID of the row containing the value
         * @param key      the key associated with the value
         * @param value    the value to be serialized
         * @param journal  the BackendJournal containing changes made during the session
         * @param gen      the JsonGenerator used for writing JSON content
         * @param provider the SerializerProvider used for serialization
         * @throws IOException if an I/O error occurs during serialization
         */
        private void serializeValue(final String rowId, final String key, final Object value, BackendJournal journal,
                                    JsonGenerator gen, SerializerProvider provider) throws IOException {
            if (value instanceof LocalDate || value instanceof LocalTime) {
                gen.writeString(value.toString());
            } else if (value instanceof LocalDateTime) {
                gen.writeString(((LocalDateTime) value).format(DT_FORMATTER));
            } else if (value instanceof Table table) {
                gen.writeStartObject();
                gen.writeNumberField("p", table.getPos());
                gen.writeNumberField("ps", table.getPageSize());
                gen.writeNumberField("s", table.getRows().size());

                if (StringUtils.isNotBlank(table.getSortField())) {
                    gen.writeStringField("sf", table.getSortField());
                    gen.writeStringField("sd", Table.toCode(table.getSortOrder()));
                }

                gen.writeArrayFieldStart("r");
                for (var i = table.getPos(); i < table.getPos() + table.getPageSize(); i++) {
                    if (i < table.getRows().size()) {
                        gen.writeString(table.getRows().get(i));
                    }
                }
                gen.writeEndArray();

                gen.writeObjectFieldStart("d");
                for (var it : (journal != null) ? journal.getNecessaryRows(rowId, key, table) :
                        table.getCurrentRows()) {
                    gen.writeFieldName(it);
                    serializeElementRow(table.getData().get(it), gen, provider);
                }
                gen.writeEndObject();

                gen.writeEndObject();
            } else if (value instanceof ElementMap) {
                this.serializeElementMap((ElementMap) value, gen, provider);
            } else if (value instanceof Pair) {
                gen.writeStartObject();
                gen.writeStringField("value", ((Pair<String, ElementValue>) value).getLeft());
                gen.writeFieldName("values");
                this.serializeElementMap(((Pair<String, ElementMap>) value).getRight(), gen, provider);
                gen.writeEndObject();
            } else if (value instanceof DateRange) {
                gen.writeStartObject();
                gen.writeStringField(FormUtils.NM_DATERANGE_FROM, ((DateRange) value).getFrom().format(D_FORMATTER));
                gen.writeStringField(FormUtils.NM_DATERANGE_TO, ((DateRange) value).getTo().format(D_FORMATTER));
                gen.writeEndObject();
            } else if (value instanceof LinkData) {
                gen.writeStartObject();
                gen.writeStringField(FormUtils.NM_LINK_TEXT, ((LinkData) value).getText());
                gen.writeStringField(FormUtils.NM_LINK_HREF, ((LinkData) value).getHRef());
                gen.writeEndObject();
            } else if (value instanceof Attachments attachments) {
                gen.writeStartArray();
                for (var it : attachments) {
                    gen.writeStartObject();
                    gen.writeStringField(FormUtils.NM_ID, it.getId());
                    gen.writeNumberField(FormUtils.NM_POS, it.getPos());
                    gen.writeStringField(FormUtils.NM_NAME, it.getFileName());
                    gen.writeStringField(FormUtils.NM_CONTENT_TYPE, it.getContentType());
                    gen.writeNumberField(FormUtils.NM_SIZE, it.getSize());
                    if (StringUtils.isNotBlank(it.getCategory())) {
                        gen.writeStringField(FormUtils.NM_CATEGORY, it.getCategory());
                    }
                    if (StringUtils.isNotBlank(it.getDescription())) {
                        gen.writeStringField(FormUtils.NM_DESCRIPTION, it.getDescription());
                    }
                    gen.writeEndObject();
                }
                gen.writeEndArray();
            } else if (value instanceof MoneyAmount) {
                gen.writeStartObject();
                gen.writeStringField(FormUtils.NM_CURRENCY, ((MoneyAmount) value).getCurrency());
                gen.writeNumberField(FormUtils.NM_AMOUNT, ((MoneyAmount) value).getAmount());
                gen.writeEndObject();
            } else {
                gen.writeObject(value);
            }
        }

        /**
         * Serializes the BackendJournal object into JSON format.
         * It checks and validates the changes in the journal before writing them to the output.
         *
         * @param form     the Form associated with the session
         * @param journal  the BackendJournal containing changes made during the session
         * @param gen      the JsonGenerator used for writing JSON content
         * @param provider the SerializerProvider used for serialization
         * @throws IOException if an I/O error occurs during serialization
         */
        private void serializeJournal(final Form form, final BackendJournal journal, JsonGenerator gen,
                                      SerializerProvider provider) throws IOException {

            gen.writeObjectFieldStart("journal");

            // check/validate changes
            var repeat = true;
            while (repeat) {
                repeat = false;
                for (var rowId : new HashSet<>(journal.getChanges().keySet())) {
                    for (var key : new HashSet<>(journal.getChanges().get(rowId).keySet())) {
                        repeat = repeat || checkChangeNecessary(form, rowId, key, journal);
                    }
                }
            }

            // after validation that only correct changes are in the journal write them accordingly
            if (!journal.getChanges().isEmpty()) {
                for (var rowId : journal.getChanges().keySet()) {
                    // if a row has no changes, skip it
                    if (journal.getChanges().get(rowId).isEmpty()) {
                        continue;
                    }

                    // otherwise write the changes
                    gen.writeObjectFieldStart(rowId);

                    for (var key : journal.getChanges().get(rowId).keySet()) {
                        gen.writeObjectFieldStart(key);
                        var ce = journal.getChanges().get(rowId).get(key);
                        for (var prop : ce.getChanges().keySet()) {
                            gen.writeFieldName(prop.getKey());
                            serializeValue(rowId, key, ce.getChanges().get(prop), journal, gen, provider);
                        }
                        gen.writeEndObject();
                    }

                    gen.writeEndObject();
                }
            }

            gen.writeEndObject();
        }

        /**
         * Check if a change to an element is necessary to be reported to the frontend. Changes directly in root
         * will always be reported, changes of tables as well. Changes of elements in tables will not be reported,
         * but the table itself will be marked as changed.
         *
         * @param form    the form to be used
         * @param rowId   the rowId of the element to be checked
         * @param key     the key of the element to be checked
         * @param journal the BackendJournal containing changes made during the session
         * @return true if change needs to be reported, false if not
         */
        private boolean checkChangeNecessary(final Form form, final String rowId, final String key,
                                             final BackendJournal journal) {
            // changes directly in root will be accepted
            if (ElementRow.ROOT.equals(rowId)) {
                return false;
            }
            // changes of tables will also be accepted
            final var ed = journal.getSd().findElementByKey(key);
            if (ed.isCollection()) {
                return false;
            }
            // all other changes will not be reported to the frontend:
            // 1. first check if the given row is in the list of rows already at frontend, in this case we need
            // to remove it from there
            final var pos = journal.removeFromInitialRows(rowId);
            if (pos != null) {
                // if we removed this row we need to add a change of the table itself (because the row is on frontend
                // side as well and nees an update
                final var element = FormUtils.findElementByRowAndKey(form, pos.getRowId(), pos.getKey());
                journal.addUpdated(pos.getRowId(), element, ChangePropertyType.Value, element.getValue());
            }
            // 2. second remove the change for this element (this is necessary in any case
            journal.getChanges().get(rowId).remove(key);
            // return true -> this changes has been removed
            return true;
        }
    }
}
