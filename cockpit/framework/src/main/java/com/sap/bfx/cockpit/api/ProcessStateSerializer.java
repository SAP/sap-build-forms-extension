package com.sap.bfx.cockpit.api;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.definition.ProcessState;

import java.io.IOException;

/**
 * Custom serializer for ProcessState enum to convert enum values to lowercase strings.
 */
public class ProcessStateSerializer extends StdSerializer<ProcessState> {
    /**
     *
     */
    public ProcessStateSerializer() {
        super(ProcessState.class);
    }

    /**
     * @param processState
     * @param gen
     * @param sp
     * @throws IOException
     */
    @Override
    public void serialize(ProcessState processState, JsonGenerator gen, SerializerProvider sp) throws IOException {
        if (processState != null) {
            gen.writeString(processState.getIdentifier().toLowerCase());
        } else {
            gen.writeNull();
        }
    }
}
