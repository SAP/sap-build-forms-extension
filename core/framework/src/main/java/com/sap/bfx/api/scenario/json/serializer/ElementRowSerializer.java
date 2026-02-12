package com.sap.bfx.api.scenario.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.session.ElementRow;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ElementRowSerializer extends StdSerializer<ElementRow> {

    public ElementRowSerializer() {
        super(ElementRow.class);
    }

    @Override
    public void serialize(ElementRow elementRow, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();
        try {
            Map<String, Object> elementRowMap = this.convertElementRow(elementRow);
            elementRowMap.forEach((key, value) -> {
                try {
                    jgen.writeObjectField(key, value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } finally {
            jgen.writeEndObject();
        }
    }

    private Map<String, Object> convertElementRow(ElementRow elementRow) {
        Map<String, Object> targetMap = new HashMap<>();
        elementRow.getElements().values().forEach(ele -> {
            targetMap.put(ele.getName(), ele.getValue());
        });
        return targetMap;
    }
}
