package com.sap.bfx.api.scenario.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.session.MoneyAmount;

import java.io.IOException;
import java.time.LocalTime;

public class MoneyAmountSerializer extends StdSerializer<MoneyAmount> {

    public MoneyAmountSerializer() {
        super(MoneyAmount.class);
    }

    @Override
    public void serialize(MoneyAmount moneyAmount, JsonGenerator jgen, SerializerProvider serializerProvider) throws IOException {
        jgen.writeRawValue(moneyAmount.getCurrency() + " " + moneyAmount.getAmount().toString());
    }
}
