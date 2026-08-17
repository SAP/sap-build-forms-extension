package com.sap.bfx.maven.common.definition;

import com.sap.bfx.definition.ElementDefinition;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class MixinInfo {
    private ElementDefinition mixin;
    private List<ElementDefinition> elements;
    private String className;
}
