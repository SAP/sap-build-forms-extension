package com.sap.bfx.cockpit.callback;

import com.sap.bfx.cockpit.service.ProcessAbstract;
import com.sap.bfx.definition.ProcessState;
import com.sap.bfx.utils.EnumUtils;
import com.sap.bfx.utils.JdbcUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Base implementation of the cockpit database adapter for HANA
 */
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
     */
    @Override
    public void findProcesses(List<ProcessAbstract> processes, SearchParams sp) {

        final var sql = new StringBuilder("SELECT * FROM forms_data.forms_forms WHERE 1=1");
        final var params = new ArrayList<>();

        //TODO: Filter for searchParameter, roleUser, ended on, scenario

//        if (sp.getDescriptionType() != null && descriptionValue != null) {
//            if (Arrays.asList(new String[]{"equals", "contains", "begins_with", "ends_with"})
//                    .contains(descriptionType)) {
//                switch (descriptionType) {
//                    case "equals" -> sql.append(" AND description = ?");
//                    case "contains" -> sql.append(" AND description LIKE CONCAT('%',?,'%')");
//                    case "begins_with" -> sql.append(" AND description LIKE CONCAT(?,'%')");
//                    case "ends_with" -> sql.append(" AND description LIKE CONCAT('%',?)");
//                }
//                params.add(descriptionValue);
//            }
//        }
//
//        if (functionalIdType != null && functionalIdValue != null) {
//            if (Arrays.asList(new String[]{"equals", "contains", "begins_with", "ends_with"})
//                    .contains(functionalIdType)) {
//                switch (functionalIdType) {
//                    case "equals" -> sql.append(" AND functional_id = ?");
//                    case "contains" -> sql.append(" AND functional_id LIKE CONCAT('%',?,'%')");
//                    case "begins_with" -> sql.append(" AND functional_id LIKE CONCAT(?,'%')");
//                    case "ends_with" -> sql.append(" AND functional_id LIKE CONCAT('%',?)");
//                }
//                params.add(functionalIdValue);
//            }
//        }
//
//        if (status != null && status.length > 0) {
//            StringBuilder builder = new StringBuilder();
//            builder.append("?,".repeat(status.length));
//            String placeHolders = builder.deleteCharAt(builder.length() - 1).toString();
//            sql.append(" AND state IN (");
//            sql.append(placeHolders);
//            sql.append(")");
//            params.addAll(Arrays.asList(status));
//        }
//
////        if (additionalInformationType != null && additionalInformationValue != null) {
////            if (Arrays.asList(new String[]{"equals", "contains", "begins_with", "ends_with"})
////                    .contains(additionalInformationType)) {
////                switch (additionalInformationType) {
////                    case "equals" -> sql.append(" AND additional_information = ?");
////                    case "contains" -> sql.append(" AND additional_information LIKE CONCAT('%',?,'%')");
////                    case "begins_with" -> sql.append(" AND additional_information LIKE CONCAT(?,'%')");
////                    case "ends_with" -> sql.append(" AND additional_information LIKE CONCAT('%',?)");
////                }
////                params.add(additionalInformationValue);
////            }
////        }
//
//        if (user != null) {
//            sql.append(" AND started_by = ?");
//            params.add(user);
//        }
//
//        if (startedBy != null) {
//            String[] dates = startedBy.split(" - ");
//            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
//
//            Timestamp startDate = null;
//            Timestamp endDate = null;
//            try {
//                startDate = new Timestamp(dateFormat.parse(dates[0]).getTime());
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(dateFormat.parse(dates[1]));
//                calendar.set(Calendar.HOUR_OF_DAY, 23);
//                calendar.set(Calendar.MINUTE, 59);
//                calendar.set(Calendar.SECOND, 59);
//                calendar.set(Calendar.MILLISECOND, 999);
//                endDate = new Timestamp(calendar.getTimeInMillis());
//
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            sql.append(" AND started_at BETWEEN ? AND ?");
//            params.add(startDate);
//            params.add(endDate);
//        }
//
//        if (endedOn != null) {
//            String[] dates = endedOn.split(" - ");
//            SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
//
//            Timestamp startDate = null;
//            Timestamp endDate = null;
//            try {
//                startDate = new Timestamp(dateFormat.parse(dates[0]).getTime());
//                Calendar calendar = Calendar.getInstance();
//                calendar.setTime(dateFormat.parse(dates[1]));
//                calendar.set(Calendar.HOUR_OF_DAY, 23);
//                calendar.set(Calendar.MINUTE, 59);
//                calendar.set(Calendar.SECOND, 59);
//                calendar.set(Calendar.MILLISECOND, 999);
//                endDate = new Timestamp(calendar.getTimeInMillis());
//
//            } catch (ParseException e) {
//                e.printStackTrace();
//            }
//            sql.append(" AND finished_at BETWEEN ? AND ?");
//            params.add(startDate);
//            params.add(endDate);
//        }

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
