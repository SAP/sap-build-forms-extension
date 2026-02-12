package com.sap.bfx.api.scenario.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalTime;

public class LocalTimeSerializer extends StdSerializer<LocalTime> {

    public LocalTimeSerializer() {
        super(LocalTime.class);
    }

    @Override
    public void serialize(LocalTime localTime, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeRawValue(localTime.toString());
    }
}
