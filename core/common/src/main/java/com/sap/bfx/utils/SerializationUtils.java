package com.sap.bfx.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.sap.bfx.exception.ExceptionUtils;
import org.apache.commons.beanutils.PropertyUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for serialization
 */
public final class SerializationUtils {

    private SerializationUtils() {
    }

    /**
     * @param node
     * @param bean
     * @param name
     * @param defaultValue
     */
    public static void setStringProp(final JsonNode node, final Object bean, final String name,
                                     final String defaultValue) {
        SerializationUtils.setStringProp(node, bean, name, null, defaultValue);
    }

    /**
     * @param node
     * @param bean
     * @param nodeElementName
     * @param propertyName
     * @param defaultValue
     */
    public static void setStringProp(final JsonNode node, final Object bean,
                                     final String nodeElementName, String propertyName, final String defaultValue) {

        if (propertyName == null) {
            propertyName = nodeElementName;
        }

        final var sn = node.get(nodeElementName);
        try {
            PropertyUtils.setProperty(bean, propertyName, sn != null ? sn.asText(defaultValue) : defaultValue);
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * @param node
     * @param bean
     * @param name
     * @param defaultValue
     */
    public static void setBooleanProp(final JsonNode node, final Object bean, final String name,
                                      final Boolean defaultValue) {
        SerializationUtils.setBooleanProp(node, bean, name, null, defaultValue);
    }

    /**
     * @param node
     * @param bean
     * @param nodeElementName
     * @param propertyName
     * @param defaultValue
     */
    public static void setBooleanProp(final JsonNode node, final Object bean,
                                      final String nodeElementName, String propertyName, final Boolean defaultValue) {

        if (propertyName == null) {
            propertyName = nodeElementName;
        }

        final var sn = node.get(nodeElementName);
        try {
            PropertyUtils.setProperty(bean, propertyName, sn != null ? sn.asBoolean(defaultValue) : defaultValue);
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * @param node
     * @param bean
     * @param name
     * @param defaultValue
     */
    public static void setIntProp(final JsonNode node, final Object bean, final String name,
                                  final Integer defaultValue) {
        SerializationUtils.setIntProp(node, bean, name, null, defaultValue);
    }

    /**
     * @param node
     * @param bean
     * @param nodeElementName
     * @param propertyName
     * @param defaultValue
     */
    public static void setIntProp(final JsonNode node, final Object bean,
                                  final String nodeElementName, String propertyName, final Integer defaultValue) {

        if (propertyName == null) {
            propertyName = nodeElementName;
        }

        final var sn = node.get(nodeElementName);
        try {
            PropertyUtils.setProperty(bean, propertyName, sn != null ? sn.asInt(defaultValue) : defaultValue);
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * @param node
     * @param bean
     * @param name
     * @param mapper
     */
    public static void setMappedProp(final JsonNode node, final Object bean, final String name,
                                     final ValueMapper mapper) {
        SerializationUtils.setMappedProp(node, bean, name, null, mapper);
    }

    /**
     * @param node
     * @param bean
     * @param nodeElementName
     * @param propertyName
     * @param mapper
     */
    public static void setMappedProp(final JsonNode node, final Object bean, final String nodeElementName,
                                     String propertyName, final ValueMapper mapper) {

        if (propertyName == null) {
            propertyName = nodeElementName;
        }

        final var sn = node.get(nodeElementName);
        final var value = mapper.map(sn == null ? null : sn.asText());
        try {
            PropertyUtils.setProperty(bean, propertyName, value);
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * @param node
     * @param bean
     * @param name
     */
    public static void setMapProp(final JsonNode node, final Object bean, final String name) {
        setMapProp(node, bean, name, null);
    }

    /**
     * @param node
     * @param bean
     * @param nodeElementName
     * @param propertyName
     */
    public static void setMapProp(final JsonNode node, final Object bean, final String nodeElementName,
                                  String propertyName) {

        if (propertyName == null) {
            propertyName = nodeElementName;
        }

        try {
            var map = (Map<String, Object>) PropertyUtils.getProperty(bean, propertyName);
            if (map == null) {
                map = new HashMap<String, Object>();
                PropertyUtils.setProperty(bean, propertyName, map);
            }

            map.clear();
            for (var it = node.get(nodeElementName).fields(); it.hasNext(); ) {
                var element = it.next();
                map.put(element.getKey(), element.getValue().asText());
            }

            PropertyUtils.setProperty(bean, propertyName, map);
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * @param node
     * @param bean
     * @param name
     * @ Exception
     */
    public static void setObjectProp(final JsonNode node, final Object bean, final String name) {
        setObjectProp(node, bean, name, null);
    }

    /**
     * @param node
     * @param bean
     * @param nodeElementName
     * @param propertyName
     */
    public static void setObjectProp(final JsonNode node, final Object bean, final String nodeElementName,
                                     String propertyName) {
        if (propertyName == null) {
            propertyName = nodeElementName;
        }

        var obj = node.get(nodeElementName);
        try {
            PropertyUtils.setProperty(bean, propertyName, obj);
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }

    /**
     * Get property value as string
     *
     * @param node     JSON node
     * @param propName property name
     * @return returns property value or null if not found
     */
    public static String getPropText(final JsonNode node, final String propName) {
        var obj = node.get(propName);
        if (obj == null) {
            return null;
        }
        return obj.asText();
    }

    /**
     * Get property value as Integer
     *
     * @param node     JSON node
     * @param propName property name
     * @return returns property value or null if not found
     */
    public static Integer getPropInt(final JsonNode node, final String propName) {
        var obj = node.get(propName);
        if (obj == null) {
            return null;
        }
        return obj.asInt();
    }

    /**
     * Get property value as Boolean
     *
     * @param node     JSON node
     * @param propName property name
     * @return returns property value or null if not found
     */
    public static Boolean getPropBoolean(final JsonNode node, final String propName) {
        var obj = node.get(propName);
        if (obj == null) {
            return null;
        }
        return obj.asBoolean();
    }

    /**
     * Get property value as Long
     *
     * @param node     JSON node
     * @param propName property name
     * @return returns property value or null if not found
     */
    public static Long getPropLong(final JsonNode node, final String propName) {
        var obj = node.get(propName);
        if (obj == null) {
            return null;
        }
        return obj.asLong();
    }

    /**
     * Get property value as Instant
     *
     * @param node     JSON node
     * @param propName property name
     * @return returns property value or null if not found
     */
    public static Instant getPropInstant(final JsonNode node, final String propName) {
        var obj = node.get(propName);
        if (obj == null) {
            return null;
        }
        return Instant.ofEpochMilli(obj.asLong());
    }

    /**
     *
     */
    public interface ValueMapper {
        Object map(String input);
    }
}