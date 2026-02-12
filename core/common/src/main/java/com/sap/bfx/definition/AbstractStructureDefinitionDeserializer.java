package com.sap.bfx.definition;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import static com.sap.bfx.definition.DefinitionNames.*;
import static com.sap.bfx.utils.SerializationUtils.setIntProp;
import static com.sap.bfx.utils.SerializationUtils.setStringProp;

public class AbstractStructureDefinitionDeserializer<T extends AbstractStructureDefinition> extends StdDeserializer<T> {

    private final boolean readTexts;
    private final Class<T> cls;

    /**
     * @param readTexts
     */
    protected AbstractStructureDefinitionDeserializer(final Class<T> cls, final boolean readTexts) {
        super(cls);

        this.readTexts = readTexts;
        this.cls = cls;
    }

    /**
     * @param jp  Parsed used for reading JSON content
     * @param ctx Context that can be used to access information about
     *            this deserialization activity.
     * @return
     * @throws IOException
     */
    @Override
    public T deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
        try {
            final var sd = cls.getDeclaredConstructor().newInstance();

            final JsonNode node = jp.getCodec().readTree(jp);

            // now read the attributes as default ...
            readProperties(sd, node);

            if (readTexts) {
                sd.getTexts().clear();
                final var texts = node.get(NM_TEXTS);
                if (texts != null) {
                    texts.fieldNames().forEachRemaining(language -> {
                        final var keyValueMap = new HashMap<String, String>();
                        final var kv = texts.get(language);
                        kv.fieldNames().forEachRemaining(key -> {
                            keyValueMap.put(key, kv.get(key).asText());
                        });
                        sd.getTexts().put(new Locale(language), keyValueMap);
                    });
                }
            }

            // read all elements
            DeserializationHelper.readElementsDefinitions(node, sd.getElements(), NM_ELEMENTS);

            return sd;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * @param obj
     * @param node
     */
    protected void readProperties(final T obj, final JsonNode node) {
        setStringProp(node, obj, NM_NAME, null);
        setIntProp(node, obj, NM_VERSION, -1);
        setStringProp(node, obj, NM_BASE_PACKAGE, "");
    }
}
