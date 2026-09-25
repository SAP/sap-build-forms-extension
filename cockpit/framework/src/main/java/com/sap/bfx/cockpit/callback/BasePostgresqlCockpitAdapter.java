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
     */
    @Override
    public void findProcesses(List<ProcessAbstract> processes, SearchParams sp) {

        final var sql = new StringBuilder("SELECT * FROM forms_forms WHERE 1=1");
        final var params = new ArrayList<>();

        if ("contains".equals(sp.getDescriptionType()) && sp.getDescriptionValue() != null) {
            sql.append(" AND description LIKE CONCAT('%',?,'%')");
            params.add(sp.getDescriptionValue());
        }

        if ("contains".equals(sp.getFunctionalIdType()) && sp.getFunctionalIdValue() != null) {
            sql.append(" AND functional_id LIKE CONCAT('%',?,'%')");
            params.add(sp.getFunctionalIdValue());
        }

        if (sp.getStatus() != null && sp.getStatus().length > 0) {
            final var placeholders = "?,".repeat(sp.getStatus().length);
            sql.append(" AND state IN (").append(placeholders, 0, placeholders.length() - 1).append(")");
            params.addAll(Arrays.asList(sp.getStatus()));
        }

        if (sp.getUser() != null) {
            sql.append(" AND started_by = ?");
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
                    sql.append(" AND started_at BETWEEN ? AND ?");
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
                    sql.append(" AND finished_at BETWEEN ? AND ?");
                    params.add(startDate);
                    params.add(new Timestamp(cal.getTimeInMillis()));
                } catch (ParseException e) {
                    log.warn("Cannot parse endedOn date range: {}", sp.getEndedOn(), e);
                }
            }
        }

        if (sp.getScenario() != null) {
            sql.append(" AND scenario_nm = ?");
            params.add(sp.getScenario());
        }

        sql.append(" ORDER BY started_at DESC, description");

        processes.addAll(jdbc.query(con -> {
            PreparedStatement ps = con.prepareStatement(sql.toString());
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return ps;
        }, new FormRowMapper()));
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
