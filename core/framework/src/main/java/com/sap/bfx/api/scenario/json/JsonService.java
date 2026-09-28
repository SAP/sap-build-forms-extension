package com.sap.bfx.api.scenario.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.sap.bfx.api.scenario.json.serializer.*;
import com.sap.bfx.definition.DateRange;
import com.sap.bfx.session.ElementRow;
import com.sap.bfx.session.MoneyAmount;
import com.sap.bfx.session.Table;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class JsonService {

    /**
     * The mapper.
     */
    final private ObjectMapper objMapper;

    /**
     * Instantiates a new json service.
     */
    public JsonService() {
        objMapper = JsonMapper.builder().build();
        final SimpleModule module = new SimpleModule();
        module.addSerializer(DateRange.class, new DateRangeSerializer());
        module.addSerializer(ElementRow.class, new ElementRowSerializer());
        module.addSerializer(Table.class, new TableSerializer());
        module.addSerializer(FieldListResponse.class, new FieldListResponseSerializer());
        module.addSerializer(FieldResponse.class, new FieldResponseSerializer());
        module.addSerializer(LocalDate.class, new LocalDateSerializer());
        module.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());
        module.addSerializer(LocalTime.class, new LocalTimeSerializer());
        module.addSerializer(MoneyAmount.class, new MoneyAmountSerializer());
        module.addSerializer(ProcessStateResponse.class, new ProcessStateResponseSerializer());
        module.addSerializer(TriggerEventResponse.class, new TriggerEventResponseSerializer());
        module.addSerializer(ParameterItem.class, new ParameterItemSerializer());
        objMapper.registerModule(module);
    }

    public void send(FieldResponse<?> fieldResponse, ServerHttpResponse response, MediaType mediaType) throws Exception {
        response.getHeaders().add("content-type", mediaType.toString());
        final ObjectWriter objWriter = objMapper.writer();
        objWriter.writeValue(response.getBody(), fieldResponse);
    }

    public void send(FieldListResponse fieldListResponse, ServerHttpResponse response, MediaType mediaType) throws Exception {
        response.getHeaders().add("content-type", mediaType.toString());
        final ObjectWriter objWriter = objMapper.writer();
        objWriter.writeValue(response.getBody(), fieldListResponse);
    }

    public void send(ProcessStateResponse processStateResponse, ServerHttpResponse response, MediaType mediaType) throws Exception {
        response.getHeaders().add("content-type", mediaType.toString());
        final ObjectWriter objWriter = objMapper.writer();
        objWriter.writeValue(response.getBody(), processStateResponse);
    }

    public void send(TriggerEventResponse triggerEventResponse, ServerHttpResponse response, MediaType mediaType) throws Exception {
        response.getHeaders().add("content-type", mediaType.toString());
        final ObjectWriter objWriter = objMapper.writer();
        objWriter.writeValue(response.getBody(), triggerEventResponse);
    }

}
