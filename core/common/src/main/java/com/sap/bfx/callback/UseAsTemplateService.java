package com.sap.bfx.callback;

/**
 * Optional Spring component that scenario apps implement to support creating a new process
 * from an existing one as a template.
 * <p>
 * Implement {@link #isTemplatable} to control whether the "save as template" button is shown
 * in the cockpit detail view. Implement {@link #createFromTemplate} to perform the actual
 * template creation.
 * <p>
 * If no bean is present: the templatable check returns {@code false} and the template endpoint
 * returns HTTP 501.
 */
public interface UseAsTemplateService {

    /**
     * Result of creating a new process from a template, carrying the new process ID and
     * the display state the cockpit should open it in.
     */
    record TemplateResult(String newProcessId, String variant) {}

    /**
     * Check whether the given process can currently be used as a template by the given user.
     * Called by the cockpit to decide whether to show the "save as template" button.
     *
     * @param formsProcessId the ID of the process
     * @param user           the user requesting the check
     * @return {@code true} if the template action should be shown; {@code false} otherwise
     */
    boolean isTemplatable(String formsProcessId, String user);

    /**
     * Create a new process from the given source process used as a template.
     *
     * @param formsProcessId the ID of the existing process to use as template
     * @param user           the user requesting the template creation
     * @return a {@link TemplateResult} containing the new process ID and the state to open it in
     */
    TemplateResult createFromTemplate(String formsProcessId, String user);
}
