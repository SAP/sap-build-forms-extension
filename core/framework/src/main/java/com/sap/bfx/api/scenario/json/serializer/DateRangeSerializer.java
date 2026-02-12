package com.sap.bfx.api.scenario.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.definition.DateRange;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class DateRangeSerializer extends StdSerializer<DateRange> {

    public DateRangeSerializer() {
        super(DateRange.class);
    }

    @Override
    public void serialize(DateRange dateRange, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();
        try {
            Map<String, String> rangeMap = this.convertDateRange(dateRange);
            rangeMap.forEach((key, value) -> {
                try {
                    jgen.writeStringField(key, value);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } finally {
            jgen.writeEndObject();
        }
    }

    private Map<String, String> convertDateRange(DateRange tempDateRange) {
        Map<String, String> targetMap = new HashMap<>();
        targetMap.put("from", tempDateRange.getFrom().format(Constants.D_FORMATTER));
        targetMap.put("to", tempDateRange.getTo().format(Constants.D_FORMATTER));
        return targetMap;
    }
}
