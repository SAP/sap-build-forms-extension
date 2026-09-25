package com.sap.bfx.cockpit.service;

import com.sap.bfx.btp.ConnectivityUtils;
import com.sap.bfx.callback.AbstractAdapterHandlingService;
import com.sap.bfx.cockpit.callback.CockpitAdapter;
import com.sap.bfx.cockpit.callback.FrontendParams;
import com.sap.bfx.cockpit.callback.FrontendSettings;
import com.sap.bfx.cockpit.callback.SearchParams;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Service class for managing cockpit adapters and process instances.
 */
@Service
@Slf4j
public class CockpitService extends AbstractAdapterHandlingService<CockpitAdapter> {

    private Map<String, String> scenarioUrls;

    /**
     * Optional static scenario URL map configured via application properties.
     * Key: scenario name, value: base URL of the scenario application.
     * When set, BTP Destination Service is not called.
     */
    @Value("#{${forms.cockpit.scenario-urls:{}}}")
    private Map<String, String> configuredScenarioUrls;

    /**
     *
     * @param applicationContext
     */
    @Autowired
    public CockpitService(final ApplicationContext applicationContext) {
        super(applicationContext, CockpitAdapter.class);

    }

    /**
     * Initialize frontend settings using all available adapters.
     *
     * @param params Frontend parameters
     * @return Initialized frontend settings
     */
    public FrontendSettings init(FrontendParams params) {
        final var result = new FrontendSettings();

        this.getAllAdapters().forEach(a -> a.init(result, params));

        return result;
    }

    /**
     * Finds processes based on the provided search parameters.
     *
     * @param params Search parameters for filtering processes.
     * @return Collection of FormAttributes matching the search criteria.
     */
    public Collection<ProcessAbstract> findProcesses(SearchParams params) {
        final var result = new ArrayList<ProcessAbstract>();
        // retrieval is done by the adapters
        this.getAllAdapters().forEach(a -> a.findProcesses(result, params));
        // after this fill the scenario URLs
        this.checkScenarioUrls();
        result.forEach(process -> {
            var scenarioUrl = scenarioUrls != null ? scenarioUrls.get(process.getScenarioName()) : null;
            if (StringUtils.isBlank(scenarioUrl)) {
                scenarioUrl = "http://localhost:8080";
            }
            process.setScenarioUrl(scenarioUrl);
        });

        return result;
    }

    /**
     * Check and load scenario URLs if not already loaded.
     * Uses statically configured URLs from application properties when available;
     * otherwise falls back to BTP Destination Service lookup.
     */
    private void checkScenarioUrls() {
        if (this.scenarioUrls != null) {
            return;
        }
        if (configuredScenarioUrls != null && !configuredScenarioUrls.isEmpty()) {
            this.scenarioUrls = configuredScenarioUrls;
            return;
        }
        try {
            this.scenarioUrls = ConnectivityUtils.getAllScenarioUrls();
        } catch (Exception e) {
            log.warn("Cannot read scenario URLs from destinations", e);
        }
    }
}