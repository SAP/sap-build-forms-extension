package com.sap.bfx.maven.common.definition;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.sap.bfx.definition.ElementDefinition;

@Data
@AllArgsConstructor
public class MixinInfo {
    private ElementDefinition mixin;
    private List<ElementDefinition> elements;
}
