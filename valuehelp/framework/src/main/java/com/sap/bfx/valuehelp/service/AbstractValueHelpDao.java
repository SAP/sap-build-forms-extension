package com.sap.bfx.valuehelp.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.valuehelp.model.ValueHelp;
import com.sap.bfx.valuehelp.model.ValueHelpDef;
import com.sap.bfx.valuehelp.model.ValueHelpType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
public abstract class AbstractValueHelpDao implements ValueHelpDao {
    protected final JdbcTemplate jdbc;

    /**
     * Creates a new instance of AbstractValueHelpDao with the provided DataSource.
     *
     * @param ds the DataSource to be used for database operations
     */
    protected AbstractValueHelpDao(final DataSource ds) {
        this.jdbc = new JdbcTemplate(ds);
    }

    /**
     * RowMapper implementation for mapping rows of a ResultSet to ValueHelpDef objects.
     * This class is used to convert the result set obtained from a database query into ValueHelpDef objects.
     * It implements the RowMapper interface and overrides the mapRow method to perform the mapping.
     * The mapRow method extracts the values from the ResultSet and sets them into a new ValueHelpDef object, which
     * is then returned.
     * <p>
     * If the "languages" or "value_keys" columns are blank, it initializes the corresponding fields in the
     * ValueHelpDef object with empty lists. The "type" column is used to determine the ValueHelpType of the
     * ValueHelpDef object.
     * <p>
     * This class is intended to be used in conjunction with JdbcTemplate's query methods to retrieve ValueHelpDef
     * objects from the database.
     */
    public static class ValueHelpDefinitionRowMapper implements RowMapper<ValueHelpDef> {
        @Override
        public ValueHelpDef mapRow(ResultSet rs, int rowNum) throws SQLException {
            ValueHelpDef vhd = new ValueHelpDef();
            vhd.setId(rs.getString("id"));
            vhd.setTtl(rs.getLong("ttl"));
            vhd.setAdapter(rs.getString("adapter"));
            vhd.setConfig(rs.getString("config"));
            vhd.setDescription(rs.getString("description"));
            if (StringUtils.isBlank(rs.getString("languages"))) {
                vhd.setLanguages(new ArrayList<>());
            } else {
                vhd.setLanguages(new ArrayList<>(Arrays.asList(rs.getString("languages").split(", "))));
            }
            vhd.setKeyKey(rs.getString("key_key"));
            if (StringUtils.isBlank(rs.getString("value_keys"))) {
                vhd.setValueKeys(new ArrayList<>());
            } else {
                vhd.setValueKeys(new ArrayList<>(Arrays.asList(rs.getString("value_keys").split(", "))));
            }
            vhd.setFormatTemplate(rs.getString("format_template"));
            String type = rs.getString("type");
            vhd.setValueHelpType(ValueHelpType.CURRENCY.getIdentifier().equals(type) ? ValueHelpType.CURRENCY :
                    ValueHelpType.FREESTYLE);

            return vhd;
        }
    }

    /**
     * RowMapper implementation for mapping rows of a ResultSet to ValueHelp objects.
     * This class is used to convert the result set obtained from a database query into ValueHelp objects.
     * It implements the RowMapper interface and overrides the mapRow method to perform the mapping.
     * The mapRow method extracts the values from the ResultSet and sets them into a new ValueHelp object,
     * which is then returned.
     * <p>
     * If the "locale" column is equal to "_", it initializes the locale field in the ValueHelp object with a new
     * Locale object with "_" as the language. Otherwise, it initializes the locale field with a new Locale object
     * using the value from the "locale" column.
     */
    public static class ValueHelpValueRowMapper implements RowMapper<ValueHelp> {
        @Override
        @SneakyThrows
        public ValueHelp mapRow(ResultSet rs, int rowNum) {
            ValueHelp vhd = new ValueHelp();
            vhd.setId(rs.getString("id"));
            vhd.setVersion(rs.getLong("version"));
            if (rs.getString("locale").equals("_")) {
                vhd.setLocale(new Locale("_"));
            } else {
                vhd.setLocale(new Locale(rs.getString("locale")));
            }
            vhd.setValidUntil(rs.getTimestamp("valid_until"));
            try {
                vhd.setValues(new ObjectMapper().readValue(rs.getString("values"), List.class));
            } catch (Exception e) {
                log.error("error reading values of value-help '" + vhd.getId() + "'", e);
                throw ExceptionUtils.from(e);
            }
            return vhd;
        }
    }
}
