package com.sap.bfx.callback;

/**
 * Optional Spring component that scenario apps implement to support process cancellation.
 * <p>
 * When the framework finds a bean of this type it delegates cancellation entirely to the bean,
 * including setting the process state and saving. If no bean is present the endpoint returns HTTP 501.
 */
public interface CancellationService {

    /**
     * Perform the full cancellation of the process: set the appropriate state and save.
     *
     * @param formsProcessId the ID of the process to cancel
     * @param user           the user performing the cancellation
     */
    void cancel(String formsProcessId, String user);
}
