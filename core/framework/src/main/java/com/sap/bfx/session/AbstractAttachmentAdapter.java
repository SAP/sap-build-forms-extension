package com.sap.bfx.session;

import com.sap.bfx.callback.AttachmentAdapter;
import org.springframework.jdbc.core.JdbcTemplate;

public abstract class AbstractAttachmentAdapter implements AttachmentAdapter {
    protected final JdbcTemplate jdbc;

    /**
     * Constructor
     *
     * @param jdbc The JdbcTemplate to be used to access the database. It is expected that the JdbcTemplate is
     *             configured with a DataSource that has auto-commit set to false.
     */
    protected AbstractAttachmentAdapter(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }
}
