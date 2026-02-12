package com.sap.bfx.valuehelp.service;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import com.sap.bfx.valuehelp.model.ValueHelp;
import org.apache.commons.lang3.tuple.Pair;

import com.sap.bfx.valuehelp.model.ValueHelpDef;

public interface CoreDao {

    Collection<ValueHelpDef> findAllDefs();

    Collection<ValueHelpDef> findAllDefsBySearchID(String searchID);

    Collection<ValueHelpDef> findAllDefsByAdapter(String[] adapter);

    Collection<ValueHelpDef> findAllDefsBySearchIDAndAdapter(String searchID, String[] adapter);

    Optional<ValueHelpDef> findDefById(String id);

    Collection<String> findAllAdapter();

    void addDef(ValueHelpDef vhd);

    void updateDef(ValueHelpDef vhd);

    void deleteDef(String id);

    Collection<ValueHelp> findAllValuesByDefId(String id);

    Collection<ValueHelp> findAllValuesByIdLocale(String id, String locale);

    Optional<ValueHelp> findValueByIdLocaleLatestVersion(String id, String locale);

    Optional<ValueHelp> findValueByIdLocaleVersion(String id, String locale, long version);

    void addValue(String id, Long version, String locale, java.sql.Timestamp validUntil, String values);

    void updateValue(String id, Long version, String locale, java.sql.Timestamp validUntil, String values);

    void deleteValue(String id);

    void deleteValue(String id, String locale);

    void deleteValue(String id, String locale, long version);

    Map<String, Long> findValuesVersion(Collection<String> ids, String locale);

    Pair<String, Long> findById(String id, String locale);
}