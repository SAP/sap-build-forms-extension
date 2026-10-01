package com.sap.bfx.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.StandardCharsets;

/**
 * SecuritySessionRedisSerializer is a custom serializer for SecuritySession objects to be stored in Redis.
 * It implements the RedisSerializer interface, providing methods for serializing and deserializing SecuritySession
 * instances.
 */
public class JsonRedisSerializer<T> implements RedisSerializer<T> {

    private final ObjectMapper om = new ObjectMapper();
    private final Class<T> clz;

    /**
     * Constructs a new JsonRedisSerializer for the specified class type.
     *
     * @param clz the class type to be serialized/deserialized
     */
    public JsonRedisSerializer(Class<T> clz) {
        this.clz = clz;
    }

    @Override
    public byte[] serialize(T value) throws SerializationException {
        try {
            final var f = om.writeValueAsBytes(value);
            return f;
        } catch (JsonProcessingException e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null) {
            return null;
        }

        try {
            // TODO(ML): This is a hack because for whatever reason there are some bytes with value 0 at the
            // beginning. They are not comming from the code, maybe from Redis?=!?!?!?!
            var f = StringUtils.trim(StringUtils.toEncodedString(bytes, StandardCharsets.UTF_8));
            return om.readValue(f, clz);
        } catch (Exception e) {
            throw new SerializationException(e.getMessage(), e);
        }
    }
}
