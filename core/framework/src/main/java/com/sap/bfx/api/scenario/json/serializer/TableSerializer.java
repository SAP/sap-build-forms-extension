package com.sap.bfx.api.scenario.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.session.Table;

import java.io.IOException;

public class TableSerializer extends StdSerializer<Table> {

    public TableSerializer() {
        super(Table.class);
    }

    @Override
    public void serialize(Table table, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartArray();
        try {
            table.getData().values().forEach(elementRow -> {
                try {
                    jgen.writeObject(elementRow);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } finally {
            jgen.writeEndArray();
        }
    }
}
