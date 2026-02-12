package com.sap.bfx.definition;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sap.bfx.callback.Context;
import com.sap.bfx.session.Attachment;
import com.sap.bfx.session.Table;

@Data
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class MinValidationRule extends AbstractValidationRule {
    private String limit;
    private boolean inclusive;

    @JsonIgnore
    private Class<?> dataTypeClass;
    @JsonIgnore
    private Object compiledLimit;

    public MinValidationRule() {
        super(ValidationRuleType.MIN);
    }

    /**
     *
     */
    @Override
    public void postLoad() {
        if (dataTypeClass == String.class || dataTypeClass == Table.class || dataTypeClass == Attachment.class
                || dataTypeClass == Integer.class) {
            try {
                compiledLimit = Integer.parseInt(limit);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing int min validation '" + limit + "'");
            }
        } else if (dataTypeClass == BigDecimal.class) {
            try {
                compiledLimit = new BigDecimal(limit);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing big-decimal min validation '" + limit + "'");
            }
        } else if (dataTypeClass == LocalDate.class) {
            try {
                compiledLimit = LocalDate.parse(limit);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing local-date min validation '" + limit + "'");
            }
        } else if (dataTypeClass == LocalTime.class) {
            try {
                compiledLimit = LocalTime.parse(limit);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing local-time min validation '" + limit + "'");
            }
        } else if (dataTypeClass == LocalDateTime.class) {
            try {
                compiledLimit = LocalDateTime.parse(limit);
            } catch (Exception e) {
                throw new RuntimeException("Error parsing local-date-time min validation '" + limit + "'");
            }
        } else {
            throw new RuntimeException("Min validation not applicable to data-type '" + dataTypeClass.getName() + "'");
        }
    }

    @Override
    public Optional<Message> validate(final String rowId, final String key, Context<?> context) {
        var value = context.getDataApi().getValue(rowId, key);

        if (value == null) {
            return Optional.of(new Message(this.severity, this.messageKey, null));
        } else if (value instanceof String) {
            var len = StringUtils.length((String) value);
            var limit = (int) compiledLimit;
//            log.debug("min-valudation-rule: Lenght is {} with limit {} and inclusive is {}", len, limit, inclusive);

            if ((inclusive && len < limit) || (!inclusive && len <= limit)) {
                return Optional.of(new Message(this.severity, this.messageKey, null));
            }
        } else if (value instanceof Integer) {
            var v = (Integer) value;
            var limit = (int) compiledLimit;

            if ((inclusive && v < limit) || (!inclusive && v <= limit)) {
                return Optional.of(new Message(this.severity, this.messageKey, null));
            }
        } else if (value instanceof Table) {
            var len = ((Table) value).getRows().size();
            var limit = (int) compiledLimit;

            if ((inclusive && len < limit) || (!inclusive && len <= limit)) {
                return Optional.of(new Message(this.severity, this.messageKey, null));
            }
        } else if (value instanceof Attachment) {
            // TODO(ML): Add here handling for attachments
        } else if (value instanceof BigDecimal) {
            var v = (BigDecimal) value;
            var limit = (BigDecimal) compiledLimit;

            if ((inclusive && v.compareTo(limit) == -1) || (!inclusive && v.compareTo(limit) <= 1)) {
                return Optional.of(new Message(this.severity, this.messageKey, null));
            }
        } else if (value instanceof LocalDate) {
            var v = (LocalDate) value;
            var limit = (LocalDate) compiledLimit;

            if ((inclusive && v.isBefore(limit)) || (!inclusive && !v.isAfter(limit))) {
                return Optional.of(new Message(this.severity, this.messageKey, null));
            }
        } else if (value instanceof LocalTime) {
            var v = (LocalTime) value;
            var limit = (LocalTime) compiledLimit;

            if ((inclusive && v.isBefore(limit)) || (!inclusive && !v.isAfter(limit))) {
                return Optional.of(new Message(this.severity, this.messageKey, null));
            }
        } else if (value instanceof LocalDateTime) {
            var v = (LocalDateTime) value;
            var limit = (LocalDateTime) compiledLimit;

            if ((inclusive && v.isBefore(limit)) || (!inclusive && !v.isAfter(limit))) {
                return Optional.of(new Message(this.severity, this.messageKey, null));
            }
        } else {
            throw new RuntimeException("unknown element type of class: '" + value.getClass().getName() + "'");
        }

        return Optional.empty();
    }
}
