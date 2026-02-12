package com.sap.bfx.definition;


import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;

import static com.sap.bfx.definition.DefinitionNames.NM_ACCESS_OBJECT;
import static com.sap.bfx.utils.SerializationUtils.setStringProp;

@Slf4j
public class MixinDefinitionDeserializer<T extends MixinDefinition> extends AbstractStructureDefinitionDeserializer<T> {

    /**
     * @param readTexts
     */
    public MixinDefinitionDeserializer(final boolean readTexts) {
        super((Class<T>) MixinDefinition.class, readTexts);
    }

    /**
     * @param cls
     */
    protected MixinDefinitionDeserializer(final Class<T> cls) {
        super(cls, false);

    }

    /**
     * @param obj
     * @param node
     */
    @Override
    protected void readProperties(final T obj, final JsonNode node) {
        super.readProperties(obj, node);

        setStringProp(node, obj, NM_ACCESS_OBJECT, "accessObjectName", null);
    }
}

