package com.sap.bfx.callback;

/**
 * Service to access configuration parameters.
 */
public interface ConfigurationService {

    /**
     * Get the destination name for workflow inbox principal propagation.
     *
     * @return the destination name
     */
    String getWorkflowInboxPrincipalPropagationDestinationName();

    /**
     * Get the destination name for workflow runtime principal propagation.
     *
     * @return the destination name
     */
    String getWorkflowRuntimePrincipalPropagationDestinationName();

    /**
     * Get the destination name for workflow runtime technical user.
     *
     * @return the destination name
     */
    String getWorkflowRuntimeTechnicalDestinationName();

}
