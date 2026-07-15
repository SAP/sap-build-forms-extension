package com.sap.bfx.p13n.service;

import com.sap.bfx.p13n.model.Personalization;
import com.sap.bfx.p13n.model.Value;

import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

/**
 * Basic implementation of a data access object for P13N service
 */
public interface CoreDao {

    /**
     * find all personalizations in the database
     *
     * @return a collection of all personalizations
     */
    Collection<Personalization> findAllPersonalizations();

    /**
     * find a personalization by its id
     *
     * @param id the ID for which the personalization should be found
     * @return an optional containing the personalization if found, or empty if not found
     */
    Optional<Personalization> findPersonalizationById(UUID id);

    /**
     * find a personalization by its key, user and app
     *
     * @param key  the key
     * @param User the user for which the personalization should be found
     * @param App  the application for which the personalization should be found
     * @return an optional containing the personalization if found, or empty if not found
     */
    Optional<Personalization> findPersonalizationByKeyUserApp(String key, String User, String App);

    /**
     * find all personalizations for a given user
     *
     * @param user the user for which the personalizations should be found
     * @return a collection of personalizations for the given user
     */
    Collection<Personalization> findPersonalizationsByUser(String user);

    /**
     * find all personalizations for a given user and app
     *
     * @param user the user
     * @param app  the application identifier
     * @return personalizations for the given user and app
     */
    Collection<Personalization> findNonStaticPersonalizationByUserAndApp(String user, String app);

    /**
     * find all visible personalizations for a given user and app
     *
     * @param user the user
     * @param app  the app
     * @return personalizations for the given user and app
     */
    Collection<Personalization> findNonStaticVisiblePersonalizationByUserAndApp(String user, String app);

    /**
     * find all value keys in the database
     *
     * @return a collection of all value keys
     */
    Collection<String> findAllValueKeys();

    /**
     * find all value keys in the database that match the given search string
     *
     * @param searchString the search string to match against value keys
     * @return a collection of value keys that match the search string
     */
    Collection<String> findAllValueKeys(String searchString);

    /**
     * find all values for a given key
     *
     * @param key the key for which the values should be found
     * @return a collection of values for the given key
     */
    Collection<Value> findAllValuesForKey(String key);

    /**
     * find a value by its locale and key
     *
     * @param locale the locale for which the value should be found
     * @param key    the key for which the value should be found
     * @return an optional containing the value if found, or empty if not found
     */
    Optional<Value> findValuesByLocaleAndKey(Locale locale, String key);

    /**
     * find all applications in the database
     *
     * @return a collection of all applications
     */
    Collection<String> findAllApps();

    /**
     * find all users in the database
     *
     * @return a collection of all users
     */
    Collection<String> findAllUsers();

    /**
     * find all users in the database that match the given search string
     *
     * @param searchString the search string to match against user names
     * @return a collection of user names that match the search string
     */
    Collection<String> findAllUsers(String searchString);

    /**
     * add a new personalization to the database
     *
     * @param personalization Personalization object to be added to the database
     */
    void addPersonalization(Personalization personalization);

    /**
     * add a new value to the database
     *
     * @param id     the ID of the value
     * @param locale the locale of the value
     * @param values the values to be added
     */
    void addValue(String id, String locale, String values);

    /**
     * updates an exisiting personliaztzioon in the database
     *
     * @param personalization Personalization object to be updated in the database
     */
    void updatePersonalizationUser(Personalization personalization);

    /**
     * updates an exisiting personliaztzioon in the database
     *
     * @param personalization Personalization object to be updated in the database
     */
    void updatePersonalizationAdmin(Personalization personalization);

    /**
     * updates an exisiting value in the database
     *
     * @param value  Value object to be updated in the database
     * @param values the new values to be set
     */
    void updateValue(Value value, String values);

    /**
     * deletes a personalization from the database by its ID
     *
     * @param id the ID of the personalization to be deleted
     */
    void deletePersonalization(UUID id);

    /**
     * deletes a value from the database by its locale and key
     *
     * @param locale the locale of the value to be deleted
     * @param key    the key of the value to be deleted
     */
    void deleteValue(Locale locale, String key);

    /**
     * deletes a personalization of a given user
     *
     * @param username the user of the personalization to be deleted
     */
    void deleteUser(String username);

    /**
     * deletes a personalization of a given application
     *
     * @param application the application of the personalization to be deleted
     */
    void deleteApplication(String application);

    /**
     * deletes a personalization of a given key and value
     *
     * @param key   key to be used
     * @param value value
     */
    void deleteByKeyAndValue(String key, String value);

    /**
     * deletes a personalization of a given user and application
     *
     * @param username    the user of the personalization to be deleted
     * @param application the application of the personalization to be deleted
     */
    void deleteUserApplication(String username, String application);

    /**
     * deletes a personalization of a given user and application
     *
     * @param username    the user of the personalization to be deleted
     * @param application the application of the personalization to be deleted
     */
    void deleteUserApplicationForUser(String username, String application);
}