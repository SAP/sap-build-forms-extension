package com.sap.bfx.callback;

/**
 * Optional Spring component that scenario apps implement to return the scenario variant
 * label shown on the form page (e.g. {@code "REQUEST"}).
 * <p>
 * The display variant represents the logical variant of a scenario — not the generic
 * {@code ProcessState} (Draft/Running/…) and not the {@code detailState} workflow step.
 * When no bean is present the framework returns {@code null} for the display variant.
 */
public interface DisplayVariantService {

    /**
     * Return the scenario variant label for the given process (e.g. {@code "REQUEST"}).
     * <p>
     * When no bean is registered the framework returns {@code null} to the caller.
     * Implementations should return a non-null value; returning {@code null} is treated
     * the same as no bean being present.
     *
     * @param formsProcessId the ID of the process
     * @param user           the requesting user
     * @param language       the requested language/locale (e.g. {@code "en"}, {@code "de"})
     * @return the scenario variant label, or {@code null} to let the caller use its default
     */
    String getDisplayVariant(String formsProcessId, String user, String language);
}
