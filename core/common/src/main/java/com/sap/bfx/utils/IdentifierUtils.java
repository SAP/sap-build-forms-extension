package com.sap.bfx.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for handling identifier transformations and key generation.
 * This class provides methods to convert strings to PascalCase and generate unique keys.
 */
public class IdentifierUtils {

    /**
     * Converts the source string to PascalCase, splitting on spaces, hyphens, and underscores,
     * capitalizing the first letter of each word and preserving inner casing.
     *
     * @param source the input string to convert
     * @return the PascalCase version of the input string
     */
    public static String toPascalCase(String source) {
        if (StringUtils.containsAny(source, ' ', '-', '_')) {
            String[] words = source.split("[ \\-_]+");
            StringBuilder sb = new StringBuilder();
            for (String word : words) {
                if (word.isEmpty()) continue;
                sb.append(Character.toUpperCase(word.charAt(0)));
                sb.append(word.substring(1));
            }
            return sb.toString();
        }
        return StringUtils.capitalize(source);
    }

    /**
     * Generates a unique key based on the source string.
     * The key is created by converting the source to PascalCase and then hashing it with MD5.
     *
     * @param source the input string to generate a key from
     * @return a unique key as a String
     */
    public static String key(final String source) {
        var result = IdentifierUtils.toPascalCase(source);
        result = DigestUtils.md5Hex(result);
        return result;
    }
}
