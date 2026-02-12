package com.sap.bfx.cockpit.callback;

import com.sap.bfx.callback.Adapter;
import com.sap.bfx.cockpit.service.ProcessAbstract;

import java.util.List;

/**
 * Adapter interface for Cockpit operations.
 */
public interface CockpitAdapter extends Adapter {

    /**
     * Initialize frontend settings based on provided parameters.
     *
     * @param settings the data structure to be filled
     * @param params   Frontend parameters
     */
    void init(final FrontendSettings settings, final FrontendParams params);

    /**
     * Query process instances based on provided attributes and locale.
     *
     * @param processes List of process instance attributes for querying
     * @param params    Search params from the frontend
     */
    void findProcesses(final List<ProcessAbstract> processes, SearchParams params);
}