package com.sap.bfx.p13n.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.bfx.p13n.model.Personalization;
import com.sap.bfx.p13n.model.Value;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Slf4j
public class CoreDaoPostgresql extends AbstractCoreDao {
    
    @Autowired
    public CoreDaoPostgresql(@Qualifier("dataSourceCore") final DataSource ds) {
        super(ds);
    }

    @Override
    public Collection<Personalization> findAllPersonalizations() {
        return jdbc.query("SELECT * FROM forms_p13n_settings ORDER BY id", new PersonalizationRowMapper());
    }

    @Override
    public Optional<Personalization> findPersonalizationById(UUID id) {
        var result =
                jdbc.query("SELECT * FROM forms_p13n_settings where id = ?", ps -> ps.setString(1, String.valueOf(id)),
                        new PersonalizationRowMapper());

        return (result.isEmpty()) ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<Personalization> findPersonalizationByKeyUserApp(String key, String user, String app) {
        var result =
                jdbc.query("SELECT * FROM forms_p13n_settings where key = ? " + "AND user_nm = ? AND app = ?", ps -> {
                    ps.setString(1, key);
                    ps.setString(2, user);
                    ps.setString(3, app);
                }, new PersonalizationRowMapper());

        return (result.isEmpty()) ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Collection<Personalization> findPersonalizationsByUser(String user) {
        return jdbc.query("SELECT * FROM forms_p13n_settings where user_nm = ? ORDER BY id",
                ps -> ps.setString(1, user), new PersonalizationRowMapper());
    }

    @Override
    public Collection<Personalization> findNonStaticPersonalizationByUserAndApp(String user, String app) {
        return jdbc.query("SELECT * FROM forms_p13n_settings where user_nm = ? " +
                "AND app = ? AND key NOT LIKE '\\_%' ORDER BY id", ps -> {
            ps.setString(1, user);
            ps.setString(2, app);
        }, new PersonalizationRowMapper());
    }

    @Override
    public Collection<Personalization> findNonStaticVisiblePersonalizationByUserAndApp(String user, String app) {
        return jdbc.query("SELECT * FROM forms_p13n_settings where user_nm = ? AND app = ? " +
                "AND key NOT LIKE '\\_%' AND visible=true ORDER BY id", ps -> {
            ps.setString(1, user);
            ps.setString(2, app);
        }, new PersonalizationRowMapper());
    }

    @Override
    public Collection<String> findAllValueKeys() {
        return jdbc.queryForList("SELECT DISTINCT id FROM forms_p13n_defaults ORDER BY id", String.class);
    }

    @Override
    public Collection<String> findAllValueKeys(String searchString) {
        return jdbc.query("SELECT DISTINCT id FROM forms_p13n_defaults WHERE LOWER(id) " +
                        "LIKE '%' || LOWER(?) || '%' ORDER BY id", ps -> ps.setString(1, searchString),
                (rs, rowNum) -> rs.getString("id"));
    }

    @Override
    public Collection<Value> findAllValuesForKey(String key) {
        return jdbc.query("SELECT * FROM forms_p13n_defaults where id = ?", ps -> ps.setString(1, key),
                new ValueRowMapper());
    }

    @Override
    public Optional<Value> findValuesByLocaleAndKey(Locale locale, String key) {
        var result = jdbc.query("SELECT * FROM forms_p13n_defaults WHERE locale = ? AND id = ?", ps -> {
            ps.setString(1, locale.toString());
            ps.setString(2, key);
        }, new ValueRowMapper());
        return (result.isEmpty()) ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Collection<String> findAllApps() {
        return jdbc.queryForList("SELECT DISTINCT app FROM forms_p13n_settings WHERE app <> '_' ORDER BY app",
                String.class);
    }

    @Override
    public Collection<String> findAllUsers() {
        return jdbc.queryForList(
                "SELECT DISTINCT user_nm FROM forms_p13n_settings WHERE user_nm <> '_' " + "ORDER BY user_nm",
                String.class);
    }

    @Override
    public Collection<String> findAllUsers(String searchString) {
        return jdbc.query("SELECT DISTINCT user_nm FROM forms_p13n_settings WHERE LOWER(user_nm) " +
                        "LIKE '%' || LOWER(?) || '%' ORDER BY user_nm", ps -> ps.setString(1, searchString),
                (rs, rowNum) -> rs.getString("user_nm"));
    }

    @Transactional
    @Override
    public void addPersonalization(Personalization personalization) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO forms_p13n_settings (id, user_nm, key, app, encoding, value, editable, visible) " +
                            "VALUES (?,?,?,?,?,?,?,?)");
            ps.setString(1, String.valueOf(personalization.getId()));
            ps.setString(2, personalization.getUser());
            ps.setString(3, personalization.getKey());
            ps.setString(4, personalization.getApp());
            ps.setString(5, personalization.getEncoding());
            ps.setString(6, personalization.getValue());
            ps.setBoolean(7, personalization.isEditable());
            ps.setBoolean(8, personalization.isVisible());
            return ps;
        });
    }

    @Transactional
    @Override
    public void addValue(String id, String locale, String values) {
        jdbc.update(con -> {
            PreparedStatement ps =
                    con.prepareStatement("INSERT INTO forms_p13n_defaults (id, locale, values) " + "VALUES (?,?,?)");
            ps.setString(1, id);
            ps.setString(2, locale);
            ps.setString(3, values);
            return ps;
        });
    }

    @Transactional
    @Override
    public void updatePersonalizationUser(Personalization personalization) {
        jdbc.update(con -> {
            PreparedStatement ps =
                    con.prepareStatement("UPDATE forms_p13n_settings SET encoding=?, value=? where id=?");
            ps.setString(1, personalization.getEncoding());
            ps.setString(2, personalization.getValue());
            ps.setString(3, String.valueOf(personalization.getId()));

            return ps;
        });
    }

    @Transactional
    @Override
    public void updatePersonalizationAdmin(Personalization personalization) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE forms_p13n_settings SET encoding=?, value=?, editable=?, " + "visible=? where id=?");
            ps.setString(1, personalization.getEncoding());
            ps.setString(2, personalization.getValue());
            ps.setBoolean(3, personalization.isEditable());
            ps.setBoolean(4, personalization.isVisible());
            ps.setString(5, String.valueOf(personalization.getId()));

            return ps;
        });
    }

    @Transactional
    @Override
    public void updateValue(Value value, String values) {
        jdbc.update(con -> {
            PreparedStatement ps =
                    con.prepareStatement("UPDATE forms_p13n_defaults SET values=? where id=? and locale=?");
            ps.setString(1, values);
            ps.setString(2, value.getId());
            ps.setString(3, value.getLocale().toString());
            return ps;
        });
    }

    @Transactional
    @Override
    public void deletePersonalization(UUID id) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("DELETE FROM forms_p13n_settings WHERE id=?");
            ps.setString(1, String.valueOf(id));

            return ps;
        });
    }

    @Transactional
    @Override
    public void deleteUser(String username) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("DELETE FROM forms_p13n_settings WHERE user_nm=?");
            ps.setString(1, username);

            return ps;
        });
    }

    @Transactional
    @Override
    public void deleteApplication(String application) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("DELETE FROM forms_p13n_settings WHERE app=?");
            ps.setString(1, application);

            return ps;
        });
    }

    @Transactional
    @Override
    public void deleteByKeyAndValue(String key, String value) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("DELETE FROM forms_p13n_settings WHERE key=? AND value=?");
            {
                ps.setString(1, key);
                ps.setString(2, value);
            }


            return ps;
        });
    }

    @Transactional
    @Override
    public void deleteUserApplication(String username, String application) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("DELETE FROM forms_p13n_settings WHERE user_nm = ? AND app=?");
            {
                ps.setString(1, username);
                ps.setString(2, application);
            }

            return ps;
        });
    }

    @Transactional
    @Override
    public void deleteUserApplicationForUser(String username, String application) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("DELETE FROM forms_p13n_settings WHERE user_nm = ? AND app=? " +
                    "AND visible=true AND editable=true");
            {
                ps.setString(1, username);
                ps.setString(2, application);
            }

            return ps;
        });
    }

    @Transactional
    @Override
    public void deleteValue(Locale locale, String key) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("DELETE FROM forms_p13n_defaults WHERE locale = ? AND id=?");
            {
                ps.setString(1, locale.toString());
                ps.setString(2, key);
            }
            return ps;
        });
    }

    public static class PersonalizationRowMapper implements RowMapper<Personalization> {
        @Override
        public Personalization mapRow(ResultSet rs, int rowNum) throws SQLException {
            Personalization personalization = new Personalization();
            personalization.setId(UUID.fromString(rs.getString("id")));
            personalization.setUser(rs.getString("user_nm"));
            personalization.setKey(rs.getString("key"));
            personalization.setApp(rs.getString("app"));
            personalization.setEncoding(rs.getString("encoding"));
            personalization.setValue(rs.getString("value"));
            personalization.setEditable(rs.getBoolean("editable"));
            personalization.setVisible(rs.getBoolean("visible"));
            return personalization;
        }
    }

    public static class ValueRowMapper implements RowMapper<Value> {
        @SneakyThrows
        @Override
        public Value mapRow(ResultSet rs, int rowNum) {
            Value value = new Value();
            value.setId(rs.getString("id"));
            if (rs.getString("locale").equals("_")) {
                value.setLocale(new Locale("_"));
            } else {
                value.setLocale(new Locale(rs.getString("locale")));
            }
            value.setValues(new ObjectMapper().readValue(rs.getString("values"), ArrayList.class));
            return value;
        }
    }
}