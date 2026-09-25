package com.sap.bfx.api.scenario.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.api.scenario.json.ProcessStateResponse;
import com.sap.bfx.api.scenario.json.TriggerEventResponse;

import java.io.IOException;

@SuppressWarnings("rawtypes")
public class TriggerEventResponseSerializer extends StdSerializer<TriggerEventResponse> {

    public TriggerEventResponseSerializer() {
        super(TriggerEventResponse.class);
    }

    @Override
    public void serialize(TriggerEventResponse triggerEventResponse, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();
        try {
            jgen.writeStringField("statusCode", (null != triggerEventResponse.getStatusCode()) ? triggerEventResponse.getStatusCode() : "n/a");
            jgen.writeStringField("statusMessage", (null != triggerEventResponse.getStatusMessage()) ? triggerEventResponse.getStatusMessage() : "n/a");
            if (null != triggerEventResponse.getParameters() && !triggerEventResponse.getParameters().isEmpty()) {
                jgen.writeObjectField("parameters", triggerEventResponse.getParameters());
            }
            if (null != triggerEventResponse.getFields() &&  !triggerEventResponse.getFields().isEmpty()) {
                jgen.writeObjectField("fields", triggerEventResponse.getFields());
            }
        } finally {
            jgen.writeEndObject();
        }
    }
}
