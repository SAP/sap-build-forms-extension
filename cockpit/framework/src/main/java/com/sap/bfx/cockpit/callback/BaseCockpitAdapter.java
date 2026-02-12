package com.sap.bfx.cockpit.callback;

import com.sap.bfx.cockpit.service.ProcessAbstract;

import java.util.List;

/**
 * Base implementation of the CockpitAdapter interface.
 * Provides default (null) implementations for the methods.
 */
public class BaseCockpitAdapter implements CockpitAdapter {
    /**
     * @param settings the data structure to be filled
     * @param params   Frontend parameters
     */
    @Override
    public void init(FrontendSettings settings, FrontendParams params) {

    }

    /**
     * @param processes List of process instance attributes for querying
     * @param params    Search params from the frontend
     */
    @Override
    public void findProcesses(List<ProcessAbstract> processes, SearchParams params) {

    }
}
