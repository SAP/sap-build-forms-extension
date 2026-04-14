package com.sap.bfx.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sap.bfx.session.*;
import com.sap.bfx.utils.EnumUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Slf4j
public class ElementDefinition {
    public final static TypeLiteral BooleanType = new TypeLiteral<Boolean>() {
    };
    public final static TypeLiteral StringType = new TypeLiteral<String>() {
    };
    public final static TypeLiteral AttachmentType = new TypeLiteral<Attachment>() {
    };
    public final static TypeLiteral IntegerType = new TypeLiteral<Integer>() {
    };
    public final static TypeLiteral BigDecimalType = new TypeLiteral<java.math.BigDecimal>() {
    };
    public final static TypeLiteral DateType = new TypeLiteral<LocalDate>() {
    };
    public final static TypeLiteral TimeType = new TypeLiteral<LocalTime>() {
    };
    public final static TypeLiteral DateTimeType = new TypeLiteral<LocalDateTime>() {
    };
    public final static TypeLiteral DateRangeType = new TypeLiteral<DateRange>() {
    };

    private String name;
    private String key;
    private int sort;
    @JsonIgnore
    private UIElementType type;
    private DataType dataType;
    private String defaultValue;
    private String visible;
    private String editable;
    private String required;
    private String col;
    private String css;
    private boolean showLabel;
    private boolean showHelp;
    private boolean lineBreak;
    private WizardFormatOptions wizardFormatOptions;
    private ColumnOptions columnOptions;
    private List<ElementDefinition> elements = new ArrayList<>();
    private boolean showAsColumn;
    private List<ValidationRule> validationRules = new ArrayList<>();

    @JsonIgnore
    private Evaluator<Object> defaultValueEvaluator;
    @JsonIgnore
    private Evaluator<Boolean> visibleEvaluator;
    @JsonIgnore
    private Evaluator<Boolean> editableEvaluator;
    @JsonIgnore
    private Evaluator<Boolean> requiredEvaluator;

    /**
     * Constructor with type.
     *
     * @param uiElementType
     */
    protected ElementDefinition(final UIElementType uiElementType) {
        this.type = uiElementType;
    }

    /**
     * Default constructor, needed for deserialization.
     */
    public ElementDefinition() {
        this.type = null;
    }

    /**
     * @param identifier
     * @return
     */
    public static DataType mapDataType(String identifier) {
        return EnumUtils.valueById(DataType.class, identifier, DataType.Auto);
    }

    /**
     * @param h
     * @return
     */
    public static boolean isExpression(String h) {
        h = StringUtils.trim(h);
        return StringUtils.startsWith(h, SpelEvaluator.START) && StringUtils.endsWith(h, SpelEvaluator.END);
    }

    /**
     *
     * @param h
     * @return
     */
    public static boolean isRegex(String h) {
        h = StringUtils.trim(h);
        return StringUtils.startsWith(h, RegexEvaluator.START) && StringUtils.endsWith(h, RegexEvaluator.END);
    }

    /**
     * Get the data type class for the element definition, based on the element type and data type.
     *
     * @param ed
     * @return
     */
    public static Class<?> getDataTypeClass(final ElementDefinition ed) {
        return switch (ed.getType()) {
            case Alert, Button, Dialog, Form, Icon, Image, MultiSelect, Radio, SearchHelp, Select,
                 Text, TextEdit, Wizard -> String.class;
            case Attachment -> Attachments.class;
            case DateRangePicker -> DateRange.class;
            case Input -> switch (ed.getDataType()) {
                case Int -> Integer.class;
                case Decimal -> java.math.BigDecimal.class;
                case Date -> LocalDate.class;
                case Time -> LocalTime.class;
                case DateTime -> LocalDateTime.class;
                default -> String.class;
            };
            case Link -> LinkData.class;
            case Table -> Table.class;
            case DocForm ->  DocFormData.class;
            case Currency -> MoneyAmount.class;
            default -> Boolean.class;
        };
    }

    /**
     * @param sd
     * @param ed
     * @param appContext
     */
    public static void postLoad(final ScenarioDefinition sd, final ElementDefinition ed,
                                final ApplicationContext appContext) {
        ed.setDefaultValueEvaluator((Evaluator<Object>) createEvaluator(ed.getDefaultValue(), false,
                getDataTypeClass(ed), ed));
        ed.setVisibleEvaluator(createEvaluator(ed.getVisible(), true, Boolean.class, ed));
        ed.setEditableEvaluator(createEvaluator(ed.getEditable(), true, Boolean.class, ed));
        ed.setRequiredEvaluator(createEvaluator(ed.getRequired(), true, Boolean.class, ed));

        // put element-definition to map
        sd.getElementsMap().put(ed.getKey(), ed);

        // check if value-help is defined and add it to the set if there is one
        if (ed instanceof HasValueHelp d) {
            if (d.getValueHelp() != null &&
                    StringUtils.isNotBlank(d.getValueHelp().getName())) {
                sd.getValueHelpIds().add(d.getValueHelp().getName());
            }
        }

        // post init the validation rules
        ed.getValidationRules().forEach(it -> {
            if (it instanceof MinValidationRule) {
                ((MinValidationRule) it).setDataTypeClass(getDataTypeClass(ed));
            } else if (it instanceof MaxValidationRule) {
                ((MaxValidationRule) it).setDataTypeClass(getDataTypeClass(ed));
            } else if (it instanceof FixedValidationRule) {
                ((FixedValidationRule) it).setDataTypeClass(getDataTypeClass(ed));
            } else if (it instanceof BeanValidationRule) {
                ((BeanValidationRule) it).setAppContext(appContext);
            }
            it.postLoad();
        });

        // iterate over all children, including elements and everything from options...
        ed.getChildren().forEach(it -> it.forEach(it2 -> postLoad(sd, it2, appContext)));
    }

    /**
     * @param expression
     * @param isVisual
     * @param cls
     * @param ed
     * @param <T>
     * @return
     */
    private static <T> Evaluator<T> createEvaluator(final String expression, final boolean isVisual,
                                                    final Class<T> cls, ElementDefinition ed) {
        if (ElementDefinition.isExpression(expression)) {
            return new SpelEvaluator<>(expression, cls);
        } else if (ElementDefinition.isRegex(expression)) {
            return (Evaluator<T>) new RegexEvaluator(expression);
        }
        return (Evaluator<T>) (isVisual
                ? new StandardVisualEvaluator(expression)
                : new StandardValueEvaluator(ed));
    }

    /**
     * Get the children of the element definition, which are the elements collection and everything from options.
     *
     * @return
     */
    @JsonIgnore
    public Collection<Collection<ElementDefinition>> getChildren() {
        var result = new ArrayList<Collection<ElementDefinition>>();

        // simple, if the element's elements collection is not empty, add it
        if (!getElements().isEmpty()) {
            result.add(getElements());
        }

        return result;
    }

    /**
     * Check if the element definition has its own type, which means it is not just a field but a complex element.
     *
     * @return
     */
    public final boolean hasOwnType() {
        return this.getType() == UIElementType.Form
                || this.getType() == UIElementType.DocForm
                || this.getType() == UIElementType.Wizard
                || this.getType() == UIElementType.Dialog
                || this.getType() == UIElementType.SearchHelp
                || this.getType() == UIElementType.Table;
    }

    /**
     * Check if the element definition is a root type, which means it can be the root of a scenario.
     *
     * @return
     */
    @JsonIgnore
    public final boolean isRootType() {
        return this.getType() == UIElementType.Form
                || this.getType() == UIElementType.DocForm
                || this.getType() == UIElementType.Wizard;
    }

    /**
     * Check if the element definition can have children, which means it is a complex element that can contain
     * other elements.
     *
     * @return
     */
    public final boolean hasChildren() {
        return this.getType() == UIElementType.Form
                || this.getType() == UIElementType.DocForm
                || this.getType() == UIElementType.Wizard
                || this.getType() == UIElementType.Dialog
                || this.getType() == UIElementType.Segment
                || this.getType() == UIElementType.Group
                || this.getType() == UIElementType.SearchHelp
                || this.getType() == UIElementType.Toolbar;
    }

    /**
     * Check if the element definition is visual dependent, which means it is a complex element that has a visual
     * representation and can contain other elements.
     *
     * @return
     */
    @JsonIgnore
    public final boolean isVisualDependent() {
        return this.getType() == UIElementType.Form
                || this.getType() == UIElementType.DocForm
                || this.getType() == UIElementType.Wizard
                || this.getType() == UIElementType.Segment
                || this.getType() == UIElementType.Group
                || this.getType() == UIElementType.Toolbar;
    }

    /**
     * Check if the element definition is a collection type, which means it is a table that can contain multiple
     * rows of
     *
     * @return
     */
    @JsonIgnore
    public final boolean isCollection() {
        return this.getType() == UIElementType.Table;
    }

    /**
     * Check if the element definition has a toolbar, which means it is a table or a dialog that can have actions.
     *
     * @return
     */
    public final boolean hasToolbar() {
        return this.getType() == UIElementType.Table || this.getType() == UIElementType.Dialog;
    }

    /**
     * Check if the element definition can be editable, which means it is not a simple visual element that cannot be
     * interacted with.
     *
     * @return
     */
    public final boolean canBeEditable() {
        return this.getType() != UIElementType.Alert
                && this.getType() != UIElementType.Dialog
                && this.getType() != UIElementType.Dummy
                && this.getType() != UIElementType.Text
                && this.getType() != UIElementType.Toolbar;
    }

    /**
     * Check if the element definition can be required, which means it is not a simple visual element that cannot be
     * interacted with and can have a value that is required.
     *
     * @return
     */
    public final boolean canBeRequired() {
        return this.getType() != UIElementType.Alert
                && this.getType() != UIElementType.Dialog
                && this.getType() != UIElementType.Dummy
                && this.getType() != UIElementType.Form
                && this.getType() != UIElementType.Text
                && this.getType() != UIElementType.Toolbar
                && this.getType() != UIElementType.Wizard;
    }

    /**
     * Check if the element definition can have a message, which means it is not a simple visual element that cannot
     * be interacted with and can have a value that can have a validation message.
     *
     * @return
     */
    public final boolean canHaveMessage() {
        return this.getType() != UIElementType.Alert
                && this.getType() != UIElementType.Dialog
                && this.getType() != UIElementType.Dummy
                && this.getType() != UIElementType.Text
                && this.getType() != UIElementType.Toolbar
                && this.getType() != UIElementType.Wizard;
    }
}
