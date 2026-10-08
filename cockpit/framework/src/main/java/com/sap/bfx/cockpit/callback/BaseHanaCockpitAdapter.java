package com.sap.bfx.cockpit.callback;

import com.sap.bfx.cockpit.service.FeedEntry;
import com.sap.bfx.cockpit.service.ProcessAbstract;
import com.sap.bfx.callback.FeedTypeConverter;
import com.sap.bfx.definition.ProcessState;
import com.sap.bfx.utils.EnumUtils;
import com.sap.bfx.utils.JdbcUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

/**
 * Base implementation of the cockpit database adapter for HANA
 */
@Slf4j
public class BaseHanaCockpitAdapter extends BaseCockpitAdapter {

    /**
     * Constructor
     *
     * @param ds Datatsource to be used
     */
    protected BaseHanaCockpitAdapter(final DataSource ds) {
        super(ds);
    }

    /**
     * @param processes List of process instance attributes for querying
     * @param sp        Search params from the frontend
     * @return total count of matching processes (before paging)
     */
    @Override
    public int findProcesses(List<ProcessAbstract> processes, SearchParams sp) {

        final var baseCondition = new StringBuilder(" FROM forms_data.forms_forms WHERE 1=1");
        final var params = new ArrayList<>();

        if (sp.getDescriptionValue() != null && sp.getDescriptionValue().length > 0) {
            appendStringFilter(baseCondition, params, "description", sp.getDescriptionType(), sp.getDescriptionValue());
        }

        if (sp.getFunctionalIdValue() != null && sp.getFunctionalIdValue().length > 0) {
            appendStringFilter(baseCondition, params, "functional_id", sp.getFunctionalIdType(), sp.getFunctionalIdValue());
        }

        if (sp.getAdditionalInformationValue() != null && sp.getAdditionalInformationValue().length > 0) {
            appendStringFilter(baseCondition, params, "additional_information", sp.getAdditionalInformationType(), sp.getAdditionalInformationValue());
        }

        if (sp.getStatus() != null && sp.getStatus().length > 0) {
            final var placeholders = "?,".repeat(sp.getStatus().length);
            baseCondition.append(" AND state IN (").append(placeholders, 0, placeholders.length() - 1).append(")");
            params.addAll(Arrays.asList(sp.getStatus()));
        }

        if (sp.getUser() != null) {
            baseCondition.append(" AND started_by = ?");
            params.add(sp.getUser());
        }

        // Date format must match the value produced by the UI5 DateRangePicker (e.g. "Sep 1, 2024 - Sep 30, 2024")
        final var dateFormat = new SimpleDateFormat("MMM d, yyyy", java.util.Locale.ENGLISH);

        if (sp.getStartedBy() != null) {
            final var dates = sp.getStartedBy().split(" - ");
            if (dates.length == 2) {
                try {
                    final var startDate = new Timestamp(dateFormat.parse(dates[0]).getTime());
                    final var cal = Calendar.getInstance();
                    cal.setTime(dateFormat.parse(dates[1]));
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                    baseCondition.append(" AND started_at BETWEEN ? AND ?");
                    params.add(startDate);
                    params.add(new Timestamp(cal.getTimeInMillis()));
                } catch (ParseException e) {
                    log.warn("Cannot parse startedBy date range: {}", sp.getStartedBy(), e);
                }
            }
        }

        if (sp.getEndedOn() != null) {
            final var dates = sp.getEndedOn().split(" - ");
            if (dates.length == 2) {
                try {
                    final var startDate = new Timestamp(dateFormat.parse(dates[0]).getTime());
                    final var cal = Calendar.getInstance();
                    cal.setTime(dateFormat.parse(dates[1]));
                    cal.set(Calendar.HOUR_OF_DAY, 23);
                    cal.set(Calendar.MINUTE, 59);
                    cal.set(Calendar.SECOND, 59);
                    cal.set(Calendar.MILLISECOND, 999);
                    baseCondition.append(" AND finished_at BETWEEN ? AND ?");
                    params.add(startDate);
                    params.add(new Timestamp(cal.getTimeInMillis()));
                } catch (ParseException e) {
                    log.warn("Cannot parse endedOn date range: {}", sp.getEndedOn(), e);
                }
            }
        }

        if (sp.getScenario() != null) {
            baseCondition.append(" AND scenario_nm = ?");
            params.add(sp.getScenario());
        }

        // Count total matching rows
        final var countSql = "SELECT COUNT(*)" + baseCondition;
        final var totalCount = jdbc.query(con -> {
            PreparedStatement ps = con.prepareStatement(countSql);
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return ps;
        }, rs -> rs.next() ? rs.getInt(1) : 0);

        // Fetch the requested page — HANA supports LIMIT/OFFSET
        final int pageSize = sp.getPageSize() > 0 ? sp.getPageSize() : 10;
        final int offset = (Math.max(sp.getPage(), 1) - 1) * pageSize;
        final var selectSql = "SELECT *" + baseCondition
                + " ORDER BY started_at DESC, CAST(description AS NVARCHAR(5000))"
                + " LIMIT ? OFFSET ?";

        processes.addAll(jdbc.query(con -> {
            PreparedStatement ps = con.prepareStatement(selectSql);
            int idx = 1;
            for (Object p : params) {
                ps.setObject(idx++, p);
            }
            ps.setObject(idx++, pageSize);
            ps.setObject(idx, offset);
            return ps;
        }, new FormRowMapper()));

        return totalCount != null ? totalCount : 0;
    }

    @Override
    public List<String> findSuggestions(String column, String search) {
        validateColumn(column);
        final String sql = "SELECT DISTINCT CAST(" + column + " AS NVARCHAR(5000))"
                + " FROM forms_data.forms_forms"
                + " WHERE " + column + " IS NOT NULL AND " + column + " <> ''"
                + " AND LOWER(CAST(" + column + " AS NVARCHAR(5000))) LIKE LOWER(CONCAT('%',?,'%'))"
                + " ORDER BY CAST(" + column + " AS NVARCHAR(5000))"
                + " LIMIT 100";
        return jdbc.queryForList(sql, String.class, search == null ? "" : search);
    }

    /**
     * Maps a SQL result row to a Form object.
     */
    private static class FormRowMapper implements RowMapper<ProcessAbstract> {
        @Override
        public ProcessAbstract mapRow(ResultSet rs, int rowNum) throws SQLException {
            final ProcessAbstract form = new ProcessAbstract();
            form.setChangedAt(JdbcUtils.fromResultSetToInstant(rs, "ts"));
            form.setChangedBy(rs.getString("user_nm"));
            form.setDescription(rs.getString("description"));
            form.setDetailState(rs.getString("detail_state"));
            form.setFinishedAt(JdbcUtils.fromResultSetToInstant(rs, "finished_at"));
            form.setFunctionalId(rs.getString("functional_id"));
            form.setId(rs.getString("id"));
            form.setRefId(rs.getString("ref_id"));
            form.setScenarioName(rs.getString("scenario_nm"));
            form.setScenarioVersion(rs.getInt("scneario_ver"));
            form.setStartedAt(JdbcUtils.fromResultSetToInstant(rs, "started_at"));
            form.setStartedBy(rs.getString("started_by"));
            form.setState(EnumUtils.valueById(ProcessState.class, StringUtils.trim(rs.getString("state")),
                    ProcessState.Draft));
            form.setTemplateName(rs.getString("template_nm"));
            form.setVersion(rs.getLong("version"));
            form.setWorkflowAdapter(rs.getString("wf_adapter"));

            return form;
        }
    }

    @Override
    public List<FeedEntry> findFeeds(String formId) {
        return jdbc.query(
                "SELECT id, parent_id, user_nm, type, pos, form_id, ts, \"text\""
                        + " FROM forms_data.forms_feeds WHERE form_id = ? ORDER BY pos ASC",
                new FeedRowMapper(), formId);
    }

    @Override
    public FeedEntry addFeed(String formId, String userNm, String text, String type, String parentId) {
        final int nextPos;
        if (parentId == null) {
            final Integer max = jdbc.queryForObject(
                    "SELECT MAX(pos) FROM forms_data.forms_feeds WHERE form_id = ? AND parent_id IS NULL",
                    Integer.class, formId);
            nextPos = (max != null ? max : 0) + 1;
        } else {
            final Integer max = jdbc.queryForObject(
                    "SELECT MAX(pos) FROM forms_data.forms_feeds WHERE form_id = ? AND parent_id = ?",
                    Integer.class, formId, parentId);
            nextPos = (max != null ? max : 0) + 1;
        }

        final String id = UUID.randomUUID().toString();
        final byte[] textBytes = text.getBytes(StandardCharsets.UTF_8);
        jdbc.update(con -> {
            final PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO forms_data.forms_feeds (id, parent_id, user_nm, type, pos, form_id, ts, \"text\")"
                            + " VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, ?)");
            ps.setString(1, id);
            ps.setString(2, parentId);
            ps.setString(3, userNm);
            ps.setString(4, FeedTypeConverter.toChar(type));
            ps.setInt(5, nextPos);
            ps.setString(6, formId);
            ps.setBlob(7, new ByteArrayInputStream(textBytes), textBytes.length);
            return ps;
        });

        final FeedEntry entry = new FeedEntry();
        entry.setId(id);
        entry.setParentId(parentId);
        entry.setFormId(formId);
        entry.setUserNm(userNm);
        entry.setType(type);
        entry.setPos(nextPos);
        entry.setTs(java.time.Instant.now());
        entry.setText(text);
        return entry;
    }

    /**
     * Maps a SQL result row to a FeedEntry object.
     */
    private static class FeedRowMapper implements RowMapper<FeedEntry> {
        @Override
        public FeedEntry mapRow(ResultSet rs, int rowNum) throws SQLException {
            final FeedEntry entry = new FeedEntry();
            entry.setId(rs.getString("id"));
            entry.setParentId(rs.getString("parent_id"));
            entry.setFormId(rs.getString("form_id"));
            entry.setUserNm(rs.getString("user_nm"));
            entry.setType(FeedTypeConverter.fromChar(rs.getString("type")));
            entry.setPos(rs.getInt("pos"));
            entry.setTs(JdbcUtils.fromResultSetToInstant(rs, "ts"));
            final java.sql.Blob blob = rs.getBlob("text");
            if (blob != null) {
                try {
                    entry.setText(new String(blob.getBinaryStream().readAllBytes(), StandardCharsets.UTF_8));
                } catch (java.io.IOException e) {
                    throw new SQLException("Failed to read text blob", e);
                }
            }
            return entry;
        }
    }

}

