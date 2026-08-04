package com.sap.bfx.definition;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.exception.FormsCoreException;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.sap.bfx.definition.DefinitionNames.*;
import static com.sap.bfx.utils.SerializationUtils.*;

/**
 * Helper class for deserialization of element definitions
 */
class DeserializationHelper {

    /**
     * Private constructor to prevent instantiation
     */
    static void readElementDefinition(final JsonNode node, final ElementDefinition element) {
        var defaultVisibilityProp = (element.getType() == UIElementType.Mixin) ? "" : "{true}";

        setStringProp(node, element, NM_NAME, null);
        setStringProp(node, element, NM_KEY, null);
        setIntProp(node, element, NM_SORT, 0);
        setMappedProp(node, element, NM_DATA_TYPE, ElementDefinition::mapDataType);
        setStringProp(node, element, NM_DEFAULT_VALUE, "");
        setStringProp(node, element, NM_VISIBLE, defaultVisibilityProp);
        setStringProp(node, element, NM_EDITABLE, defaultVisibilityProp);
        setStringProp(node, element, NM_REQUIRED, defaultVisibilityProp);
        setStringProp(node, element, NM_COL, null);
        setBooleanProp(node, element, NM_SHOW_AS_COLUMN, Boolean.FALSE);
        setStringProp(node, element, NM_CSS, null);
        setBooleanProp(node, element, NM_SHOW_LABEL, Boolean.TRUE);
        setBooleanProp(node, element, NM_SHOW_HELP, Boolean.FALSE);
        setBooleanProp(node, element, NM_LINE_BREAK, Boolean.FALSE);
        readWizardFormatOptions(node, element);
        readColumnOptions(node, element);

        DeserializationHelper.readValidationRules(node, element);

        // set type specific additional properties
        switch (element.getType()) {
            case Alert:
                setStringProp(node, element, NM_ICON, "");
                setMappedProp(node, element, NM_DESIGN, AlertElementDefinition::mapDesignType);
                break;
            case Attachment:
                setStringProp(node, element, NM_ADAPTER, "");
                setBooleanProp(node, element, NM_HAS_DESCRIPTION, false);
                setMappedProp(node, element, NM_DESIGN, AttachmentElementDefinition::mapDesignType);
                setStringProp(node, element, NM_FILE_TYPES, "");
                setMappedProp(node, element, NM_CARDINALITY, AttachmentCardinality::mapUploadType);
                setMappedProp(node, element, NM_SELECT, AttachmentSelectType::mapSelectType);
                readCategories(node, element);
                break;
            case Button:
                setMappedProp(node, element, NM_DESIGN, ButtonElementDefinition::mapDesignType);
                setStringProp(node, element, NM_ICON, "");
                setStringProp(node, element, NM_LINK_HREF, "");
                setStringProp(node, element, NM_TOOLTIP, "");
                break;
            case Icon:
                setStringProp(node, element, NM_ICON, "");
                setStringProp(node, element, NM_TOOLTIP, "");
                break;
            case Currency:
                readValueHelpOptions(node, element);
                break;
            case Dialog:
                readDimension(node, element);
                DeserializationHelper.readAddonElementDefinition(node, element, NM_FOOTER, NM_FOOTER,
                        Constants.TYPE_TOOLBAR, element.getName() + "Footer");
                break;
            case DocForm:
                //TODO: remove the docUrl property in favor of a mixin in the future
            case Form:
                DeserializationHelper.readAddonElementDefinition(node, element, NM_FOOTER, NM_FOOTER,
                        Constants.TYPE_TOOLBAR, element.getName() + "Footer");
                DeserializationHelper.readAddonElementDefinition(node, element, NM_HEADER_SEGMENT, NM_HEADER_SEGMENT,
                        Constants.TYPE_SEGMENT, null);
                break;
            case Image:
                readDimension(node, element);
                break;
            case Input:
                setMappedProp(node, element, NM_INPUT_TYPE, InputElementDefinition::mapType);
                break;
            case Link:
                if (node.get(NM_LINK_TEXT) != null || node.get(NM_LINK_HREF) != null) {
                    LinkData linkData = new LinkData();
                    if (node.get(NM_LINK_TEXT) != null) {
                        linkData.setText(node.get(NM_LINK_TEXT).asText());
                    }
                    if (node.get(NM_LINK_HREF) != null) {
                        linkData.setHRef(node.get(NM_LINK_HREF).asText());
                    }
                    ((LinkElementDefinition) element).setLinkData(linkData);
                }
                break;
            case Mixin:
                setStringProp(node, element, NM_PATH, "");
                setStringProp(node, element, NM_MIXIN_NAME, "");
                setIntProp(node, element, NM_VERSION, 0);
                break;
            case MultiSelect:
                readValueHelpOptions(node, element);
                break;
            case Radio:
                readValueHelpOptions(node, element);
                break;
            case SearchHelp:
                readDimension(node, element);
                DeserializationHelper.readAddonElementDefinition(node, element, NM_FOOTER, NM_FOOTER,
                        Constants.TYPE_TOOLBAR, element.getName() + "Footer");
                break;
            case Select:
                readValueHelpOptions(node, element);
                break;
            case Table:
                setMappedProp(node, element, NM_SELECT, TableElementDefinition::mapSelectType);
                setMappedProp(node, element, NM_STYLE, TableElementDefinition::mapStyleType);
                setIntProp(node, element, NM_PAGE_SIZE, 25);
                DeserializationHelper.readAddonElementDefinition(node, element, NM_TOOLBAR, NM_TOOLBAR,
                        Constants.TYPE_TOOLBAR, element.getName() + "Toolbar");
                break;
            case Toolbar:
                DeserializationHelper.readElementsDefinitions(node,
                        ((ToolbarElementDefinition) element).getLeftElements(), NM_LEFT_ELEMENTS);
                DeserializationHelper.readElementsDefinitions(node,
                        ((ToolbarElementDefinition) element).getRightElements(), NM_RIGHT_ELEMENTS);
                break;
            case Wizard:
                DeserializationHelper.readAddonElementDefinition(node, element, NM_FOOTER, NM_FOOTER,
                        Constants.TYPE_TOOLBAR, element.getName() + "Footer");
                break;
        }

        // now add any potential children
        DeserializationHelper.readElementsDefinitions(node, element.getElements(), NM_ELEMENTS);
    }

    /**
     * @param node
     * @param elements
     * @param name
     */
    static void readElementsDefinitions(final JsonNode node, final List<ElementDefinition> elements,
                                        final String name) {
        final var children = node.get(name);
        if (children == null) {
            return;
        }

        children.iterator().forEachRemaining(it -> {
            try {
                final var element = DeserializationHelper.createElementDefinitionByType(it.get(NM_TYPE).asText());
                DeserializationHelper.readElementDefinition(it, element);
                elements.add(element);
            } catch (Exception e) {
                throw ExceptionUtils.from(e);
            }
        });
    }

    /**
     * @param type
     * @return
     */
    static ElementDefinition createElementDefinitionByType(String type) {
        if (StringUtils.isBlank(type)) {
            type = Constants.TYPE_NONE;
        }

        return switch (type) {
            case Constants.TYPE_ALERT -> new AlertElementDefinition();
            case Constants.TYPE_AUTO_COMPLETE -> new AutoCompleteElementDefinition();
            case Constants.TYPE_BUTTON -> new ButtonElementDefinition();
            case Constants.TYPE_CHECKBOX -> new CheckboxElementDefinition();
            case Constants.TYPE_CURRENCY -> new CurrencyElementDefinition();
            case Constants.TYPE_DATE_RANGE_PICKER -> new DateRangeElementDefinition();
            case Constants.TYPE_DIALOG -> new DialogElementDefinition();
            case Constants.TYPE_DOC_FORMS -> new DocFormElementDefinition();
            case Constants.TYPE_DUMMY -> new DummyElementDefinition();
            case Constants.TYPE_FORM -> new FormElementDefinition();
            case Constants.TYPE_GROUP -> new GroupElementDefinition();
            case Constants.TYPE_ICON -> new IconElementDefinition();
            case Constants.TYPE_IMAGE -> new ImageElementDefinition();
            case Constants.TYPE_LINK -> new LinkElementDefinition();
            case Constants.TYPE_MIXIN -> new MetaFileElementDefinition();
            case Constants.TYPE_INPUT -> new InputElementDefinition();
            case Constants.TYPE_MULTI_SELECT -> new MultiSelectElementDefinition();
            case Constants.TYPE_RADIO -> new RadioElementDefinition();
            case Constants.TYPE_SEARCH_HELP -> new SearchHelpElementDefinition();
            case Constants.TYPE_SEGMENT -> new SegmentElementDefinition();
            case Constants.TYPE_SELECT -> new SelectElementDefinition();
            case Constants.TYPE_TABLE -> new TableElementDefinition();
            case Constants.TYPE_TEXT -> new TextElementDefinition();
            case Constants.TYPE_TEXT_EDIT -> new TextEditElementDefinition();
            case Constants.TYPE_TOOLBAR -> new ToolbarElementDefinition();
            case Constants.TYPE_ATTACHMENT -> new AttachmentElementDefinition();
            case Constants.TYPE_WIZARD -> new WizardElementDefinition();
            case Constants.TYPE_NONE -> new ElementDefinition();
            default -> throw new FormsCoreException("unknown type: " + type);
        };
    }

    /**
     * @param node
     * @param element
     */
    private static void readValidationRules(final JsonNode node, final ElementDefinition element) {
        var rules = new ArrayList<ValidationRule>();
        element.setValidationRules(rules);

        final var children = node.get(NM_VALIDATION_RULES);
        if (children == null) {
            return;
        }

        children.iterator().forEachRemaining(it -> {
            var rule = switch (it.get(NM_TYPE).asText()) {
                case Constants.VALIDATION_TYPE_FIXED -> new FixedValidationRule();
                case Constants.VALIDATION_TYPE_MIN -> new MinValidationRule();
                case Constants.VALIDATION_TYPE_MAX -> new MaxValidationRule();
                case Constants.VALIDATION_TYPE_BEAN -> new BeanValidationRule();
                case Constants.VALIDATION_TYPE_REGEX -> new RegexValidationRule();
                case Constants.VALIDATION_TYPE_SPEL -> new SpelValidationRule();
                default -> throw new FormsCoreException(
                        "unknown type '" + it.get(NM_TYPE).asText() + "' for validation rule");
            };
            rule.setSeverity(ValidationRule.mapSeverity(StringUtils.left(it.get(NM_SEVERITY).asText(), 1)));
            rule.setMessageKey(it.get(NM_MESSAGE_KEY).asText());

            if (rule instanceof MinValidationRule) {
                ((MinValidationRule) rule).setLimit(it.get(NM_LIMIT).asText());
                ((MinValidationRule) rule).setInclusive(it.get(NM_INCLUSIVE).asBoolean());
            } else if (rule instanceof MaxValidationRule) {
                ((MaxValidationRule) rule).setLimit(it.get(NM_LIMIT).asText());
                ((MaxValidationRule) rule).setInclusive(it.get(NM_INCLUSIVE).asBoolean());
            } else if (rule instanceof FixedValidationRule) {
                ((FixedValidationRule) rule).setLength(it.get(NM_LENGTH).asInt());
                ((FixedValidationRule) rule).setFractions(it.get(NM_FRACTIONS).asInt());
            } else if (rule instanceof RegexValidationRule) {
                ((RegexValidationRule) rule).setPattern(it.get(NM_PATTERN).asText());
            } else if (rule instanceof SpelValidationRule) {
                ((SpelValidationRule) rule).setExpression(it.get(NM_EXPRESSION).asText());
            } else {
                ((BeanValidationRule) rule).setBeanName(it.get(NM_BEAN_NAME).asText());
            }

            rules.add(rule);
        });
    }

    /**
     * Read an additional element definition
     *
     * @param node            Json node
     * @param element         element definition
     * @param nodeElementName name of the node in the JSON
     * @param propertyName    name of the property in the element definition
     * @param type            type of the element definition
     * @param name            name to set for the element definition (if null, the name from JSON is used)
     */
    private static void readAddonElementDefinition(final JsonNode node, final ElementDefinition element,
                                                   final String nodeElementName, final String propertyName,
                                                   final String type, final String name) {
        if (node.get(nodeElementName) != null) {
            final var addonElement = DeserializationHelper.createElementDefinitionByType(type);
            DeserializationHelper.readElementDefinition(node.get(nodeElementName), addonElement);
            if (StringUtils.isNoneEmpty(name)) {
                addonElement.setName(name);
            }
            try {
                BeanUtils.setProperty(element, propertyName, addonElement);
            } catch (Exception e) {
                throw ExceptionUtils.from(e);
            }
        }
    }

    /**
     * Read the categories for attachment element
     *
     * @param node    Json node
     * @param element element definition
     */
    private static void readCategories(JsonNode node, ElementDefinition element) {
        try {
            List<CategoryOptions> categoryOptionsList = new ArrayList<>();
            JsonNode categoryOptionsNode = node.get(NM_CATEGORIES);
            for (JsonNode categoryNode : categoryOptionsNode) {
                CategoryOptions categoryOptions = new CategoryOptions();
                setStringProp(categoryNode, categoryOptions, NM_LABEL, "");

                ValueHelpOption valueHelpOption = new ValueHelpOption();
                JsonNode valueHelpOptionNode = categoryNode.get(NM_HV_OPT);
                setStringProp(valueHelpOptionNode, valueHelpOption, NM_NAME, "");
                setBooleanProp(valueHelpOptionNode, valueHelpOption, NM_VALIDATE, Boolean.FALSE);
                setBooleanProp(valueHelpOptionNode, valueHelpOption, NM_EMPTY_SELECTION, Boolean.FALSE);
                setStringProp(valueHelpOptionNode, valueHelpOption, NM_DISPLAY_FORMAT, "");
                PropertyUtils.setProperty(categoryOptions, NM_HV_OPT, valueHelpOption);

                categoryOptionsList.add(categoryOptions);
            }
            PropertyUtils.setProperty(element, NM_CATEGORIES, categoryOptionsList);
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * Read the wizard format options
     *
     * @param node    Json node
     * @param element element definition
     */
    private static void readWizardFormatOptions(JsonNode node, ElementDefinition element) {
        WizardFormatOptions wizardFormatOptions = new WizardFormatOptions();
        JsonNode wizardFormatOptionsNode = node.get(NM_WIZARD_FORMAT_OPTIONS);

        try {
            if (wizardFormatOptionsNode != null && !wizardFormatOptionsNode.isNull()) {
                setBooleanProp(wizardFormatOptionsNode, wizardFormatOptions, NM_SKIP_IN_SUMMARY, Boolean.FALSE);
                setBooleanProp(wizardFormatOptionsNode, wizardFormatOptions, NM_SKIP_IN_FORM, Boolean.FALSE);
                PropertyUtils.setProperty(element, NM_WIZARD_FORMAT_OPTIONS, wizardFormatOptions);
            } else {
                PropertyUtils.setProperty(element, NM_WIZARD_FORMAT_OPTIONS, null);
            }
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * Read the column options
     *
     * @param node    Json node
     * @param element element definition
     */
    private static void readColumnOptions(JsonNode node, ElementDefinition element) {
        ColumnOptions columnOptions = new ColumnOptions();
        JsonNode columnOptionsNode = node.get(NM_COLUMN_OPTIONS);

        try {
            if (columnOptionsNode != null && !columnOptionsNode.isNull()) {
                setStringProp(columnOptionsNode, columnOptions, NM_MIN_COLUMN_WIDTH, "");
                setStringProp(columnOptionsNode, columnOptions, NM_MAX_COLUMN_WIDTH, "");
                PropertyUtils.setProperty(element, NM_COLUMN_OPTIONS, columnOptions);
            } else {
                PropertyUtils.setProperty(element, NM_COLUMN_OPTIONS, null);
            }
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * Read the value help options
     *
     * @param node    Json node
     * @param element element definition
     */
    private static void readValueHelpOptions(JsonNode node, ElementDefinition element) {
        ValueHelpOption valueHelpOption = new ValueHelpOption();
        JsonNode valueHelpOptionNode = node.get(NM_VALUE_HELP);

        try {
            if (valueHelpOptionNode != null && !valueHelpOptionNode.isNull()) {
                setStringProp(valueHelpOptionNode, valueHelpOption, NM_NAME, "");
                setBooleanProp(valueHelpOptionNode, valueHelpOption, NM_VALIDATE, Boolean.FALSE);
                setBooleanProp(valueHelpOptionNode, valueHelpOption, NM_EMPTY_SELECTION, Boolean.FALSE);
                setStringProp(valueHelpOptionNode, valueHelpOption, NM_DISPLAY_FORMAT, "");
                PropertyUtils.setProperty(element, NM_VALUE_HELP, valueHelpOption);
            } else {
                PropertyUtils.setProperty(element, NM_COLUMN_OPTIONS, null);
            }
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * Read the dimension options
     *
     * @param node    Json node
     * @param element element definition
     * @throws Exception
     */
    private static void readDimension(JsonNode node, ElementDefinition element) {
        Dimension dimension = new Dimension();
        JsonNode dimensionNode = node.get(NM_SIZE);

        try {
            if (dimensionNode != null && !dimensionNode.isNull()) {
                setStringProp(dimensionNode, dimension, NM_HEIGHT, "");
                setStringProp(dimensionNode, dimension, NM_WIDTH, "");
                PropertyUtils.setProperty(element, NM_SIZE, dimension);
            } else {
                PropertyUtils.setProperty(element, NM_SIZE, null);
            }
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }
}
