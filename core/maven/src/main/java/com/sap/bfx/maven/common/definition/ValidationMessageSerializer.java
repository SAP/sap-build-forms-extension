package com.sap.bfx.maven.common.definition;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.maven.common.AbstractProcessor;

import java.io.IOException;

public class ValidationMessageSerializer extends StdSerializer<AbstractProcessor.ValidationMessage> {

    /**
     *
     */
    public ValidationMessageSerializer() {
        super(AbstractProcessor.ValidationMessage.class);
    }

    /**
     * @param value    Value to serialize; can <b>not</b> be null.
     * @param gen      Generator used to output resulting Json content
     * @param provider Provider that can be used to get serializers for
     *                 serializing Objects value contains, if any.
     * @throws IOException
     */
    @Override
    public void serialize(AbstractProcessor.ValidationMessage value, JsonGenerator gen, SerializerProvider provider)
            throws IOException {

        gen.writeStartObject();

        if (value.getDef() != null) {
            gen.writeStringField("defName", value.getDef().getName());
            gen.writeNumberField("defVersion", value.getDef().getVersion());
        }
        if (value.getSeverity() != null) {
            gen.writeStringField("severity", value.getSeverity().getIdentifier());
        }
        if (value.getElement() != null) {
            gen.writeStringField("elementId", value.getElement().getName());
        }
        if (value.getMessage() != null) {
            gen.writeStringField("message", value.getMessage());
        } if (value.getElementPart() != null) {
            gen.writeStringField("elementPart", value.getElementPart().getIdentifier());
        }

        gen.writeEndObject();
    }

}
