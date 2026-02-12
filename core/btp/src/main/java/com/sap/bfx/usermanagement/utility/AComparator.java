package com.sap.bfx.usermanagement.utility;

import lombok.Getter;

import java.util.Comparator;

public abstract class AComparator<T> implements Comparator<T> {

    private final SortOrder sortOrder;

    public AComparator(SortOrder sortOrder) {
        super();
        this.sortOrder = sortOrder != null ? sortOrder : SortOrder.ASC;
    }

    public int compare(T o1, T o2) {
        int result = 0;
        if (o1 == null) {
            if (o2 == null) {
                result = 0;
            } else {
                result = -1;
            }
        } else if (o2 == null) {
            result = 1;
        } else {
            result = internalCompare(o1, o2);
        }
        return result * sortOrder.getMultiplier();
    }

    /**
     * Vergleicht die Objekte o1 und o2 - null-Checks wurden bereits zuvor
     * durchgeführt.
     *
     * @param o1 Objekt 1 - nie null
     * @param o2 Objekt 2 - nie null
     * @return int-Wert analog zu {@link Comparator#compare(Object, Object)}
     */

    protected abstract int internalCompare(T o1, T o2);

    @Getter
    public enum SortOrder {
        ASC(1), DESC(-1);

        private final int multiplier;

        private SortOrder(int multiplier) {
            this.multiplier = multiplier;
        }

    }
}
