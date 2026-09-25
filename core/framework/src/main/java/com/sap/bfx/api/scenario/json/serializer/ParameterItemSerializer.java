package com.sap.bfx.api.scenario.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.api.scenario.json.ParameterItem;
import com.sap.bfx.api.scenario.json.ProcessStateResponse;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public class ParameterItemSerializer extends StdSerializer<ParameterItem> {

    public ParameterItemSerializer() {
        super(ParameterItem.class);
    }

    @Override
    public void serialize(ParameterItem parameterItem, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();
        try {
            jgen.writeStringField("key", parameterItem.getKey());
            jgen.writeObjectField("value", parameterItem.getValue());
        } finally {
            jgen.writeEndObject();
        }
    }
}
