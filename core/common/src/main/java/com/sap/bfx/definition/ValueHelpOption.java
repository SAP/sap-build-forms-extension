package com.sap.bfx.definition;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class ValueHelpOption {

    private String name;
    private boolean validate;
    private boolean emptySelection;
    private String displayFormat;

    /**
     *
     */
    public ValueHelpOption() {

    }

    /**
     * @param name
     * @param validate
     * @param emptySelection
     * @param displayFormat
     */
    public ValueHelpOption(final String name,
                           final boolean validate,
                           final boolean emptySelection,
                           final String displayFormat) {
        this.name = name;
        this.validate = validate;
        this.emptySelection = emptySelection;
        this.displayFormat = displayFormat;
    }
}
