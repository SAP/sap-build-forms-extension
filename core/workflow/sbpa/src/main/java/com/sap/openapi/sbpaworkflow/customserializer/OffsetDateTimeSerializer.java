package com.sap.openapi.sbpaworkflow.customserializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

public class OffsetDateTimeSerializer extends JsonSerializer<OffsetDateTime> {

    @Override
    public void serialize(OffsetDateTime offsetDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (null != offsetDateTime) {
            // e.g. content is '/Date(1718229600000)'
            long millis = Date.from(offsetDateTime.toLocalDateTime().atZone(ZoneId.systemDefault()).toInstant()).getTime();
            String tempOutput = MessageFormat.format("\"\\/Date({0})\\/\"", String.valueOf(millis));
            jsonGenerator.writeRawValue(tempOutput);
        } else {
            jsonGenerator.writeNull();
        }
    }
}
