package com.sap.bfx.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.CaseUtils;

/**
 * Utility class for handling identifier transformations and key generation.
 * This class provides methods to convert strings to camel case, capitalize them,
 * and generate unique keys based on the transformed strings.
 */
public class IdentifierUtils {

    /**
     * Converts the source string to camel case.
     * This method replaces spaces, hyphens, and underscores with camel case formatting.
     *
     * @param source the input string to convert
     * @return the camel case version of the input string
     */
    public static String camelCase(String source) {
        if (StringUtils.containsAny(source, ' ', '-', '_')) {
            return CaseUtils.toCamelCase(source, true, ' ', '-', '_');
        }
        return source;
    }

    /**
     * Converts the source string to camel case and capitalizes the first letter.
     * This is useful for creating identifiers that follow the Java naming conventions.
     *
     * @param source the input string to convert
     * @return the capitalized camel case version of the input string
     */
    public static String capitalCamelCase(String source) {
        return StringUtils.capitalize(IdentifierUtils.camelCase(source));
    }

    /**
     * Generates a unique key based on the source string.
     * The key is created by converting the source to capital camel case and then hashing it with MD5.
     *
     * @param source the input string to generate a key from
     * @return a unique key as a String
     */
    public static String key(final String source) {
        var result = IdentifierUtils.capitalCamelCase(source);
        result = DigestUtils.md5Hex(result);
        return result;
    }
}
