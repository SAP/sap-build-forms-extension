package com.sap.bfx.definition;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import java.util.Locale;

import static com.sap.bfx.definition.DefinitionNames.*;
import static com.sap.bfx.utils.SerializationUtils.*;

@Slf4j
public class ScenarioDefinitionDeserializer<T extends ScenarioDefinition>
        extends AbstractStructureDefinitionDeserializer<T> {

    /**
     * @param readTexts
     */
    public ScenarioDefinitionDeserializer(final boolean readTexts) {
        super((Class<T>) ScenarioDefinition.class, readTexts);
    }

    /**
     *
     */
    protected ScenarioDefinitionDeserializer(final Class<T> cls, final boolean readTexts) {
        super(cls, readTexts);

    }

    /**
     * @param obj
     * @param node
     */
    @Override
    protected void readProperties(final T obj, final JsonNode node) {
        super.readProperties(obj, node);

        setBooleanProp(node, obj, NM_ACTIVE, Boolean.FALSE);
        setStringProp(node, obj, NM_ACCESS_OBJECT, "accessObjectName", null);
        setStringProp(node, obj, NM_ROOT_ELEMENT, "rootElementName", null);
        setStringProp(node, obj, NM_ROOT_ELEMENT_KEY, "");
        setMappedProp(node, obj, NM_DEFAULT_LOCALE, "defaultLocale", Locale::new);
    }
}
