package com.sap.bfx.session;

import com.sap.bfx.exception.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * for database layout and SQL statements see
 * <ahref="https://jdbc.postgresql.org/documentation/binary-data/">Postgresql documentation</ahref="https://jdbc.postgresql.org/documentation/binary-data/">
 * <p>
 * The adapter expects a table with <pre>CREATE TABLE forms_attachments (id vchar(80), content oid);</pre>.
 * Beside this, the connection must be configured with auto-commit == false!
 */
@Slf4j
public abstract class PostgresqlAttachmentAdapter extends AbstractAttachmentAdapter {


    /**
     * Constructor
     *
     * @param jdbc The JdbcTemplate to be used to access the database. It is expected that the JdbcTemplate is
     *             configured with a DataSource that has auto-commit set to false.
     */
    protected PostgresqlAttachmentAdapter(final JdbcTemplate jdbc) {
        super(jdbc);
    }

    /**
     * @param id
     * @param is
     * @param fileName
     * @param contentType
     * @param size
     * @param category
     * @param description
     * @return
     */
    protected String internalSave(final String id, final InputStream is, final String fileName,
                                  final String contentType, final long size, final String category,
                                  final String description) {

        final var ref = UUID.randomUUID().toString();

        jdbc.update(con -> {
            final var ps = con.prepareStatement("INSERT INTO forms_attachments (id, content) VALUES (?,?)");
            ps.setString(1, ref);
            ps.setBlob(2, is, size);
            return ps;
        });
        return ref;
    }

    /**
     * @param ref
     * @return
     */
    @Override
    public InputStream load(String ref) {
        final var result = new AtomicReference<InputStream>(null);

        jdbc.query("SELECT content FROM forms_attachments WHERE id=?", ps -> {
            ps.setString(1, ref);
        }, rs -> {
            try {
                byte[] b = null;
                try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                    IOUtils.copy(rs.getBlob(1).getBinaryStream(), baos);
                    b = baos.toByteArray();
                }
                result.set(new ByteArrayInputStream(b));
            } catch (SQLException e) {
                throw ExceptionUtils.from("Error loading attachment: " + e.getMessage() + " (" + e.getSQLState() + "," +
                        e.getErrorCode() + ")", e);
            } catch (Exception e) {
                throw ExceptionUtils.from(e);
            }
        });

        return result.get();
    }

    /**
     * @param ref
     */
    protected void internalDelete(final String ref) {
        jdbc.update(con -> {
            final var ps = con.prepareStatement("DELETE FROM forms_attachments WHERE id=?");
            ps.setString(1, ref);
            return ps;
        });
    }
}
