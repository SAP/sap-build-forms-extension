package com.sap.bfx.standard;

import com.sap.bfx.definition.DateRange;
import com.sap.bfx.definition.LinkData;
import com.sap.bfx.session.SortOrder;
import com.sap.bfx.session.Table;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;

/**
 * Default implementation of a table comparator.
 * This comparator does not perform any actual comparison and always returns 0.
 * It can be used as a placeholder or default comparator in scenarios where no specific sorting is required.
 */
public class StandardTableComparator implements Comparator<String> {
    final private Table table;

    /**
     * @param table
     */
    public StandardTableComparator(final Table table) {
        this.table = table;
    }

    /**
     * Compares two objects of type String.
     *
     * @param rowId1 the first object to be compared.
     * @param rowId2 the second object to be compared.
     * @return
     */
    @Override
    public int compare(String rowId1, String rowId2) {
        final var row1 = table.getData().get(rowId1);
        final var row2 = table.getData().get(rowId2);

        final var value1 = row1.getElements().get(table.getSortField()).getValue();
        final var value2 = row2.getElements().get(table.getSortField()).getValue();

        var result = 0;
        if (value1 instanceof LocalDate) {
            result = ((LocalDate) value1).compareTo((LocalDate) value2);
        } else if (value1 instanceof LocalDateTime) {
            result = ((LocalDateTime) value1).compareTo((LocalDateTime) value2);
        } else if (value1 instanceof LocalTime) {
            result = ((LocalTime) value1).compareTo((LocalTime) value2);
        } else if (value1 instanceof BigDecimal) {
            result = ((BigDecimal) value1).compareTo((BigDecimal) value2);
        } else if (value1 instanceof String) {
            result = StringUtils.compare((String) value1, (String) value2);
        } else if (value1 instanceof Integer) {
            result = Integer.compare((Integer) value1, (Integer) value2);
        } else if (value1 instanceof DateRange) {
            result = (((DateRange) value1).getFrom()).compareTo(((DateRange) value2).getFrom());
        } else if (value1 instanceof LinkData) {
            result = ((LinkData) value1).getHRef().compareTo(((LinkData) value2).getHRef());
        }
        // all other (table, attachment aren't comparable, we leave the default 0

        if (table.getSortOrder().equals(SortOrder.DESCENDING)) {
            result = -1 * result;
        }

        return result;
    }
}
