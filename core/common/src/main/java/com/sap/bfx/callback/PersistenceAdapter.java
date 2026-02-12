package com.sap.bfx.callback;

import com.sap.bfx.definition.FormAttributes;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;

/**
 * Persistence Adapter Interface
 */
public interface PersistenceAdapter extends Adapter {

    /**
     * Load form by id
     *
     * @param id form id
     * @return pair of form attributes and form data as input stream
     */
    Pair<FormAttributes, InputStream> loadById(final String id);

    /**
     * Load form by scenario name and reference id
     *
     * @param scenarioName
     * @param refId
     * @return
     */
    Pair<FormAttributes, InputStream> loadByRefId(final String scenarioName, final String refId);

    /**
     * Save form
     *
     * @param formAttributes form attributes
     * @param data           form data as input stream
     * @param isNew          indicates if the form is new or an update
     */
    void save(final FormAttributes formAttributes, final InputStream data, final boolean isNew);

    /**
     * Delete form by id
     *
     * @param id form id
     */
    void delete(final String id);

}
