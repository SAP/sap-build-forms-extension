package com.sap.bfx.callback;

/**
 * Interface for Value Help API that provides methods to interact with value help data. It will be part of the
 * callback context so that scenarios can access the connected value-help engine in lifecycle-hooks and/or
 * event handlers to request valuehelps (e.g. for filtering and returning them as dynamic value-helps.
 */
public interface ValuehelpApi extends Api {
}
