package com.sap.bfx.security;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;

/**
 * SecuritySessionSerializer is a RedisSerializer implementation for serializing and deserializing SecuritySession
 * objects.
 */
public class SecuritySessionRedisSerializer implements RedisSerializer<SecuritySession> {

    private final ObjectMapper om;

    /**
     * Constructor initializes the ObjectMapper and registers custom serializers and deserializers
     * for GrantedAuthority.
     */
    public SecuritySessionRedisSerializer() {
        om = JsonMapper.builder().build();

        final var module = new SimpleModule();
        module.addSerializer(GrantedAuthority.class, new GrantedAuthoritySerializer());
        module.addDeserializer(GrantedAuthority.class, new GrantedAuthorityDeserializer());
        om.registerModule(module);
    }

    @Override
    public byte[] serialize(SecuritySession value) throws SerializationException {
        try {
            return om.writeValueAsBytes(value);
        } catch (JsonProcessingException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    @Override
    public SecuritySession deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }

        try {
            return om.readValue(bytes, SecuritySession.class);
        } catch (Exception e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    /**
     * Custom serializer for GrantedAuthority objects.
     */
    public static class GrantedAuthoritySerializer extends StdSerializer<GrantedAuthority> {

        public GrantedAuthoritySerializer() {
            super(GrantedAuthority.class);
        }

        @Override
        public void serialize(GrantedAuthority value, com.fasterxml.jackson.core.JsonGenerator gen,
                              com.fasterxml.jackson.databind.SerializerProvider provider) throws IOException {
            gen.writeStartObject();
            gen.writeStringField("value", value.getAuthority());
            gen.writeEndObject();
        }
    }

    /**
     * Custom deserializer for GrantedAuthority objects.
     */
    public static class GrantedAuthorityDeserializer extends StdDeserializer<GrantedAuthority> {

        public GrantedAuthorityDeserializer() {
            super(GrantedAuthority.class);
        }

        @Override
        public GrantedAuthority deserialize(com.fasterxml.jackson.core.JsonParser p,
                                            com.fasterxml.jackson.databind.DeserializationContext ctxt)
                throws IOException {
            final JsonNode node = p.getCodec().readTree(p);
            return new SimpleGrantedAuthority(node.get("value").asText());
        }
    }
}