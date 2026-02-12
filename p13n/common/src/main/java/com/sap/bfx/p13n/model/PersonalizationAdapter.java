package com.sap.bfx.p13n.model;

import java.util.Locale;

/**
 * Adapter interface for Personalization operations.
 */
public interface PersonalizationAdapter {
    /**
     * Checks the validity of the given Personalization object.
     *
     * @param personalization
     * @return
     */
    boolean check(Personalization personalization);

    /**
     * Queries and retrieves a Personalization based on the provided object and locale.
     *
     * @param personalization
     * @param locale
     * @return
     */
    Personalization query(Personalization personalization, Locale locale);
}