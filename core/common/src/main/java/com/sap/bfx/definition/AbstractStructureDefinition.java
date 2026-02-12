package com.sap.bfx.definition;

import lombok.Data;

import java.util.*;

@Data
public abstract class AbstractStructureDefinition {
    private String name;
    private int version;
    private List<ElementDefinition> elements = new ArrayList<>();
    private Map<Locale, Map<String, String>> texts = new HashMap<>();
    private String accessObjectName;
    private String basePackage;

}
