package com.sap.bfx.definition;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.List;

import static com.sap.bfx.definition.DefinitionNames.*;

public class ScenarioDefinitionSerializer extends StdSerializer<ScenarioDefinition> {
    private boolean includeKeys = false;
    private boolean includeTexts = false;
    private boolean toFrontend = false;

    /**
     * Constructor
     *
     * @param includeKeys  flag if keys should be included
     * @param includeTexts flag if texts should be included
     * @param toFrontend   flag if the serialization is for frontend usage
     */
    public ScenarioDefinitionSerializer(final boolean includeKeys, final boolean includeTexts,
                                        final boolean toFrontend) {
        super(ScenarioDefinition.class);

        this.includeKeys = includeKeys;
        this.includeTexts = includeTexts;
        this.toFrontend = toFrontend;
    }

    @Override
    public void serialize(ScenarioDefinition sd, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField(NM_NAME, sd.getName());
        gen.writeNumberField(NM_VERSION, sd.getVersion());
        gen.writeBooleanField(NM_ACTIVE, sd.isActive());
        gen.writeStringField(NM_ROOT_ELEMENT, sd.getRootElementName());
        gen.writeStringField(NM_DEFAULT_LOCALE, sd.getDefaultLocale().toString());
        gen.writeStringField(NM_ACCESS_OBJECT, sd.getAccessObjectName());
        gen.writeStringField(NM_BASE_PACKAGE, sd.getBasePackage());
        if (includeKeys) {
            gen.writeStringField(NM_ROOT_ELEMENT_KEY, sd.getRootElementKey());
        }
        // write data of elements (this will be recursively)
        this.serializeElements(sd.getElements(), gen, provider);
        // write data of text elements (if configured)
        if (includeTexts) {
            gen.writeObjectField(NM_TEXTS, sd.getTexts());
        }
        gen.writeEndObject();
    }

    /**
     * Serializes the elements list. This results in an JSON element named elements and an array
     *
     * @param elementDefs list of element-definitions
     * @param gen         the JsonGenerator used for writing JSON content
     * @param provider    the SerializerProvider that can be used to access serializers for serializing objects
     * @throws IOException if an io-exception occurs during serialization
     */
    private void serializeElements(List<ElementDefinition> elementDefs, JsonGenerator gen, SerializerProvider provider)
            throws IOException {
        gen.writeFieldName(NM_ELEMENTS);
        gen.writeStartArray();
        for (var ed : elementDefs) {
            this.serializeElement(ed, gen, provider);
        }
        gen.writeEndArray();
    }

    /**
     * Serializes a single element definition into JSON format. This method handles the serialization of various
     * properties of the element definition, including its name, type, data type, default value, visibility, and
     * other attributes. It also handles specific serialization logic based on the type of the element definition.
     *
     * @param ed       the ElementDefinition object to be serialized
     * @param gen      the JsonGenerator used for writing JSON content
     * @param provider the SerializerProvider that can be used to access serializers for serializing objects
     * @throws IOException if an io-exception occurs during serialization
     */
    private void serializeElement(ElementDefinition ed, JsonGenerator gen, SerializerProvider provider)
            throws IOException {

        gen.writeStartObject();
        gen.writeStringField(NM_NAME, ed.getName());
        gen.writeNumberField(NM_SORT, ed.getSort());
        gen.writeStringField(NM_TYPE, ed.getType().getIdentifier());
        gen.writeStringField(NM_DATA_TYPE, ed.getDataType().getIdentifier());
        gen.writeStringField(NM_DEFAULT_VALUE, ed.getDefaultValue());
        gen.writeStringField(NM_VISIBLE, ed.getVisible());
        gen.writeStringField(NM_EDITABLE, ed.getEditable());
        gen.writeStringField(NM_REQUIRED, ed.getRequired());
        gen.writeStringField(NM_COL, ed.getCol());
        gen.writeBooleanField(NM_SHOW_AS_COLUMN, ed.isShowAsColumn());
        gen.writeStringField(NM_CSS, ed.getCss());
        gen.writeBooleanField(NM_SHOW_LABEL, ed.isShowLabel());
        gen.writeBooleanField(NM_SHOW_HELP, ed.isShowHelp());
        gen.writeBooleanField(NM_LINE_BREAK, ed.isLineBreak());

        if (ed.getWizardFormatOptions() != null) {
            gen.writeObjectFieldStart(NM_WIZARD_FORMAT_OPTIONS);
            gen.writeBooleanField(NM_SKIP_IN_SUMMARY, ed.getWizardFormatOptions().isSkipInSummary());
            gen.writeBooleanField(NM_SKIP_IN_FORM, ed.getWizardFormatOptions().isSkipInForm());
            gen.writeEndObject();
        }

        if (ed.getColumnOptions() != null) {
            gen.writeObjectFieldStart(NM_COLUMN_OPTIONS);
            gen.writeStringField(NM_MIN_COLUMN_WIDTH, ed.getColumnOptions().getMinColumnWidth());
            gen.writeStringField(NM_MAX_COLUMN_WIDTH, ed.getColumnOptions().getMaxColumnWidth());
            gen.writeEndObject();
        }

        serializeValidationRules(ed, gen);
        if (includeKeys) {
            gen.writeStringField(NM_KEY, ed.getKey());
        }

        switch (ed.getType()) {
            case Alert:
                gen.writeStringField(NM_DESIGN, ((AlertElementDefinition) ed).getDesign().getIdentifier());
                gen.writeStringField(NM_ICON, ((AlertElementDefinition) ed).getIcon());
                break;
            case Attachment:
                gen.writeStringField(NM_ADAPTER, ((AttachmentElementDefinition) ed).getAdapter());
                gen.writeBooleanField(NM_HAS_DESCRIPTION, ((AttachmentElementDefinition) ed).isHasDescription());
                gen.writeStringField(NM_CARDINALITY,
                        ((AttachmentElementDefinition) ed).getCardinality().getIdentifier());
                gen.writeStringField(NM_FILE_TYPES, ((AttachmentElementDefinition) ed).getFileTypes());
                gen.writeStringField(NM_DESIGN, ((AttachmentElementDefinition) ed).getDesign().getIdentifier());
                gen.writeStringField(NM_SELECT, ((AttachmentElementDefinition) ed).getSelect().getIdentifier());

                gen.writeArrayFieldStart(NM_CATEGORIES);
                for (CategoryOptions categoryOptions : ((AttachmentElementDefinition) ed).getCategories()) {
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

                if (((AttachmentElementDefinition) ed).getValueHelp() != null) {
                    gen.writeObjectFieldStart(NM_VALUE_HELP);
                    gen.writeStringField(NM_NAME, ((AttachmentElementDefinition) ed).getValueHelp().getName());
                    gen.writeBooleanField(NM_VALIDATE, ((AttachmentElementDefinition) ed).getValueHelp().isValidate());
                    gen.writeBooleanField(NM_EMPTY_SELECTION,
                            ((AttachmentElementDefinition) ed).getValueHelp().isEmptySelection());
                    gen.writeStringField(NM_DISPLAY_FORMAT,
                            ((AttachmentElementDefinition) ed).getValueHelp().getDisplayFormat());
                    gen.writeEndObject();
                }

                break;
            case Button:
                gen.writeStringField(NM_DESIGN, ((ButtonElementDefinition) ed).getDesign().getIdentifier());
                gen.writeStringField(NM_ICON, ((ButtonElementDefinition) ed).getIcon());
                if (((ButtonElementDefinition) ed).getTooltip() != null
                        && !((ButtonElementDefinition) ed).getTooltip().isEmpty()) {
                    gen.writeStringField(NM_TOOLTIP, ((ButtonElementDefinition) ed).getTooltip());
                }
                if (((ButtonElementDefinition) ed).getLinkHRef() != null
                        && !((ButtonElementDefinition) ed).getLinkHRef().isEmpty()) {
                    gen.writeStringField(NM_LINK_HREF, ((ButtonElementDefinition) ed).getLinkHRef());
                }
                break;
            case Currency:
                if (((CurrencyElementDefinition) ed).getValueHelp() != null) {
                    gen.writeObjectFieldStart(NM_VALUE_HELP);
                    gen.writeStringField(NM_NAME, ((CurrencyElementDefinition) ed).getValueHelp().getName());
                    gen.writeBooleanField(NM_VALIDATE, ((CurrencyElementDefinition) ed).getValueHelp().isValidate());
                    gen.writeBooleanField(NM_EMPTY_SELECTION,
                            ((CurrencyElementDefinition) ed).getValueHelp().isEmptySelection());
                    gen.writeStringField(NM_DISPLAY_FORMAT,
                            ((CurrencyElementDefinition) ed).getValueHelp().getDisplayFormat());
                    gen.writeEndObject();
                }
                break;
            case Dialog:
                Dimension size = ((DialogElementDefinition) ed).getSize();
                if (size != null) {
                    gen.writeObjectFieldStart(NM_SIZE);
                    gen.writeStringField(NM_HEIGHT, ((DialogElementDefinition) ed).getSize().getHeight());
                    gen.writeStringField(NM_WIDTH, ((DialogElementDefinition) ed).getSize().getWidth());
                    gen.writeEndObject();
                }

                this.serializeElementWithName(NM_FOOTER, ((DialogElementDefinition) ed).getFooter(), gen, provider);
                break;
            case DocForm:
                this.serializeElementWithName(NM_HEADER_SEGMENT, ((DocFormElementDefinition) ed).getHeaderSegment(),
                        gen, provider);
                this.serializeElementWithName(NM_FOOTER, ((DocFormElementDefinition) ed).getFooter(), gen, provider);
                break;
            case Form:
                this.serializeElementWithName(NM_FOOTER, ((FormElementDefinition) ed).getFooter(), gen, provider);
                this.serializeElementWithName(NM_HEADER_SEGMENT, ((FormElementDefinition) ed).getHeaderSegment(), gen,
                        provider);
                break;
            case Icon:
                gen.writeStringField(NM_ICON, ((IconElementDefinition) ed).getIcon());
                if (((IconElementDefinition) ed).getTooltip() != null
                        && !((IconElementDefinition) ed).getTooltip().isEmpty()) {
                    gen.writeStringField(NM_TOOLTIP, ((IconElementDefinition) ed).getTooltip());
                }
                break;
            case Image:
                if (((ImageElementDefinition) ed).getSize() != null) {
                    gen.writeObjectFieldStart(NM_SIZE);
                    gen.writeStringField(NM_HEIGHT, ((ImageElementDefinition) ed).getSize().getHeight());
                    gen.writeStringField(NM_WIDTH, ((ImageElementDefinition) ed).getSize().getWidth());
                    gen.writeEndObject();
                }
                break;
            case Input:
                gen.writeStringField(NM_INPUT_TYPE, ((InputElementDefinition) ed).getInputType().getIdentifier());
                break;
            case Link:
                if (((LinkElementDefinition) ed).getLinkData() != null) {
                    LinkData linkData = ((LinkElementDefinition) ed).getLinkData();
                    if (linkData.getText() != null && !linkData.getText().isEmpty()) {
                        gen.writeStringField(NM_LINK_TEXT, linkData.getText());
                    }
                    if (linkData.getHRef() != null && !linkData.getHRef().isEmpty()) {
                        gen.writeStringField(NM_LINK_HREF, linkData.getHRef());
                    }
                }
                break;
            case MultiSelect:
                if (((MultiSelectElementDefinition) ed).getValueHelp() != null) {
                    gen.writeObjectFieldStart(NM_VALUE_HELP);
                    gen.writeStringField(NM_NAME, ((MultiSelectElementDefinition) ed).getValueHelp().getName());
                    gen.writeBooleanField(NM_VALIDATE, ((MultiSelectElementDefinition) ed).getValueHelp().isValidate());
                    gen.writeBooleanField(NM_EMPTY_SELECTION,
                            ((MultiSelectElementDefinition) ed).getValueHelp().isEmptySelection());
                    gen.writeStringField(NM_DISPLAY_FORMAT,
                            ((MultiSelectElementDefinition) ed).getValueHelp().getDisplayFormat());
                    gen.writeEndObject();
                }
                break;
            case Radio:
                if (((RadioElementDefinition) ed).getValueHelp() != null) {
                    gen.writeObjectFieldStart(NM_VALUE_HELP);
                    gen.writeStringField(NM_NAME, ((RadioElementDefinition) ed).getValueHelp().getName());
                    gen.writeBooleanField(NM_VALIDATE, ((RadioElementDefinition) ed).getValueHelp().isValidate());
                    gen.writeBooleanField(NM_EMPTY_SELECTION,
                            ((RadioElementDefinition) ed).getValueHelp().isEmptySelection());
                    gen.writeStringField(NM_DISPLAY_FORMAT,
                            ((RadioElementDefinition) ed).getValueHelp().getDisplayFormat());
                    gen.writeEndObject();
                }
                break;
            case SearchHelp:
                if (((SearchHelpElementDefinition) ed).getSize() != null) {
                    gen.writeObjectFieldStart(NM_SIZE);
                    gen.writeStringField(NM_HEIGHT, ((SearchHelpElementDefinition) ed).getSize().getHeight());
                    gen.writeStringField(NM_WIDTH, ((SearchHelpElementDefinition) ed).getSize().getWidth());
                    gen.writeEndObject();
                }
                this.serializeElementWithName(NM_FOOTER, ((SearchHelpElementDefinition) ed).getFooter(), gen, provider);
                break;
            case Select:
                if (((SelectElementDefinition) ed).getValueHelp() != null) {
                    gen.writeObjectFieldStart(NM_VALUE_HELP);
                    gen.writeStringField(NM_NAME, ((SelectElementDefinition) ed).getValueHelp().getName());
                    gen.writeBooleanField(NM_VALIDATE, ((SelectElementDefinition) ed).getValueHelp().isValidate());
                    gen.writeBooleanField(NM_EMPTY_SELECTION,
                            ((SelectElementDefinition) ed).getValueHelp().isEmptySelection());
                    gen.writeStringField(NM_DISPLAY_FORMAT,
                            ((SelectElementDefinition) ed).getValueHelp().getDisplayFormat());
                    gen.writeEndObject();
                }
                break;
            case Mixin:
                gen.writeStringField(NM_MIXIN_NAME, ((MetaFileElementDefinition) ed).getMixinName());
                gen.writeStringField(NM_PATH, ((MetaFileElementDefinition) ed).getPath());
                gen.writeNumberField(NM_VERSION, ((MetaFileElementDefinition) ed).getVersion());
                if (this.toFrontend) {
                    gen.writeStringField(NM_KIND, ((MetaFileElementDefinition) ed).getKindCode());
                }
                break;
            case Table:
                gen.writeStringField(NM_SELECT, ((TableElementDefinition) ed).getSelect().getIdentifier());
                gen.writeStringField(NM_STYLE, ((TableElementDefinition) ed).getStyle().getIdentifier());
                gen.writeNumberField(NM_PAGE_SIZE, ((TableElementDefinition) ed).getPageSize());
                this.serializeElementWithName(NM_TOOLBAR, ((TableElementDefinition) ed).getToolbar(), gen, provider);
                break;
            case Toolbar:
                gen.writeFieldName(NM_LEFT_ELEMENTS);
                gen.writeStartArray();
                for (var it : ((ToolbarElementDefinition) ed).getLeftElements()) {
                    this.serializeElement(it, gen, provider);
                }
                gen.writeEndArray();
                gen.writeFieldName(NM_RIGHT_ELEMENTS);
                gen.writeStartArray();
                for (var it : ((ToolbarElementDefinition) ed).getRightElements()) {
                    this.serializeElement(it, gen, provider);
                }
                gen.writeEndArray();
                break;
            case Wizard:
                this.serializeElementWithName(NM_FOOTER, ((WizardElementDefinition) ed).getFooter(), gen, provider);
                break;
        }

        this.serializeElements(ed.getElements(), gen, provider);

        gen.writeEndObject();
    }

    /**
     * @param ed
     * @param gen
     * @throws IOException
     */
    private void serializeValidationRules(final ElementDefinition ed, final JsonGenerator gen) throws IOException {
        gen.writeFieldName(NM_VALIDATION_RULES);
        gen.writeStartArray();

        if (ed.getValidationRules() != null) {
            for (ValidationRule it : ed.getValidationRules()) {
                gen.writeStartObject();
                gen.writeStringField(NM_TYPE, ((AbstractValidationRule) it).getType().getIdentifier());
                gen.writeStringField(NM_SEVERITY, ((AbstractValidationRule) it).getSeverity().getIdentifier());
                gen.writeStringField(NM_MESSAGE_KEY, ((AbstractValidationRule) it).getMessageKey());

                if (it instanceof MinValidationRule) {
                    gen.writeStringField(NM_LIMIT, ((MinValidationRule) it).getLimit());
                    gen.writeBooleanField(NM_INCLUSIVE, ((MinValidationRule) it).isInclusive());
                } else if (it instanceof MaxValidationRule) {
                    gen.writeStringField(NM_LIMIT, ((MaxValidationRule) it).getLimit());
                    gen.writeBooleanField(NM_INCLUSIVE, ((MaxValidationRule) it).isInclusive());
                } else if (it instanceof FixedValidationRule) {
                    gen.writeNumberField(NM_LENGTH, ((FixedValidationRule) it).getLength());
                    gen.writeNumberField(NM_FRACTIONS, ((FixedValidationRule) it).getFractions());
                } else if (it instanceof RegexValidationRule) {
                    gen.writeStringField(NM_PATTERN, ((RegexValidationRule) it).getPattern());
                } else if (it instanceof SpelValidationRule) {
                    gen.writeStringField(NM_EXPRESSION, ((SpelValidationRule) it).getExpression());
                } else if (it instanceof BeanValidationRule) {
                    gen.writeStringField(NM_BEAN_NAME, ((BeanValidationRule) it).getBeanName());
                }

                gen.writeEndObject();
            }
        }

        gen.writeEndArray();
    }

    /**
     * @param name
     * @param ed
     * @param gen
     * @param provider
     * @throws IOException
     */
    private void serializeElementWithName(final String name, final ElementDefinition ed, final JsonGenerator gen,
                                          final SerializerProvider provider) throws IOException {
        if (ed != null) {
            gen.writeFieldName(name);
            this.serializeElement(ed, gen, provider);
        }
    }
}
