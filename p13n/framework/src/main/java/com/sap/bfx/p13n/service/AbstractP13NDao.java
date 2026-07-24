package com.sap.bfx.p13n.service;

import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Basic implementation of a data access object for P13N service
 */
public abstract class AbstractP13NDao implements P13NDao {
    protected final JdbcTemplate jdbc;

    /**
     * Creates a new instance of AbstractCoreDao with the given DataSource.
     *
     * @param ds the DataSource to be used for database access
     */
    protected AbstractP13NDao(final DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }
}
