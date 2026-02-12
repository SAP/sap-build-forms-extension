package com.sap.bfx.definition;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class WizardFormatOptions {

    private boolean skipInSummary;
    private boolean skipInForm;

    /**
     *
     */
    public WizardFormatOptions() {

    }

    /**
     * @param skipInSummary
     * @param skipInForm
     */
    public WizardFormatOptions(final boolean skipInSummary, final boolean skipInForm) {
        this.skipInSummary = skipInSummary;
        this.skipInForm = skipInForm;
    }
}
