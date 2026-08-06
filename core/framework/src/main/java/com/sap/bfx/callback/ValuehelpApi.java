package com.sap.bfx.callback;

import com.sap.bfx.valuehelp.ValueHelpData;

import java.util.Locale;

/**
 * Interface for Value Help API that provides methods to interact with value help data. It will be part of the
 * callback context so that scenarios can access the connected value-help engine in lifecycle-hooks and/or
 * event handlers to request valuehelps (e.g. for filtering and returning them as dynamic value-helps.
 */
public interface ValuehelpApi extends Api {

    /**
     * Finds the latest value help data for a given ID and locale.
     *
     * @param id     identifier of the valuehelp
     * @param locale language of the valuehelp
     * @return a structure with value-help definition and current data
     */
    ValueHelpData findLatest(String id, Locale locale);
}
