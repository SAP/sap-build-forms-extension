package com.sap.bfx.valuehelp;

import com.sap.bfx.definition.ScenarioDefinition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Map;

/**
 * Service class for handling value help operations.
 */
@Service
public class ValueHelpService {

    private final ValueHelpClient client;

    /**
     * Constructor for ValueHelpService.
     *
     * @param client the ValueHelpClient to be used for value help operations
     */
    @Autowired
    public ValueHelpService(final ValueHelpClient client) {
        this.client = client;
    }

    /**
     * Finds versions of value helps based on the provided scenario definition and locale.
     *
     * @param sd     the ScenarioDefinition containing value help IDs
     * @param locale the Locale for which to find versions
     * @return a map of value help IDs to their corresponding versions
     */
    public Map<String, Long> findVersions(final ScenarioDefinition sd, final Locale locale) {
        return client.findValuesVersion(sd.getValueHelpIds(), locale);
    }

    /**
     * Finds values for a given value help ID and locale.
     *
     * @param id     the value help ID
     * @param locale the Locale for which to find values
     * @return a Pair containing the value help ID and its corresponding version
     */
    public GetValueHelpResponse findValues(final String id, final Locale locale) {
        return client.findValues(id, locale);
    }
}
