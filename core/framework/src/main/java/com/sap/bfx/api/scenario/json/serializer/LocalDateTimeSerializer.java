package com.sap.bfx.api.scenario.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDateTime;

public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

    public LocalDateTimeSerializer() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(LocalDateTime localDateTime, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeRawValue(localDateTime.format(Constants.DT_FORMATTER));
    }
}
