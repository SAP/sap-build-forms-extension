package com.sap.bfx.api.scenario.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.api.scenario.json.FieldListResponse;
import com.sap.bfx.api.scenario.json.ProcessStateResponse;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public class ProcessStateResponseSerializer extends StdSerializer<ProcessStateResponse> {

    public ProcessStateResponseSerializer() {
        super(ProcessStateResponse.class);
    }

    @Override
    public void serialize(ProcessStateResponse processStateResponse, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();
        try {
            jgen.writeStringField("statusCode", (null != processStateResponse.getStatusCode()) ? processStateResponse.getStatusCode() : "n/a");
            jgen.writeStringField("statusMessage", (null != processStateResponse.getStatusMessage()) ? processStateResponse.getStatusMessage() : "n/a");
        } finally {
            jgen.writeEndObject();
        }
    }
}
