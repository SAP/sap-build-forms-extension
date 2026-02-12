package com.sap.bfx.definition;

import java.io.IOException;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class ElementDefinitionDeserializer extends StdDeserializer<ElementDefinition> {

    public ElementDefinitionDeserializer() {
        super(ElementDefinition.class);
    }

    /**
     * @param jp
     * @param deserializationContext
     * @return
     * @throws IOException
     * @throws JacksonException
     */
    @Override
    public ElementDefinition deserialize(JsonParser jp, DeserializationContext deserializationContext)
            throws IOException, JacksonException {
        try {
            final var ed = new ElementDefinition();
            final JsonNode node = jp.getCodec().readTree(jp);

            // now read the attributes as default ...
            DeserializationHelper.readElementDefinition(node, ed);
            // .. and return the element-definition
            return ed;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }
}
