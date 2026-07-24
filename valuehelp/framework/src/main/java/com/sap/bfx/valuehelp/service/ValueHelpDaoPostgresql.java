package com.sap.bfx.valuehelp.service;

import com.sap.bfx.valuehelp.model.ValueHelp;
import com.sap.bfx.valuehelp.model.ValueHelpDef;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.*;

/**
 * @see <a href="https://www.jackrutorial.com/2018/08/multiple-datasource-in-spring-boot.html" />
 */
@Repository("valueHelpDaoPostgresql")
@Qualifier("valueHelpDaoPostgresql")
@Slf4j
public class ValueHelpDaoPostgresql extends AbstractValueHelpDao {

    @Autowired
    public ValueHelpDaoPostgresql(@Qualifier("dataSourceCore") final DataSource ds) {
        super(ds);
    }

    @Override
    public Collection<ValueHelpDef> findAllDefs() {
        return jdbc.query("SELECT * FROM forms_vh_defs ORDER BY id", new ValueHelpDefinitionRowMapper());
    }

    @Override
    public Collection<ValueHelpDef> findAllDefsBySearchID(String searchID) {
        return jdbc.query("SELECT * FROM forms_vh_defs WHERE LOWER (id) LIKE ? ORDER BY id",
                ps -> ps.setString(1, '%' + searchID.toLowerCase() + '%'), new ValueHelpDefinitionRowMapper());
    }

    @Override
    public Collection<ValueHelpDef> findAllDefsByAdapter(String[] adapter) {

        StringBuilder builder = new StringBuilder();
        builder.append("?,".repeat(adapter.length));
        String placeHolders = builder.deleteCharAt(builder.length() - 1).toString();

        return jdbc.query("SELECT * FROM forms_vh_defs WHERE adapter IN (" + placeHolders + ") ORDER BY id", p -> {
            int i = 1;
            for (String o : adapter) {
                p.setString(i, o);
                i++;
            }
        }, new ValueHelpDefinitionRowMapper());
    }

    @Override
    public Collection<ValueHelpDef> findAllDefsBySearchIDAndAdapter(String searchID, String[] adapter) {

        StringBuilder builder = new StringBuilder();
        builder.append("?,".repeat(adapter.length));
        String placeHolders = builder.deleteCharAt(builder.length() - 1).toString();

        return jdbc.query(
                "SELECT * FROM forms_vh_defs WHERE LOWER (id) LIKE ? AND adapter IN (" + placeHolders + ") ORDER BY id",
                p -> {
                    p.setString(1, '%' + searchID.toLowerCase() + '%');
                    int i = 2;
                    for (String o : adapter) {
                        p.setString(i, o);
                        i++;
                    }
                }, new ValueHelpDefinitionRowMapper());
    }

    @Override
    public Optional<ValueHelpDef> findDefById(String id) {
        var result = jdbc.query("SELECT * FROM forms_vh_defs where id = ?", ps -> ps.setString(1, id),
                new ValueHelpDefinitionRowMapper());

        return (result.isEmpty()) ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Collection<String> findAllAdapter() {
        return jdbc.queryForList("SELECT DISTINCT adapter FROM forms_vh_defs", String.class);
    }

    @Transactional
    @Override
    public void addDef(ValueHelpDef vhd) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO forms_vh_defs (id, ttl, type, adapter, config, description, languages, key_key, value_keys, format_template) VALUES (?,?,?,?,?,?,?,?,?,?)");
            ps.setString(1, vhd.getId());
            ps.setLong(2, vhd.getTtl());
            ps.setString(3, vhd.getValueHelpType() != null ? vhd.getValueHelpType().getIdentifier() : "freestyle");
            ps.setString(4, vhd.getAdapter());
            ps.setString(5, vhd.getConfig());
            ps.setString(6, vhd.getDescription());
            if (vhd.getLanguages().size() > 0) {
                ps.setString(7, String.join(", ", vhd.getLanguages()));
            } else {
                ps.setString(7, "");
            }
            ps.setString(8, vhd.getKeyKey());
            if (vhd.getValueKeys().size() > 0) {
                ps.setString(9, String.join(", ", vhd.getValueKeys()));
            } else {
                ps.setString(9, "");
            }
            ps.setString(10, vhd.getFormatTemplate());

            return ps;
        });
    }

    @Transactional
    @Override
    public void updateDef(ValueHelpDef vhd) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE forms_vh_defs SET ttl=?, type=?, adapter=?, config=?, description=?, languages=?, key_key=?, value_keys=?, format_template=? where id=?");
            ps.setLong(1, vhd.getTtl());
            ps.setString(2, vhd.getValueHelpType() != null ? vhd.getValueHelpType().getIdentifier() : "freestyle");
            ps.setString(3, vhd.getAdapter());
            ps.setString(4, vhd.getConfig());
            ps.setString(5, vhd.getDescription());
            if (vhd.getLanguages().size() > 0) {
                ps.setString(6, String.join(", ", vhd.getLanguages()));
            } else {
                ps.setString(6, "");
            }
            ps.setString(7, vhd.getKeyKey());
            if (vhd.getValueKeys().size() > 0) {
                ps.setString(8, String.join(", ", vhd.getValueKeys()));
            } else {
                ps.setString(8, "");
            }
            ps.setString(9, vhd.getFormatTemplate());
            ps.setString(10, vhd.getId());

            return ps;
        });
    }

    @Transactional
    @Override
    public void deleteDef(String id) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("DELETE FROM forms_vh_defs WHERE id=?");
            ps.setString(1, id);

            return ps;
        });
    }

    @Override
    public Collection<ValueHelp> findAllValuesByDefId(String def_id) {
        return jdbc.query("SELECT * FROM forms_vh_values WHERE id = ?", ps -> ps.setString(1, def_id),
                new ValueHelpValueRowMapper());
    }

    @Override
    public Collection<ValueHelp> findAllValuesByIdLocale(String id, String locale) {
        return jdbc.query("SELECT * FROM forms_vh_values WHERE id = ? AND locale = ?", p -> {
            p.setString(1, id);
            p.setString(2, locale);
        }, new ValueHelpValueRowMapper());
    }

    @Override
    public Optional<ValueHelp> findValueByIdLocaleLatestVersion(String id, String locale) {
        var result = jdbc.query("""
                SELECT * FROM forms_vh_values\s
                WHERE id=? and locale=? and version = (SELECT max(version)
                FROM forms_vh_values where id=? and locale=? group by id, locale)""", p -> {
            p.setString(1, id);
            p.setString(2, locale);
            p.setString(3, id);
            p.setString(4, locale);
        }, new ValueHelpValueRowMapper());
        return (result.isEmpty()) ? Optional.empty() : Optional.of(result.get(0));
    }

    @Override
    public Optional<ValueHelp> findValueByIdLocaleVersion(String id, String locale, long version) {
        var result = jdbc.query("SELECT * FROM forms_vh_values WHERE id = ? AND locale = ? AND version = ?", p -> {
            p.setString(1, id);
            p.setString(2, locale);
            p.setLong(3, version);
        }, new ValueHelpValueRowMapper());
        return (result.isEmpty()) ? Optional.empty() : Optional.of(result.get(0));
    }

    @Transactional
    @Override
    public void addValue(String id, Long version, String locale, java.sql.Timestamp validUntil, String values) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO forms_vh_values (id,version,locale,valid_until,values) VALUES (?,?,?,?,?)");
            ps.setString(1, id);
            ps.setLong(2, version);
            ps.setString(3, locale);
            ps.setTimestamp(4, validUntil);
            ps.setString(5, values);

            return ps;
        });
    }

    @Transactional
    @Override
    public void updateValue(String id, Long version, String locale, java.sql.Timestamp validUntil, String values) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                    "UPDATE forms_vh_values SET values=?,valid_until=?, version=? WHERE id=? and locale=?");
            ps.setString(1, values);
            ps.setTimestamp(2, validUntil);
            ps.setLong(3, version + 1);
            ps.setString(4, id);
            ps.setString(5, locale);

            return ps;
        });
    }

    @Transactional
    @Override
    public void deleteValue(String id) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("DELETE FROM forms_vh_values WHERE id = ?");
            ps.setString(1, id);

            return ps;
        });
    }

    @Transactional
    @Override
    public void deleteValue(String id, String locale) {
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement("DELETE FROM forms_vh_values WHERE id = ? AND locale = ?");
            ps.setString(1, id);
            ps.setString(2, locale);

            return ps;
        });
    }

    @Transactional
    @Override
    public void deleteValue(String id, String locale, long version) {
        jdbc.update(con -> {
            PreparedStatement ps =
                    con.prepareStatement("DELETE FROM forms_vh_values WHERE id = ? AND locale = ? AND version = ?");
            ps.setString(1, id);
            ps.setString(2, locale);
            ps.setLong(3, version);

            return ps;
        });
    }

    @Override
    public Map<String, Long> findValuesVersion(Collection<String> ids, String locale) {
        var result = new HashMap<String, Long>();

        final var params = ArrayUtils.addFirst(ids.toArray(new String[0]), locale);
        final String inSQL = String.join(",", Collections.nCopies(ids.size(), "?"));
        jdbc.query(String.format("SELECT id,version FROM forms_vh_values WHERE locale=? AND id IN(%s)", inSQL),
                (rs, rowNum) -> result.put(rs.getString(1), rs.getLong(2)), params);
        return result;
    }

    @Override
    public Pair<String, Long> findById(String id, String locale) {
        var result = new MutablePair<String, Long>();

        jdbc.query("SELECT version,values FROM forms_vh_values WHERE id=? AND locale=? LIMIT 1", (rs, rowNum) -> {
            try {
                result.setLeft(rs.getString("values"));
                result.setRight(rs.getLong("version"));
            } catch (Exception e) {
                log.error("error", e);
                return null;
            }
            return null;
        }, id, locale);

        return result;
    }


}