package com.sap.bfx.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sap.bfx.session.Attachment;
import com.sap.bfx.session.Attachments;
import com.sap.bfx.session.Table;
import com.sap.bfx.utils.EnumUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.springframework.context.ApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Slf4j
public class ElementDefinition {
    public final static TypeLiteral<Boolean> BooleanType = new TypeLiteral<>() {
    };
    public final static TypeLiteral<String> StringType = new TypeLiteral<>() {
    };
    public final static TypeLiteral<Attachment> AttachmentType = new TypeLiteral<>() {
    };
    public final static TypeLiteral<Integer> IntegerType = new TypeLiteral<>() {
    };
    public final static TypeLiteral<BigDecimal> BigDecimalType = new TypeLiteral<>() {
    };
    public final static TypeLiteral<LocalDate> DateType = new TypeLiteral<>() {
    };
    public final static TypeLiteral<LocalTime> TimeType = new TypeLiteral<>() {
    };
    public final static TypeLiteral<LocalDateTime> DateTimeType = new TypeLiteral<>() {
    };
    public final static TypeLiteral<DateRange> DateRangeType = new TypeLiteral<>() {
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

    protected ElementDefinition(final UIElementType uiElementType) {
        this.type = uiElementType;
    }

    public ElementDefinition() {
        this.type = null;
    }

    public static DataType mapDataType(String identifier) {
        return EnumUtils.valueById(DataType.class, identifier, DataType.Auto);
    }

    public static boolean isExpression(String h) {
        h = StringUtils.trim(h);
        return Strings.CS.startsWith(h, SpelEvaluator.START) && Strings.CS.endsWith(h, SpelEvaluator.END);
    }

    public static boolean isRegex(String h) {
        h = StringUtils.trim(h);
        return Strings.CS.startsWith(h, RegexEvaluator.START) && Strings.CS.endsWith(h, RegexEvaluator.END);
    }

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
     * @return
     */
    public final boolean hasOwnType() {
        return this.getType() == UIElementType.Form
                || this.getType() == UIElementType.Wizard
                || this.getType() == UIElementType.Dialog
                || this.getType() == UIElementType.SearchHelp
                || this.getType() == UIElementType.Table;
    }

    /**
     * @return
     */
    @JsonIgnore
    public final boolean isRootType() {
        return this.getType() == UIElementType.Form
                || this.getType() == UIElementType.Wizard;
    }

    /**
     * @return
     */
    public final boolean hasChildren() {
        return this.getType() == UIElementType.Form
                || this.getType() == UIElementType.Wizard
                || this.getType() == UIElementType.Dialog
                || this.getType() == UIElementType.Segment
                || this.getType() == UIElementType.Group
                || this.getType() == UIElementType.SearchHelp
                || this.getType() == UIElementType.Toolbar;
    }

    /**
     * @return
     */
    @JsonIgnore
    public final boolean isVisualDependent() {
        return this.getType() == UIElementType.Form
                || this.getType() == UIElementType.Wizard
                || this.getType() == UIElementType.Segment
                || this.getType() == UIElementType.Group
                || this.getType() == UIElementType.Toolbar;
    }

    /**
     * @return
     */
    @JsonIgnore
    public final boolean isCollection() {
        return this.getType() == UIElementType.Table;
    }

    /**
     * @return
     */
    public final boolean hasToolbar() {
        return this.getType() == UIElementType.Table || this.getType() == UIElementType.Dialog;
    }

    /**
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
