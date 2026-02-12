package com.sap.bfx.session;

import com.sap.bfx.callback.PersistenceAdapter;
import com.sap.bfx.definition.FormAttributes;
import com.sap.bfx.definition.ProcessState;
import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.utils.EnumUtils;
import com.sap.bfx.utils.JdbcUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

@Slf4j
public abstract class PostgresqlPersistenceAdapter implements PersistenceAdapter {

    private final JdbcTemplate jdbc;

    /**
     * @param jdbc
     */
    protected PostgresqlPersistenceAdapter(final JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public Pair<FormAttributes, InputStream> loadById(String id) {

        var res = new MutablePair<FormAttributes, InputStream>();

        jdbc.query("SELECT * FROM forms_forms WHERE id=? LIMIT 1",
                (PreparedStatementSetter) ps -> {
                    ps.setString(1, id);
                }, new FormCallbackHandler(res));
        return res;
    }

    @Override
    public Pair<FormAttributes, InputStream> loadByRefId(String scenarioName, String refId) {

        var res = new MutablePair<FormAttributes, InputStream>();

        jdbc.query("SELECT * FROM forms_forms WHERE scenario_nm=? AND ref_id=? LIMIT 1",
                (PreparedStatementSetter) ps -> {
                    ps.setString(1, scenarioName);
                    ps.setString(2, refId);
                }, new FormCallbackHandler(res));
        return res;
    }

    /**
     * Saves the form data.
     *
     * @param formAttributes the form attributes
     * @param data           the form data as input stream
     * @param isNew          indicates if this is a new form or an update
     */
    protected void internalSave(final FormAttributes formAttributes, final InputStream data, final boolean isNew) {
        if (isNew) {
            jdbc.update(con -> {
                final var ps = con.prepareStatement("INSERT INTO forms_forms (id,version,ref_id,scenario_nm," +
                        "scneario_ver,wf_adapter,user_nm,ts,template_nm,description,finished_at,functional_id," +
                        "started_at,started_by,state,detail_state,data) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
                ps.setString(1, formAttributes.getId());
                ps.setLong(2, formAttributes.getVersion());
                ps.setString(3, formAttributes.getRefId());
                ps.setString(4, formAttributes.getScenarioName());
                ps.setInt(5, formAttributes.getScenarioVersion());
                ps.setString(6, formAttributes.getWorkflowAdapter());
                ps.setString(7, formAttributes.getChangedBy());
                ps.setTimestamp(8, Timestamp.from(formAttributes.getChangedAt()));
                ps.setString(9, formAttributes.getTemplateName());
                ps.setString(10, formAttributes.getDescription());
                ps.setTimestamp(11, formAttributes.getFinishedAt() != null
                        ? Timestamp.from(formAttributes.getFinishedAt()) : null);
                ps.setString(12, formAttributes.getFunctionalId());
                ps.setTimestamp(13, formAttributes.getStartedAt() != null
                        ? Timestamp.from(formAttributes.getStartedAt()) : null);
                ps.setString(14, formAttributes.getStartedBy());
                ps.setString(15, formAttributes.getState() != null
                        ? formAttributes.getState().getIdentifier() : ProcessState.Draft.getIdentifier());
                ps.setString(16, formAttributes.getDetailState());
                ps.setBlob(17, data);
                return ps;
            });
        } else {
            jdbc.update(con -> {
                final var ps = con.prepareStatement("UPDATE forms_forms SET data=?,version=?,ref_id=?,"
                        + "scenario_nm=?,scneario_ver=?,wf_adapter=?,user_nm=?,ts=?,template_nm=?,description=?,"
                        + "finished_at=?,functional_id=?,started_at=?,started_by=?,state=?,detail_state=?"
                        + " WHERE id=? AND version=?");
                ps.setBlob(1, data);
                ps.setLong(2, formAttributes.getVersion());
                ps.setString(3, formAttributes.getRefId());
                ps.setString(4, formAttributes.getScenarioName());
                ps.setLong(5, formAttributes.getScenarioVersion());
                ps.setString(6, formAttributes.getWorkflowAdapter());
                ps.setString(7, formAttributes.getChangedBy());
                ps.setTimestamp(8, Timestamp.from(formAttributes.getChangedAt()));
                ps.setString(9, formAttributes.getTemplateName());
                ps.setString(10, formAttributes.getDescription());
                ps.setTimestamp(11, formAttributes.getFinishedAt() != null
                        ? Timestamp.from(formAttributes.getFinishedAt()) : null);
                ps.setString(12, formAttributes.getFunctionalId());
                ps.setTimestamp(13, formAttributes.getStartedAt() != null
                        ? Timestamp.from(formAttributes.getStartedAt()) : null);
                ps.setString(14, formAttributes.getStartedBy());
                ps.setString(15, formAttributes.getState() != null
                        ? formAttributes.getState().getIdentifier() : ProcessState.Draft.getIdentifier());
                ps.setString(16, formAttributes.getDetailState());
                ps.setString(17, formAttributes.getId());
                ps.setLong(18, formAttributes.getVersion() - 1); // optimistic locking
                return ps;
            });
        }
    }

    /**
     * Deletes the form with the given id.
     *
     * @param id
     */
    protected void internalDelete(String id) {
        jdbc.update(con -> {
            final var ps = con.prepareStatement("DELETE FROM forms_forms WHERE id=?");
            ps.setString(1, id);
            return ps;
        });
    }

    /**
     * Callback handler to map a ResultSet row to a Form object and InputStream
     */
    private static class FormCallbackHandler implements RowCallbackHandler {

        private MutablePair<FormAttributes, InputStream> result;

        /**
         * Constructor
         *
         * @param result pair to store the result
         */
        FormCallbackHandler(MutablePair<FormAttributes, InputStream> result) {
            this.result = result;
        }

        /**
         * Process a row of the ResultSet
         *
         * @param rs the ResultSet
         */
        @Override
        public void processRow(ResultSet rs) {
            try {
                final Form form = new Form();
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
                form.setState(EnumUtils.valueById(ProcessState.class, rs.getString("state"),
                        ProcessState.Draft));
                form.setTemplateName(rs.getString("template_nm"));
                form.setVersion(rs.getLong("version"));
                form.setWorkflowAdapter(rs.getString("wf_adapter"));

                result.setLeft(form);

                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    IOUtils.copy(rs.getBlob("data").getBinaryStream(), baos);
                    byte[] b = baos.toByteArray();
                    result.setRight(new ByteArrayInputStream(b));
                }
            } catch (SQLException e) {
                throw ExceptionUtils.from("Error loading form: " + e.getMessage() + " (" + e.getSQLState()
                        + "," + e.getErrorCode() + ")", e);
            } catch (Exception e) {
                throw ExceptionUtils.from(e);
            }
        }
    }
}