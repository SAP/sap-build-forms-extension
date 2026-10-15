package com.sap.bfx.callback;

/**
 * Optional Spring component that scenario apps implement to support process cancellation.
 * <p>
 * Implement {@link #isCancelable} to control whether the cancel button is shown in the cockpit
 * detail view. Implement {@link #cancel} to perform the actual cancellation.
 * <p>
 * If no bean is present: the cancelable check returns {@code false} and the cancel endpoint
 * returns HTTP 501.
 */
public interface CancellationService {

    /**
     * Check whether the given process can currently be cancelled by the given user.
     * Called by the cockpit to decide whether to show the cancel button.
     *
     * @param formsProcessId the ID of the process
     * @param user           the user requesting the check
     * @return {@code true} if the cancel action should be shown; {@code false} otherwise
     */
    boolean isCancelable(String formsProcessId, String user);

    /**
     * Perform the full cancellation of the process: set the appropriate state and save.
     *
     * @param formsProcessId the ID of the process to cancel
     * @param user           the user performing the cancellation
     */
    void cancel(String formsProcessId, String user);
}
