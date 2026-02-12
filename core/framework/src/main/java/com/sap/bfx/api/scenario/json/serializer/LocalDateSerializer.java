package com.sap.bfx.api.scenario.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.time.LocalDate;

public class LocalDateSerializer extends StdSerializer<LocalDate> {

    public LocalDateSerializer() {
        super(LocalDate.class);
    }

    @Override
    public void serialize(LocalDate localDate, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeRawValue(localDate.toString());
    }
}
