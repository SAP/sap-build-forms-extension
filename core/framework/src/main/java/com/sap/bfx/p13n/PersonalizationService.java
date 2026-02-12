package com.sap.bfx.p13n;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service class for managing personalization settings.
 */
@Service
public class PersonalizationService {

    private final PersonalizationClient client;

    /**
     * Constructor for PersonalizationService.
     *
     * @param client the PersonalizationClient to be used for API calls
     */
    @Autowired
    public PersonalizationService(final PersonalizationClient client) {
        this.client = client;
    }

    /**
     * Fetches settings for a given application and user.
     *
     * @param app  the application identifier
     * @param user the user identifier
     * @return a list of Settings objects
     */
    public List<Settings> getSettings(final String app, final String user) {
        return client.getSettings(app, user);
    }

    /**
     * Changes the provided settings.
     *
     * @param settings the list of Settings to be changed
     * @return a list of updated Settings objects
     */
    public List<Settings> changeSettings(ArrayList<Settings> settings) {
        return client.changeSettings(settings);
    }

    /**
     * Deletes the provided settings.
     *
     * @param settings the list of Settings to be deleted
     * @return a list of deleted Settings objects
     */
    public List<Settings> deleteSettings(ArrayList<Settings> settings) {
        return client.deleteSettings(settings);
    }
}
