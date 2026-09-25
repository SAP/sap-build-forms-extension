package com.sap.bfx.api.scenario.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.api.scenario.json.FieldListResponse;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public class FieldListResponseSerializer extends StdSerializer<FieldListResponse> {

    public FieldListResponseSerializer() {
        super(FieldListResponse.class);
    }

    @Override
    public void serialize(FieldListResponse fieldListResponse, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();
        try {
            jgen.writeStringField("scenarioFieldNames", (null != fieldListResponse.getScenarioFieldNames()) ? fieldListResponse.getScenarioFieldNames() : "n/a");
            jgen.writeArrayFieldStart("fieldList");
            try {
                fieldListResponse.getFieldList().forEach(item -> {
                    try {
                        jgen.writeObject(item);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            } finally {
                jgen.writeEndArray();
            }
        } finally {
            jgen.writeEndObject();
        }
    }
}
