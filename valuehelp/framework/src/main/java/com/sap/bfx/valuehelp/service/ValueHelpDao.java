package com.sap.bfx.valuehelp.service;

import com.sap.bfx.valuehelp.model.ValueHelp;
import com.sap.bfx.valuehelp.model.ValueHelpDef;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Dao interface for ValueHelp, which defines the methods to access the underlying data storage for ValueHelp
 * definitions and values.
 */
public interface ValueHelpDao {

    /**
     * Finds all ValueHelp definitions.
     *
     * @return a collection of all ValueHelp definitions
     */
    Collection<ValueHelpDef> findAllDefs();

    /**
     * Finds all ValueHelp definitions by search ID.
     *
     * @param searchID the search ID to filter the ValueHelp definitions
     * @return a collection of ValueHelp definitions that match the search ID
     */
    Collection<ValueHelpDef> findAllDefsBySearchID(String searchID);

    /**
     * Finds all ValueHelp definitions by adapter.
     *
     * @param adapter the adapter to filter the ValueHelp definitions
     * @return a collection of ValueHelp definitions that match the adapter
     */
    Collection<ValueHelpDef> findAllDefsByAdapter(String[] adapter);

    /**
     * Finds all ValueHelp definitions by search ID and adapter.
     *
     * @param searchID the search ID to filter the ValueHelp definitions
     * @param adapter  the adapter to filter the ValueHelp definitions
     * @return a collection of ValueHelp definitions that match the search ID and adapter
     */
    Collection<ValueHelpDef> findAllDefsBySearchIDAndAdapter(String searchID, String[] adapter);

    /**
     * Finds a ValueHelp definition by ID.
     *
     * @param id the ID of the ValueHelp definition to find
     * @return an Optional containing the ValueHelp definition if found, or empty if not found
     */
    Optional<ValueHelpDef> findDefById(String id);

    /**
     * Finds all adapters.
     *
     * @return a collection of all adapters
     */
    Collection<String> findAllAdapter();

    /**
     * Adds a new ValueHelp definition.
     *
     * @param vhd the ValueHelp definition to add
     */
    void addDef(ValueHelpDef vhd);

    /**
     * Updates an existing ValueHelp definition.
     *
     * @param vhd the ValueHelp definition to update
     */
    void updateDef(ValueHelpDef vhd);

    /**
     * Deletes a ValueHelp definition by ID.
     *
     * @param id the ID of the ValueHelp definition to delete
     */
    void deleteDef(String id);

    /**
     * Finds all ValueHelp values by definition ID.
     *
     * @param id the ID of the ValueHelp definition to find values for
     * @return a collection of ValueHelp values that match the definition ID
     */
    Collection<ValueHelp> findAllValuesByDefId(String id);

    /**
     * Finds all ValueHelp values by definition ID and locale.
     *
     * @param id     the ID of the ValueHelp definition to find values for
     * @param locale the locale to filter the ValueHelp values
     * @return a collection of ValueHelp values that match the definition ID and locale
     */
    Collection<ValueHelp> findAllValuesByIdLocale(String id, String locale);

    /**
     * Finds the latest version of a ValueHelp value by definition ID and locale.
     *
     * @param id     the ID of the ValueHelp definition to find the value for
     * @param locale the locale to filter the ValueHelp value
     * @return an Optional containing the latest version of the ValueHelp value if found, or empty if not found
     */
    Optional<ValueHelp> findValueByIdLocaleLatestVersion(String id, String locale);

    /**
     * Finds a ValueHelp value by definition ID, locale, and version.
     *
     * @param id      the ID of the ValueHelp definition to find the value for
     * @param locale  the locale to filter the ValueHelp value
     * @param version the version of the ValueHelp value to find
     * @return an Optional containing the ValueHelp value if found, or empty if not found
     */
    Optional<ValueHelp> findValueByIdLocaleVersion(String id, String locale, long version);

    /**
     * Adds a new ValueHelp value.
     *
     * @param id         the ID of the ValueHelp definition to add the value for
     * @param version    the version of the ValueHelp value to add
     * @param locale     the locale of the ValueHelp value to add
     * @param validUntil the timestamp until which the ValueHelp value is valid
     * @param values     the values of the ValueHelp value to add
     */
    void addValue(String id, Long version, String locale, java.sql.Timestamp validUntil, String values);

    /**
     * Updates an existing ValueHelp value.
     *
     * @param id         the ID of the ValueHelp definition to update the value for
     * @param version    the version of the ValueHelp value to update
     * @param locale     the locale of the ValueHelp value to update
     * @param validUntil the timestamp until which the ValueHelp value is valid
     * @param values     the values of the ValueHelp value to update
     */
    void updateValue(String id, Long version, String locale, java.sql.Timestamp validUntil, String values);

    /**
     * Deletes a ValueHelp value by definition ID.
     *
     * @param id the ID of the ValueHelp definition to delete the value for
     */
    void deleteValue(String id);

    /**
     * Deletes a ValueHelp value by definition ID and locale.
     *
     * @param id     the ID of the ValueHelp definition to delete the value for
     * @param locale the locale to filter the ValueHelp value to delete
     */
    void deleteValue(String id, String locale);

    /**
     * Deletes a ValueHelp value by definition ID, locale, and version.
     *
     * @param id      the ID of the ValueHelp definition to delete the value for
     * @param locale  the locale to filter the ValueHelp value to delete
     * @param version the version of the ValueHelp value to delete
     */
    void deleteValue(String id, String locale, long version);

    /**
     * Finds the versions of ValueHelp values by definition IDs and locale.
     *
     * @param ids    the collection of definition IDs to find the versions for
     * @param locale the locale to filter the ValueHelp values
     * @return a map of definition IDs to their corresponding latest version numbers
     */
    Map<String, Long> findValuesVersion(Collection<String> ids, String locale);

    /**
     * Finds a ValueHelp value by definition ID and locale.
     *
     * @param id     the ID of the ValueHelp definition to find the value for
     * @param locale the locale to filter the ValueHelp value
     * @return a Pair containing the values of the ValueHelp values (as JSON string) and its version number if found,
     * or null if not found
     */
    Pair<String, Long> findById(String id, String locale);
}