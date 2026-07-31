package com.sap.bfx.utils;

import java.util.UUID;

/**
 * Utility class for generating UUIDs.
 * This class provides a method to generate a random UUID and return it as a string without dashes.
 */
public final class UUIDUtils {

    /**
     * Private constructor to prevent instantiation of the utility class.
     */
    private UUIDUtils() {
    }

    /**
     * Generates a random UUID and returns it as a string without dashes.
     *
     * @return a string representation of a random UUID without dashes
     */
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

}
