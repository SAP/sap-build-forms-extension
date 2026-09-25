package com.sap.bfx.api.scenario.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.api.scenario.json.FieldResponse;
import com.sap.bfx.definition.DateRange;
import com.sap.bfx.session.ElementRow;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Map;

@SuppressWarnings("rawtypes")
public class FieldResponseSerializer extends StdSerializer<FieldResponse> {

    public FieldResponseSerializer() {
        super(FieldResponse.class);
    }

    @Override
    public void serialize(FieldResponse fieldResponse, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        if (null != fieldResponse.getScenarioFieldName()) {
            this.serializeWithFieldName(fieldResponse.getScenarioFieldName(), fieldResponse.getFieldValue(), jgen, serializerProvider);
        } else {
            jgen.writeObject(fieldResponse.getFieldValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void serializeWithFieldName(String fieldName, Object fieldObj, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject();
        try {
            jgen.writeStringField("scenarioFieldName", fieldName);
            if (fieldObj instanceof LocalDate || fieldObj instanceof LocalTime) {
                jgen.writeStringField("fieldValue", fieldObj.toString());
            } else if (fieldObj instanceof LocalDateTime tempDateTime) {
                jgen.writeStringField("fieldValue", tempDateTime.format(Constants.DT_FORMATTER));
            } else if (fieldObj instanceof DateRange tempDateRange) {
                jgen.writeObjectField("fieldValue", tempDateRange);
            } else if (fieldObj instanceof BigDecimal tempBigDecimal) {
                jgen.writeNumberField("fieldValue", tempBigDecimal);
            } else if (fieldObj instanceof Integer tempInteger) {
                jgen.writeNumberField("fieldValue", tempInteger);
            } else if (fieldObj instanceof String tempString) {
                jgen.writeStringField("fieldValue", tempString);
            } else if (fieldObj instanceof Boolean tempBoolean) {
                jgen.writeBooleanField("fieldValue", tempBoolean);
            } else if (fieldObj instanceof Collection tempColl) {
                jgen.writeArrayFieldStart("fieldValue");
                try {
                    tempColl.forEach(item -> {
                        try {
                            jgen.writeObject(item);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                } finally {
                    jgen.writeEndArray();
                }
            } else if (fieldObj instanceof Map tempMap) {
                jgen.writeObjectField("fieldValue", tempMap);
            } else if (fieldObj instanceof ElementRow tempElementRow) {
                jgen.writeObjectField("fieldValue", tempElementRow);
            } else {
                throw new RuntimeException("Unhandled type " + fieldObj.getClass().getName() + " in FieldResponseSerializer");
            }
        } finally {
            jgen.writeEndObject();
        }
    }
}
