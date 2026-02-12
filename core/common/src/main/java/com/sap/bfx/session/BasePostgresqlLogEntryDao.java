package com.sap.bfx.session;

import com.sap.bfx.definition.LogEntry;
import com.sap.bfx.definition.Severity;
import com.sap.bfx.utils.EnumUtils;
import com.sap.bfx.utils.JdbcUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public abstract class BasePostgresqlLogEntryDao implements LogEntryDao {
    protected final JdbcTemplate jdbc;

    /**
     * Constructor
     *
     * @param ds The DataSource for database connections.
     */
    protected BasePostgresqlLogEntryDao(final DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }

    /**
     * @param logEntryEntity The log entry to be inserted.
     */
    @Override
    public void insert(LogEntry logEntryEntity) {

        jdbc.update(con -> {
            final var ps = con.prepareStatement(
                    "INSERT INTO forms_log_entries " +
                            "(id, form_id, severity, ts, user_nm, message_id, message_text, message_data, action) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?)"
            );
            ps.setString(1, logEntryEntity.getId());
            ps.setString(2, logEntryEntity.getFormId());
            ps.setString(3, logEntryEntity.getSeverity().getIdentifier());
            ps.setObject(4, logEntryEntity.getTimestamp());
            ps.setString(5, logEntryEntity.getUser());
            ps.setString(6, logEntryEntity.getMessageId());
            ps.setString(7, logEntryEntity.getMessageText());
            ps.setObject(8, logEntryEntity.getMessageData() != null
                    ? logEntryEntity.getMessageData().toString()
                    : null);
            ps.setString(9, logEntryEntity.getAction().getIdentifier());
            return ps;
        });
    }

    /**
     * @param formId The ID of the form whose log entries are to be retrieved.
     * @return
     */
    @Override
    public List<LogEntry> getByFormId(String formId) {
        return jdbc.query(con -> {
            final var ps = con.prepareStatement("SELECT * FROM forms_logs WHERE form_id = ? ORDER BY ts DESC");
            ps.setString(1, formId);
            return ps;
        }, new LogEntryRowMapper());
    }

    /**
     * RowMapper implementation for mapping ResultSet rows to LogEntry objects.
     */
    protected static class LogEntryRowMapper implements RowMapper<LogEntry> {
        @Override
        public LogEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            final var entry = new LogEntry();
            entry.setId(rs.getString("id"));
            entry.setFormId(rs.getString("form_id"));
            entry.setSeverity(EnumUtils.valueById(Severity.class, rs.getString("severity"), Severity.None));
            entry.setTimestamp(JdbcUtils.fromResultSetToInstant(rs, "ts"));
            entry.setUser(rs.getString("user_nm"));
            entry.setMessageId(rs.getString("message_id"));
            entry.setMessageText(rs.getString("message_text"));
            entry.setMessageData(rs.getObject("message_data"));
            entry.setAction(EnumUtils.valueById(LogEntry.Action.class, rs.getString("action"),
                    LogEntry.Action.Info));

            return entry;
        }
    }
}