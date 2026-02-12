package com.sap.bfx.definition;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sap.bfx.exception.ExceptionUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class DefinitionService {

    @Autowired
    private ApplicationContext appContext;

    private Map<Integer, ScenarioDefinition> map = new HashMap<>();

    /**
     * Load scenario definitions from JSON file
     */
    @PostConstruct
    @SuppressWarnings("unused")
    private void init() {
        log.info("scenario-service - init - start");

        try (InputStream is = DefinitionService.class.getClassLoader().getResourceAsStream("definitions.json")) {
            final var om = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            final var module = new SimpleModule();
            module.addDeserializer(ScenarioDefinition.class, new ScenarioDefinitionDeserializer(true));
            om.registerModule(module);

            final var typeRef = new TypeReference<HashMap<Integer, ScenarioDefinition>>() {
            };
            map = om.readValue(is, typeRef);
        } catch (Exception e) {
            if (!appContext.getEnvironment().getProperty("forms.ignoreMissingDefinitions",
                    Boolean.class, false)) {
                throw ExceptionUtils.from("Error in DefinitionService.init", e);
            } else {
                log.warn("No definitions found but forms.ignoreMissingDefinitions is set to true, continuing...");
            }
        }

        for (var version : map.keySet()) {
            final var sd = map.get(version);
            log.info("  Version: " + version + " (" + (sd.isActive() ? "active" : "non-active") + ")");
            sd.postLoad(appContext);
        }

        log.info("scenario-service - init - finish");
    }

    /**
     * Get all available versions
     *
     * @return collection of versions
     */
    public Collection<Integer> getVersions() {
        return map.keySet();
    }

    /**
     * Find scenario definition by version
     *
     * @param version version
     * @return optional scenario definition
     */
    public Optional<ScenarioDefinition> findDefinitionByVersion(Integer version) {
        return Optional.ofNullable(map.get(version));
    }

    /**
     * Find active scenario definition
     *
     * @return optional scenario definition
     */
    public Optional<ScenarioDefinition> findActiveDefinition() {
        return map.values().stream().filter(ScenarioDefinition::isActive).findFirst();
    }
}
