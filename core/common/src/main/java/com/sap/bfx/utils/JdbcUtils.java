package com.sap.bfx.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

/**
 * JDBC related utility methods
 */
public final class JdbcUtils {

    private JdbcUtils() {
    }

    /**
     * Convert SQL Timestamp to Instant
     *
     * @param rs
     * @param columnLabel
     * @return
     * @throws SQLException
     */
    public static Instant fromResultSetToInstant(ResultSet rs, String columnLabel) throws SQLException {
        final var timestamp = rs.getTimestamp(columnLabel);
        return timestamp != null ? timestamp.toInstant() : null;
    }
}
