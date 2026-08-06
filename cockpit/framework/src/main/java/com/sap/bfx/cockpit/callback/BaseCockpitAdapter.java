package com.sap.bfx.cockpit.callback;

import com.sap.bfx.cockpit.service.ProcessAbstract;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

/**
 * Base implementation of the CockpitAdapter interface.
 * Provides default (null) implementations for the methods.
 */
public class BaseCockpitAdapter implements CockpitAdapter {
    protected final JdbcTemplate jdbc;

    /**
     * Constructor
     *
     * @param ds Datatsource to be used
     */
    protected BaseCockpitAdapter(final DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }

    /**
     * Initializes the adapter with the provided settings and parameters.
     *
     * @param settings the data structure to be filled
     * @param params   Frontend parameters
     */
    @Override
    public void init(FrontendSettings settings, FrontendParams params) {
    }

    /**
     * Finds processes based on the provided list of process instance attributes and search parameters.
     *
     * @param processes List of process instance attributes for querying
     * @param params    Search params from the frontend
     */
    @Override
    public void findProcesses(List<ProcessAbstract> processes, SearchParams params) {
    }
}
