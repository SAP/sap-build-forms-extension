package com.sap.bfx.utils;

import java.util.Arrays;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;

/**
 * Helper class for Enum deserialization
 *
 */
public class EnumUtils {

	/**
	 * 
	 * @param <T>
	 * @param e
	 * @param id
	 * @return
	 */
    public static <T extends Enum<T> & Identifier> Optional<T> valueById(Class<T> e, final String id) {
        return Arrays.stream(e.getEnumConstants()).filter(it -> StringUtils.equalsIgnoreCase(
                it.getIdentifier(), id)).findFirst();
    }

    /**
     * @param e
     * @param id
     * @param defaultValue
     * @param <T>
     * @return
     */
    public static <T extends Enum<T> & Identifier> T valueById(Class<T> e, final String id, T defaultValue) {
        var opt = Arrays.stream(e.getEnumConstants()).filter(it -> StringUtils.equalsIgnoreCase(
                it.getIdentifier(), id)).findFirst();
        return opt.orElse(defaultValue);
    }
}
