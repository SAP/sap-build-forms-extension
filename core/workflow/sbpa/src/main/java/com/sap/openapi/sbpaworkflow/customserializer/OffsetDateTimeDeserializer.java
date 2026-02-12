package com.sap.openapi.sbpaworkflow.customserializer;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;

public class OffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    @Override
    public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        String source = jsonParser.getText();
        // e.g. content is '/Date(1718229600000)'
        if (source.matches("^\\/Date\\(\\d+\\)\\/$")) {
            int startIndex = source.indexOf("(") + 1;
            int endIndex = source.indexOf(")");
            long time = Long.parseLong((String) source.subSequence(startIndex, endIndex));
            Date theDate = new Date();
            theDate.setTime(time);
            return theDate.toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
        }
        return null;
    }

}
