package com.sap.bfx.utils;

import org.apache.commons.lang3.StringUtils;

public final class FileUtils {

    /**
     * Avoid external construction of objects
     */
    private FileUtils() {

    }

    /**
     * @param fName
     * @return
     */
    public static boolean isYamlFile(final String fName) {
        return StringUtils.endsWithIgnoreCase(fName, ".yaml")
                || StringUtils.endsWithIgnoreCase(fName, ".yml");
    }

    /**
     * @param fName
     * @return
     */
    public static boolean isPropertyFile(final String fName) {
        return StringUtils.endsWithIgnoreCase(fName, ".properties");
    }
}
