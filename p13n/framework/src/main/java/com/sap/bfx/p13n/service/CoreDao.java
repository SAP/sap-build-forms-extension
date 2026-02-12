package com.sap.bfx.p13n.service;

import com.sap.bfx.p13n.model.Personalization;
import com.sap.bfx.p13n.model.Value;

import java.util.*;

public interface CoreDao {

    Collection<Personalization> findAllPersonalizations();

    Optional<Personalization> findPersonalizationById(UUID id);

    Optional<Personalization> findPersonalizationByKeyUserApp(String key, String User, String App);

    Collection<Personalization> findPersonalizationsByUser(String user);

    Collection<Personalization> findNonStaticPersonalizationByUserAndApp(String user, String app);

    Collection<Personalization> findNonStaticVisiblePersonalizationByUserAndApp(String user, String app);

    Collection<String> findAllValueKeys();

    Collection<String> findAllValueKeys(String searchString);

    Collection<Value> findAllValuesForKey(String key);

    Optional<Value> findValuesByLocaleAndKey(Locale locale, String key);

    Collection<String> findAllApps();

    Collection<String> findAllUsers();

    Collection<String> findAllUsers(String searchString);

    void addPersonalization(Personalization personalization);

    void addValue(String id, String locale, String values);

    void updatePersonalizationUser(Personalization personalization);

    void updatePersonalizationAdmin(Personalization personalization);

    void updateValue(Value value, String values);

    void deletePersonalization(UUID id);

    void deleteValue(Locale locale, String key);

    void deleteUser(String username);

    void deleteApplication(String application);

    void deleteByKeyAndValue(String key, String value);

    void deleteUserApplication(String username, String application);

    void deleteUserApplicationForUser(String username, String application);
}