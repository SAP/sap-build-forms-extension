package com.sap.bfx.callback;

/**
 * Optional Spring component that scenario apps implement to support creating a new process
 * from an existing one as a template.
 * <p>
 * When the framework finds a bean of this type it delegates template creation to the bean.
 * If no bean is present, the endpoint returns HTTP 501.
 */
public interface UseAsTemplateService {

    /**
     * Result of creating a new process from a template, carrying the new process ID and
     * the display state the cockpit should open it in.
     */
    record TemplateResult(String newProcessId, String variant) {}

    /**
     * Create a new process from the given source process used as a template.
     *
     * @param formsProcessId the ID of the existing process to use as template
     * @param user           the user requesting the template creation
     * @return a {@link TemplateResult} containing the new process ID and the state to open it in
     */
    TemplateResult createFromTemplate(String formsProcessId, String user);
}
