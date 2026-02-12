package com.sap.bfx.session;

import com.sap.bfx.definition.LogEntry;

import java.util.List;

/**
 * Data Access Object (DAO) interface for managing log entries in the database.
 */
public interface LogEntryDao {

    /**
     * Inserts a log entry into the database.
     *
     * @param logEntryEntity The log entry to be inserted.
     */
    void insert(final LogEntry logEntryEntity);

    /**
     * Retrieves all log entries associated with a specific form ID.
     *
     * @param formId The ID of the form whose log entries are to be retrieved.
     * @return A list of log entries associated with the specified form ID.
     */
    List<LogEntry> getByFormId(final String formId);
}
