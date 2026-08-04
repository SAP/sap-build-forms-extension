package com.sap.bfx.p13n.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sap.bfx.callback.AdapterDescriptor;
import com.sap.bfx.p13n.model.Constants;
import com.sap.bfx.p13n.model.Personalization;
import com.sap.bfx.p13n.model.PersonalizationAdapter;
import com.sap.bfx.p13n.model.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service class for managing Personalization operations.
 */
@Service @Slf4j public class PersonalizationService {

    private final Map<String, PersonalizationAdapter> adapterMap = new HashMap<>();
    private final P13NDao dao;

    /**
     * Constructor for PersonalizationService.
     *
     * @param dao
     */
    @Autowired public PersonalizationService(final P13NDao dao) {
        this.dao = dao;
    }

    /**
     * Initializes the PersonalizationAdapter instances from the application context.
     *
     * @param ctx the application context
     */
    public void initPersonalizationAdapter(final ApplicationContext ctx) {
        adapterMap.clear();

        final var adapter = ctx.getBeansOfType(PersonalizationAdapter.class);
        adapter.values().forEach(it -> {
            var descriptor = it.getClass().getAnnotation(AdapterDescriptor.class);
            if (descriptor == null) {
                log.error("PersonalizationAdapter '" + it.getClass().getName() +
                        "' has not annotation of type AdapterDescriptor");
            } else {
                adapterMap.put(descriptor.value(), it);
                log.info("PersonalizationAdapter '" + it.getClass().getName() + "' added with name '" +
                        descriptor.value() + "'.");
            }
        });
    }

    /**
     * Retrieves all Personalization for a given user.
     *
     * @param user the user identifier
     * @return a collection of Personalization objects
     */
    public Collection<Personalization> findAllPersonalizations(String user) {
        if (user != null && !user.isEmpty()) {
            return dao.findPersonalizationsByUser(user);
        } else {
            return dao.findAllPersonalizations();
        }
    }

    /**
     * Finds a Personalization by its unique identifier.
     *
     * @param id the UUID of the Personalization
     * @return an Optional containing the Personalization if found, otherwise empty
     */
    public Optional<Personalization> findPersonalizationById(UUID id) {
        return dao.findPersonalizationById(id);
    }

    /**
     * Finds a Personalization by key, user, and application.
     *
     * @param key  the key of the Personalization
     * @param user the user identifier
     * @param app  the application identifier
     * @return the found Personalization or null if not found
     */
    public Personalization findPersonalizationByKeyUserAndApp(String key, String user, String app) {
        Personalization personalization = null;
        Optional<Personalization> newPersonalization;
        newPersonalization = dao.findPersonalizationByKeyUserApp(key, "_", "_");
        if (newPersonalization.isPresent()) {
            personalization = newPersonalization.get();
        }
        if (user != null && !user.isEmpty() && !user.equals("_")) {
            newPersonalization = dao.findPersonalizationByKeyUserApp(key, user, "_");
            if (newPersonalization.isPresent()) {
                personalization = newPersonalization.get();
            }
        }
        if (app != null && !app.isEmpty() && !app.equals("_")) {
            newPersonalization = dao.findPersonalizationByKeyUserApp(key, "_", app);
            if (newPersonalization.isPresent()) {
                personalization = newPersonalization.get();
            }
        }
        if (user != null && !user.isEmpty() && !user.equals("_") && app != null && !app.isEmpty() && !app.equals("_")) {
            newPersonalization = dao.findPersonalizationByKeyUserApp(key, user, app);
            if (newPersonalization.isPresent()) {
                personalization = newPersonalization.get();
            }
        }

        return personalization;
    }

    /**
     * Finds Personalizations for a user, combining static and non-static visible personalizations.
     *
     * @param username the username to search for
     * @return a collection of Personalization objects
     */
    public Collection<Personalization> findPersonalizationsByUserForUser(String username) {
        Collection<Personalization> personalizations = new ArrayList<>();
        personalizations.addAll(this.findStaticPersonalizations(username, "_"));
        personalizations.addAll(this.findNonStaticVisiblePersonalizations(username, null));
        return personalizations;
    }

    /**
     * Finds Personalizations for a user and application, combining static and non-static visible personalizations.
     *
     * @param username the username to search for
     * @param app      the application to search for
     * @return a collection of Personalization objects
     */
    public Collection<Personalization> findPersonalizationsByUserAndAppForUser(String username, String app) {
        Collection<Personalization> personalizations = new ArrayList<>();
        personalizations.addAll(this.findStaticPersonalizations(username, app));
        personalizations.addAll(this.findNonStaticVisiblePersonalizations(username, app));
        return personalizations;
    }

    /**
     * Finds Personalizations for a user, combining static and all non-static personalizations.
     *
     * @param username the username to search for
     * @return a collection of Personalization objects
     */
    public Collection<Personalization> findPersonalizationsByUserForAdmin(String username) {
        Collection<Personalization> personalizations = new ArrayList<>();
        personalizations.addAll(this.findStaticPersonalizations(username, "_"));
        personalizations.addAll(findNonStaticPersonalizations(username, null));
        return personalizations;
    }

    /**
     * Finds Personalizations for a user and application, combining static and all non-static personalizations.
     *
     * @param username the username to search for
     * @param app      the application to search for
     * @return a collection of Personalization objects
     */
    public Collection<Personalization> findPersonalizationsByUserAndAppForAdmin(String username, String app) {
        Collection<Personalization> personalizations = new ArrayList<>();
        personalizations.addAll(this.findStaticPersonalizations(username, app));
        personalizations.addAll(this.findNonStaticPersonalizations(username, app));
        return personalizations;
    }

    /**
     * Finds static Personalizations for a user and application.
     *
     * @param username the username to search for
     * @param app      the application to search for
     * @return a collection of static Personalization objects
     */
    public Collection<Personalization> findStaticPersonalizations(String username, String app) {
        Collection<Personalization> personalizations = new ArrayList<>();
        for (Field field : Constants.class.getDeclaredFields()) {
            try {
                var p = this.findPersonalizationByKeyUserAndApp(field.get(null).toString(), username, app);
                if (p != null) {
                    personalizations.add(p);
                } else {
                    Optional<Value> value = dao.findValuesByLocaleAndKey(new Locale("_"), field.get(null).toString());
                    if (value.isPresent() && value.get().getValues().size() > 0) {
                        var firstValue = value.get().getValues().get(0);
                        Personalization newPersonalization = new Personalization();
                        newPersonalization.setId(UUID.randomUUID());
                        newPersonalization.setApp("_");
                        newPersonalization.setUser("_");
                        newPersonalization.setEncoding("t");
                        newPersonalization.setKey(field.get(null).toString());
                        if (firstValue.contains("(")) {
                            newPersonalization.setValue(
                                    firstValue.substring(firstValue.indexOf("(") + 1, firstValue.indexOf(")")));
                        } else {
                            newPersonalization.setValue(firstValue);
                        }
                        newPersonalization.setEditable(true);
                        newPersonalization.setVisible(true);
                        dao.addPersonalization(newPersonalization);
                        personalizations.add(newPersonalization);
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return personalizations;
    }

    /**
     * Finds non-static Personalizations for a user and application.
     *
     * @param username the username to search for
     * @param app      the application to search for
     * @return a collection of non-static Personalization objects
     */
    public Collection<Personalization> findNonStaticPersonalizations(String username, String app) {
        Collection<Personalization> personalizations = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        for (Personalization p : dao.findNonStaticPersonalizationByUserAndApp("_", "_")) keys.add(p.getKey());
        for (Personalization p : dao.findNonStaticPersonalizationByUserAndApp(username, "_")) keys.add(p.getKey());
        if (app != null) {
            for (Personalization p : dao.findNonStaticPersonalizationByUserAndApp("_", app)) keys.add(p.getKey());
            for (Personalization p : dao.findNonStaticPersonalizationByUserAndApp(username, app)) keys.add(p.getKey());
        }
        for (String key : keys) {
            personalizations.add(this.findPersonalizationByKeyUserAndApp(key, username, app));
        }
        return personalizations;
    }

    /**
     * Finds non-static visible Personalizations for a user and application.
     *
     * @param username the username to search for
     * @param app      the application to search for
     * @return a collection of non-static visible Personalization objects
     */
    public Collection<Personalization> findNonStaticVisiblePersonalizations(String username, String app) {
        Collection<Personalization> personalizations = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        for (Personalization p : dao.findNonStaticVisiblePersonalizationByUserAndApp("_", "_")) keys.add(p.getKey());
        for (Personalization p : dao.findNonStaticVisiblePersonalizationByUserAndApp(username, "_"))
            keys.add(p.getKey());
        if (app != null) {
            for (Personalization p : dao.findNonStaticVisiblePersonalizationByUserAndApp("_", app))
                keys.add(p.getKey());
            for (Personalization p : dao.findNonStaticVisiblePersonalizationByUserAndApp(username, app))
                keys.add(p.getKey());
        }

        for (String key : keys) {
            personalizations.add(this.findPersonalizationByKeyUserAndApp(key, username, app));
        }
        return personalizations;
    }

    /**
     * Adds a new Personalization.
     *
     * @param personalization the Personalization object to add
     */
    public void addPersonalization(Personalization personalization) {
        if (personalization.getId() == null) {
            personalization.setId(UUID.randomUUID());
        }
        dao.addPersonalization(personalization);
    }

    /**
     * Updates a Personalization for user context.
     *
     * @param personalization the Personalization object to update
     */
    public void updatePersonalizationUser(Personalization personalization) {
        dao.updatePersonalizationUser(personalization);
    }

    /**
     * Updates a Personalization for admin context.
     *
     * @param personalization the Personalization object to update
     */
    public void updatePersonalizationAdmin(Personalization personalization) {
        dao.updatePersonalizationAdmin(personalization);
    }

    /**
     * Retrieves values for static personalizations based on the given locale.
     *
     * @param locale the locale to search for
     * @return a map of keys and their corresponding values
     * @throws NullPointerException   if any value intervals are not completely filled
     * @throws IllegalAccessException if there is an error accessing the fields
     */
    public Map<String, Object> getValuesForStaticPersonalizations(Locale locale)
            throws NullPointerException, IllegalAccessException {
        Map<String, Object> values = new HashMap<>();
        for (Field field : Constants.class.getDeclaredFields()) {
            Optional<Value> value = dao.findValuesByLocaleAndKey(locale, field.get(null).toString());
            if (value.isPresent()) {
                values.put(field.get(null).toString(), value.get().getValues());
            } else {
                var value2 = dao.findValuesByLocaleAndKey(new Locale("_"), field.get(null).toString());
                if (value2.isPresent()) {
                    values.put(field.get(null).toString(), value2.get().getValues());
                } else {
                    throw new NullPointerException("Value intervals are not completely filled");
                }
            }

        }
        return values;
    }

    /**
     * Finds users based on the search string.
     *
     * @param searchString the search string to filter users
     * @return a collection of usernames
     */
    public Collection<String> findUsers(String searchString) {
        if (searchString == null || searchString.isEmpty()) {
            return dao.findAllUsers();
        } else {
            return dao.findAllUsers(searchString);
        }
    }

    /**
     *
     * @param key
     * @param locale
     * @return
     */
    public Optional<Value> findValueByKeyAndLocale(String key, Locale locale) {
        return dao.findValuesByLocaleAndKey(locale, key);
    }

    /**
     * Finds value keys based on the search string.
     *
     * @param searchString the search string to filter value keys
     * @return a collection of value keys
     */
    public Collection<String> findValueKeys(String searchString) {
        Set<String> allKeys = new HashSet<>();

        if (searchString == null || searchString.isEmpty()) {
            for (Field field : Constants.class.getDeclaredFields()) {
                try {
                    allKeys.add(field.get(null).toString());
                } catch (IllegalAccessException ignored) {
                }
            }
            allKeys.addAll(dao.findAllValueKeys());
        } else {
            for (Field field : Constants.class.getDeclaredFields()) {
                try {
                    String newString = field.get(null).toString();
                    if (newString.toLowerCase().contains(searchString.toLowerCase())) {
                        allKeys.add(field.get(null).toString());
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
            allKeys.addAll(dao.findAllValueKeys(searchString));
        }
        return allKeys;
    }

    /**
     * Finds values for a given key.
     *
     * @param key the key to search for
     * @return a collection of Value objects
     */
    public Collection<Value> findValuesForKey(String key) {
        return dao.findAllValuesForKey(key);
    }

    /**
     * Adds a new Value.
     *
     * @param value the Value object to add
     * @throws JsonProcessingException if there is an error processing JSON
     */
    public void addValue(Value value) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        String values = objectMapper.writeValueAsString(value.getValues());
        dao.addValue(value.getId(), value.getLocale().toString(), values);
    }

    /**
     * Updates an existing Value.
     *
     * @param value the Value object to update
     * @throws JsonProcessingException if there is an error processing JSON
     */
    public void updateValue(Value value) throws JsonProcessingException {
        if (value.getLocale().toString().equals("_")) {
            Set<String> newKeys = value.getValues().stream().map(str -> {
                if (str.contains("(") && str.contains(")")) {
                    return str.substring(str.indexOf("(") + 1, str.indexOf(")"));
                } else {
                    return "";
                }
            }).collect(Collectors.toSet());
            if (!newKeys.equals(Set.of(""))) {
                var valueExistent = dao.findValuesByLocaleAndKey(value.getLocale(), value.getId());
                if (valueExistent.isPresent()) {
                    Set<String> existingKeys = valueExistent.get().getValues().stream().map(str -> {
                        if (str.contains("(") && str.contains(")")) {
                            return str.substring(str.indexOf("(") + 1, str.indexOf(")"));
                        } else {
                            return "";
                        }
                    }).collect(Collectors.toSet());
                    Set<String> differentKeys =
                            existingKeys.stream().filter(key -> newKeys.stream().noneMatch(k -> k.equals(key)))
                                        .collect(Collectors.toSet());
                    for (var key : differentKeys) {
                        dao.deleteByKeyAndValue(value.getId(), key);
                    }
                }
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        String values = objectMapper.writeValueAsString(value.getValues());
        dao.updateValue(value, values);
    }

    /**
     * Deletes a Personalization by its unique identifier.
     *
     * @param id the UUID of the Personalization to delete
     */
    public void deletePersonalizationById(UUID id) {
        dao.deletePersonalization(id);
    }

    /**
     * Deletes a user and all associated personalizations.
     *
     * @param username the username to delete
     */
    public void deleteUser(String username) {
        if (!Objects.equals(username, "_")) {
            dao.deleteUser(username);
        }
    }

    /**
     * Deletes a user-application association.
     *
     * @param username    the username
     * @param application the application identifier
     */
    public void deleteUser(String username, String application) {
        if (!(Objects.equals(username, "_") && application.equals("_"))) {
            dao.deleteUserApplication(username, application);
        }
    }

    /**
     * Deletes a user-application association for user context.
     *
     * @param username    the username
     * @param application the application identifier
     */
    public void deleteUserForUser(String username, String application) {
        if (!(Objects.equals(username, "_") && application.equals("_"))) {
            dao.deleteUserApplicationForUser(username, application);
        }
    }

    /**
     * Deletes a Value by locale and key.
     *
     * @param locale the locale of the Value
     * @param key    the key of the Value
     */
    public void deleteValue(Locale locale, String key) {
        dao.deleteValue(locale, key);
    }

    /**
     * Finds all applications.
     *
     * @return a collection of application identifiers
     */
    public Collection<String> findApps() {
        return dao.findAllApps();
    }

    /**
     * Checks if a Personalization exists based on key, user, and application.
     *
     * @param personalization the Personalization object to check
     * @return true if the Personalization exists, false otherwise
     */
    public boolean isPersonalizationExistent(Personalization personalization) {
        return dao.findPersonalizationByKeyUserApp(personalization.getKey(), personalization.getUser(),
                personalization.getApp()).isPresent();
    }
}