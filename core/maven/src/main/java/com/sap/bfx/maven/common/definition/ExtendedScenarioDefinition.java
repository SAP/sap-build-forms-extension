package com.sap.bfx.maven.common.definition;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sap.bfx.definition.ScenarioDefinition;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtendedScenarioDefinition extends ScenarioDefinition {
    @JsonIgnore
    private Map<String, List<MixinInfo>> mixins = new HashMap<>();

    public ExtendedScenarioDefinition() {
        super();
    }
}
