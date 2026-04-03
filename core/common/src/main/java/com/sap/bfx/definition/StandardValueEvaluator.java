package com.sap.bfx.definition;

import com.sap.bfx.callback.AccessClass;
import com.sap.bfx.callback.Context;
import com.sap.bfx.session.Attachments;
import com.sap.bfx.session.DocFormData;
import com.sap.bfx.session.MoneyAmount;
import com.sap.bfx.session.Table;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.format.DateTimeParseException;

@Slf4j
public class StandardValueEvaluator implements Evaluator<Object> {

    private final ElementDefinition ed;

    public StandardValueEvaluator(final ElementDefinition ed) {
        this.ed = ed;
    }

    @Override
    public Object eval(Context<? extends AccessClass> ctx, boolean isInitial, Object defaultValue) {
        if (ed.isCollection()) {
//            log.debug("Form.createAndAddElement: Adding element '{}' with type ElementCollection",
//                    ed.getName());
            return getDefaultTableValue();
        }

        final var dt = ElementDefinition.getDataTypeClass(ed);
        if (dt == Integer.class) {
            return this.getDefaultIntegerValue(ed.getDefaultValue());
        } else if (dt == LocalDate.class) {
            return this.getDefaultDateValue(ed.getDefaultValue());
        } else if (dt == LocalDateTime.class) {
            return this.getDefaultDateTimeValue(ed.getDefaultValue());
        } else if (dt == LocalTime.class) {
            return this.getDefaultTimeValue(ed.getDefaultValue());
        } else if (dt == BigDecimal.class) {
            return this.getDefaultBigDecimalValue(ed.getDefaultValue());
        } else if (dt == String.class) {
            return this.getDefaultStringValue(ed.getDefaultValue());
        } else if (dt == Attachments.class) {
            return this.getDefaultAttachmentsValue(ed.getDefaultValue());
        } else if (dt == DateRange.class) {
            return this.getDefaultDateRangeValue(ed.getDefaultValue());
        } else if (dt == LinkData.class) {
            return this.getDefaultLinkDataValue(ed.getDefaultValue());
        } else if (dt == DocFormData.class) {
            return this.getDefaultDocFormDataValue(ed.getDefaultValue());
        } else if (dt == MoneyAmount.class) {
            return this.getDefaultMoneyAmountValue(ed.getDefaultValue());
        }
        return StringUtils.isNotBlank(ed.getDefaultValue())
                && Boolean.parseBoolean(ed.getDefaultValue());
    }

    /**
     * @return
     */
    private Table getDefaultTableValue() {
        final var table = new Table((TableElementDefinition) ed);
        return table;
    }

    /**
     * @param def
     * @return
     */
    private String getDefaultStringValue(final String def) {
        return StringUtils.trimToEmpty(def);
    }

    /**
     * @param def
     * @return
     */
    private Integer getDefaultIntegerValue(final String def) {
        if (StringUtils.isBlank(def)) {
            return 0;
        }
        return Integer.parseInt(def);
    }

    /**
     * @param def
     * @return
     */
    private BigDecimal getDefaultBigDecimalValue(final String def) {
        if (StringUtils.isBlank(def)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(def);
    }

    /**
     * @param def
     * @return
     */
    private LocalDate getDefaultDateValue(final String def) {
        if (StringUtils.isBlank(def)) {
            return null;
        }

        try {
            return LocalDate.parse(def);
        } catch (DateTimeParseException e) {
            log.error("Error parsing default value '{" + def + "}' to date.", e);
            return LocalDate.of(1970, Month.JANUARY, 1);
        }
    }

    /**
     * @param def
     * @return
     */
    private LocalTime getDefaultTimeValue(final String def) {
        if (StringUtils.isBlank(def)) {
            return null;
        }

        try {
            return LocalTime.parse(def);
        } catch (DateTimeParseException e) {
            log.error("Error parsing default value '{" + def + "}' to time", e);
            return LocalTime.of(0, 0);
        }
    }

    /**
     * @param def
     * @return
     */
    private LocalDateTime getDefaultDateTimeValue(final String def) {
        if (StringUtils.isBlank(def)) {
            return null;
        }

        try {
            return LocalDateTime.parse(def);
        } catch (DateTimeParseException e) {
            log.error("Error parsing default value '{" + def + "}' to datetime.", e);
            return LocalDateTime.of(1970, Month.JANUARY, 1, 0, 0);
        }
    }

    /**
     * @param def
     * @return
     */
    private Attachments getDefaultAttachmentsValue(final String def) {
        return new Attachments();
    }

    /**
     * @param def
     * @return
     */
    private DateRange getDefaultDateRangeValue(final String def) {
        if (StringUtils.isBlank(def)) {
            return null;
        }

        try {
            final var parts = StringUtils.split(def, "-");
            if (parts.length == 2) {
                return new DateRange(StringUtils.trim(parts[0]), StringUtils.trim(parts[1]));
            }
        } catch (DateTimeParseException e) {
            log.error("Error parsing default value '{" + def + "}' to date.", e);
        }

        return null;
    }

    /**
     * @param def default value string
     * @return LinkData
     */
    private LinkData getDefaultLinkDataValue(final String def) {
        if (ed instanceof LinkElementDefinition) {
            LinkData configuredLinkData = ((LinkElementDefinition) ed).getLinkData();
            if (configuredLinkData != null &&
                (StringUtils.isNotBlank(configuredLinkData.getText()) ||
                 StringUtils.isNotBlank(configuredLinkData.getHRef()))) {
                return new LinkData(
                    configuredLinkData.getText(),
                    configuredLinkData.getHRef()
                );
            }
        }

        if (StringUtils.isBlank(def)) {
            return new LinkData();
        }

        final var linkData = new LinkData();
        linkData.setHRef(StringUtils.trim(def));
        return linkData;
    }

    /**
     *
     * @param def
     * @return
     */
    private DocFormData getDefaultDocFormDataValue(final String def) {
        return new DocFormData();
    }

    /**
     *
     * @param def
     * @return
     */
    private MoneyAmount getDefaultMoneyAmountValue(final String def) {
        final var r = new MoneyAmount();

        if (StringUtils.isNotBlank(def)) {
            if (StringUtils.contains(def, ' ')) {
                final var parts = StringUtils.split(def, ' ');
                if (parts.length == 2) {
                    r.setCurrency(StringUtils.trim(parts[0]));
                    r.setAmount(new BigDecimal(StringUtils.trim(parts[1])));
                    return r;
                }
            }
            log.error("Error parsing default value '{" + def + "}' to currency-amount.");
        }

        return r;
    }
}
