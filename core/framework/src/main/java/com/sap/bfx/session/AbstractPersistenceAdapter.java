package com.sap.bfx.session;

import com.sap.bfx.callback.PersistenceAdapter;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Abstract base class for persistence adapters that provides access to a JdbcTemplate for database operations.
 * Subclasses should implement the specific persistence logic for their respective database systems.
 */
public abstract class AbstractPersistenceAdapter implements PersistenceAdapter {
    protected final JdbcTemplate jdbc;

    /**
     * Constructor
     *
     * @param jdbc The JdbcTemplate to be used to access the database. It is expected that the JdbcTemplate is
     *             configured with a DataSource that has auto-commit set to false.
     */
    protected AbstractPersistenceAdapter(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
}
