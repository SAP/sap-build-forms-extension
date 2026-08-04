package com.sap.bfx.session;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sap.bfx.p13n.Settings;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

public class SessionRedisSerializer implements RedisSerializer<Session> {

    private final ObjectMapper om;
    private final FormsService formsService;

    /**
     * Constructor
     *
     * @param formsService Reference to FormsService
     */
    public SessionRedisSerializer(final FormsService formsService) {
        om = JsonMapper.builder().addModule(new JavaTimeModule()).build();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY);
        final var module = new SimpleModule();
        module.addSerializer(Session.class, new SessionSerializer());
        module.addSerializer(Form.class, new FormSerializer());
        module.addDeserializer(Session.class, new SessionDeserializer());
        om.registerModule(module);

        this.formsService = formsService;
    }

    @Override
    public byte[] serialize(Session t) throws SerializationException {
        try {
            return om.writeValueAsBytes(t);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    @Override
    public Session deserialize(byte[] bytes) throws SerializationException {

        if (bytes == null) {
            return null;
        }

        try {
            return om.readValue(bytes, Session.class);
        } catch (Exception e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    /**
     *
     */
    class SessionSerializer extends StdSerializer<Session> {

        public SessionSerializer() {
            super(Session.class);
        }

        @Override
        public void serialize(Session value, JsonGenerator gen, SerializerProvider provider) throws IOException {

            gen.writeStartObject();

            gen.writeStringField(FormUtils.NM_ID, value.getId());
            gen.writeStringField(FormUtils.NM_LOCALE, value.getLocale().toString());
            gen.writeStringField(FormUtils.NM_STATE, value.getDisplayState());
            gen.writeObjectField(FormUtils.NM_FORM, value.getForm());
            gen.writeStringField(FormUtils.NM_USERNAME, value.getUserName());
            if (StringUtils.isNotBlank(value.getTaskInstanceId())) {
                gen.writeStringField(FormUtils.NM_TASK_INSTANCE_ID, value.getTaskInstanceId());
            } else {
                gen.writeNullField(FormUtils.NM_TASK_INSTANCE_ID);
            }
            if (StringUtils.isNotBlank(value.getForm().getId())) {
                gen.writeStringField(FormUtils.NM_FORM_ID, value.getForm().getId());
            } else {
                gen.writeNullField(FormUtils.NM_FORM_ID);
            }
            gen.writeNumberField(FormUtils.NM_FORM_VERSION, value.getForm().getVersion());
            if (StringUtils.isNotBlank(value.getForm().getScenarioName())) {
                gen.writeStringField(FormUtils.NM_FORM_SCENARIO_NAME, value.getForm().getScenarioName());
            } else {
                gen.writeNullField(FormUtils.NM_FORM_SCENARIO_NAME);
            }
            gen.writeNumberField(FormUtils.NM_FORM_SCENARIO_VERSION, value.getForm().getScenarioVersion());
            gen.writeObjectField(FormUtils.NM_ADD_DATA, value.getAdditionalData());

            gen.writeArrayFieldStart(FormUtils.NM_PERSONALIZATIONS);
            if (value.getSettings() != null) {
                for (Settings obj : value.getSettings()) {
                    gen.writeStartObject();
                    gen.writeStringField(FormUtils.NM_PERSONALIZATIONS_ID, obj.getId());
                    gen.writeStringField(FormUtils.NM_PERSONALIZATIONS_USER, obj.getUser());
                    gen.writeStringField(FormUtils.NM_PERSONALIZATIONS_KEY, obj.getKey());
                    gen.writeStringField(FormUtils.NM_PERSONALIZATIONS_APP, obj.getApp());
                    gen.writeStringField(FormUtils.NM_PERSONALIZATIONS_ENCODING, obj.getEncoding());
                    gen.writeStringField(FormUtils.NM_PERSONALIZATIONS_VALUE, obj.getValue());
                    gen.writeEndObject();
                }
            }
            gen.writeEndArray();

            gen.writeEndObject();
        }
    }

    /**
     *
     */
    class SessionDeserializer extends StdDeserializer<Session> {

        protected SessionDeserializer() {
            super(Session.class);
        }

        @Override
        public Session deserialize(JsonParser jp, DeserializationContext ctx) throws IOException {
            final var result = new Session();
            final JsonNode node = jp.getCodec().readTree(jp);

            result.setId(node.get(FormUtils.NM_ID).asText());
            result.setLocale(new Locale(node.get(FormUtils.NM_LOCALE).asText()));
            result.setDisplayState(node.get(FormUtils.NM_STATE).asText());

            result.setForm(formsService.readForm(node.get(FormUtils.NM_FORM)));
            result.setUserName(node.get(FormUtils.NM_USERNAME).asText());
            result.setTaskInstanceId((!node.get(FormUtils.NM_TASK_INSTANCE_ID).isNull()) ?
                    node.get(FormUtils.NM_TASK_INSTANCE_ID).asText() : null);
            result.setAdditionalData(ctx.readTreeAsValue(node.get(FormUtils.NM_ADD_DATA), HashMap.class));

            var settings = node.get(FormUtils.NM_PERSONALIZATIONS);
            var personalizations = new ArrayList<Settings>();
            if (settings.isArray()) {
                Iterator<JsonNode> elements = settings.elements();
                while (elements.hasNext()) {
                    JsonNode elementNode = elements.next();
                    personalizations.add(new Settings(elementNode.get(FormUtils.NM_PERSONALIZATIONS_ID).asText(),
                            elementNode.get(FormUtils.NM_PERSONALIZATIONS_USER).asText(),
                            elementNode.get(FormUtils.NM_PERSONALIZATIONS_KEY).asText(),
                            elementNode.get(FormUtils.NM_PERSONALIZATIONS_APP).asText(),
                            elementNode.get(FormUtils.NM_PERSONALIZATIONS_ENCODING).asText(),
                            elementNode.get(FormUtils.NM_PERSONALIZATIONS_VALUE).asText()));
                }
            }
            result.setSettings(personalizations);

            return result;
        }
    }
}