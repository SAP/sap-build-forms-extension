package com.sap.bfx.definition;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class CategoryOptions {
    String label;
    ValueHelpOption hvOpt;

    /**
     *
     */
    public CategoryOptions() {

    }

    /**
     * @param label
     * @param hvOpt
     */
    public CategoryOptions(final String label, final ValueHelpOption hvOpt) {
        this.setLabel(label);
        this.setHvOpt(hvOpt);
    }
}
