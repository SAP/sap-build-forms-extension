package com.sap.bfx.cockpit.callback;

import com.sap.bfx.cockpit.service.ProcessAbstract;
import com.sap.bfx.definition.ProcessState;
import com.sap.bfx.utils.EnumUtils;
import com.sap.bfx.utils.JdbcUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
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

/**
 * Base implementation of the cockpit database adapter for PostgreSQL
 */
@Slf4j
public class BasePostgresqlCockpitAdapter extends BaseCockpitAdapter {

    /**
     * Constructor
     *
     * @param ds Datatsource to be used
     */
    protected BasePostgresqlCockpitAdapter(final DataSource ds) {
        super(ds);
    }

    /**
     * @param processes List of process instance attributes for querying
     * @param sp        Search params from the frontend
     * @return total count of matching processes (before paging)
     */
    @Override
    public int findProcesses(List<ProcessAbstract> processes, SearchParams sp) {

        final var baseCondition = new StringBuilder(" FROM forms_forms WHERE 1=1");
        final var params = new ArrayList<>();

        if ("contains".equals(sp.getDescriptionType()) && sp.getDescriptionValue() != null && sp.getDescriptionValue().length > 0) {
            if (sp.getDescriptionValue().length == 1) {
                baseCondition.append(" AND description LIKE CONCAT('%',?,'%')");
                params.add(sp.getDescriptionValue()[0]);
            } else {
                final var placeholders = "?,".repeat(sp.getDescriptionValue().length);
                baseCondition.append(" AND description IN (").append(placeholders, 0, placeholders.length() - 1).append(")");
                params.addAll(Arrays.asList(sp.getDescriptionValue()));
            }
        }

        if ("contains".equals(sp.getFunctionalIdType()) && sp.getFunctionalIdValue() != null && sp.getFunctionalIdValue().length > 0) {
            if (sp.getFunctionalIdValue().length == 1) {
                baseCondition.append(" AND functional_id LIKE CONCAT('%',?,'%')");
                params.add(sp.getFunctionalIdValue()[0]);
            } else {
                final var placeholders = "?,".repeat(sp.getFunctionalIdValue().length);
                baseCondition.append(" AND functional_id IN (").append(placeholders, 0, placeholders.length() - 1).append(")");
                params.addAll(Arrays.asList(sp.getFunctionalIdValue()));
            }
        }

        if ("contains".equals(sp.getAdditionalInformationType()) && sp.getAdditionalInformationValue() != null && sp.getAdditionalInformationValue().length > 0) {
            if (sp.getAdditionalInformationValue().length == 1) {
                baseCondition.append(" AND additional_information LIKE CONCAT('%',?,'%')");
                params.add(sp.getAdditionalInformationValue()[0]);
            } else {
                final var placeholders = "?,".repeat(sp.getAdditionalInformationValue().length);
                baseCondition.append(" AND additional_information IN (").append(placeholders, 0, placeholders.length() - 1).append(")");
                params.addAll(Arrays.asList(sp.getAdditionalInformationValue()));
            }
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

        // Fetch the requested page
        final int pageSize = sp.getPageSize() > 0 ? sp.getPageSize() : 10;
        final int offset = (Math.max(sp.getPage(), 1) - 1) * pageSize;
        final var selectSql = "SELECT *" + baseCondition
                + " ORDER BY started_at DESC, description"
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
        final String sql = "SELECT DISTINCT " + column
                + " FROM forms_forms"
                + " WHERE " + column + " IS NOT NULL AND " + column + " <> ''"
                + " AND LOWER(" + column + ") LIKE LOWER(CONCAT('%',?,'%'))"
                + " ORDER BY " + column
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
}
