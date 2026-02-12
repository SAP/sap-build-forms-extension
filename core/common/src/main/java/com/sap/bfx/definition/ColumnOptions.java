package com.sap.bfx.definition;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ColumnOptions {
    String minColumnWidth;
    String maxColumnWidth;

    /**
     *
     */
    public ColumnOptions() {

    }

    /**
     * @param minColumnWidth
     * @param maxColumnWidth
     */
    public ColumnOptions(final String minColumnWidth, final String maxColumnWidth) {
        this.setMinColumnWidth(minColumnWidth);
        this.setMaxColumnWidth(maxColumnWidth);
    }
}
