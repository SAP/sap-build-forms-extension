package com.sap.bfx.callback;

import com.sap.bfx.definition.ScenarioDefinition;
import com.sap.bfx.p13n.Settings;
import com.sap.bfx.security.User;
import com.sap.bfx.session.ElementPos;

import java.util.Locale;
import java.util.Set;

/**
 * Context interface that provides access to various APIs and data related to the current session and scenario.
 *
 * @param <AC> the type of AccessClass associated with this context
 */
public interface Context<AC extends AccessClass> {

    /**
     * Get API instance. Currently, these are:
     * <ul>
     *     <it>FormsApi</it>
     *     <it>WorkflowApi</it>
     *     <it>ValuehelpApi</it>
     * </ul>
     *
     * @param apiCls - the class of the API to retrieve
     * @return An instance of the requested api. If it's not one of the listed above then an exception is thrown.
     */
    <T extends Api> T getApi(Class<T> apiCls);

    /**
     * Returns the display state that is given by the browser
     *
     * @return the display state that is given by the browser
     */
    String getDisplayState();

    /**
     * Sets the current state of the display forms
     *
     * @param state - the state of the display forms
     */
    void setDisplayState(final String state);

    /**
     * Returns the current locale of the display forms
     *
     * @return the current locale of the display forms
     */
    Locale getLocale();

    /**
     * Sets the current locale of the display forms
     *
     * @param locale
     */
    void setLocale(final Locale locale);

    /**
     * Returns the keys of the additional data from the context. Additional data is a map of key-value pairs that can
     * be used to store and retrieve custom data related to the current context. It is saved in session-store together
     * with the other session data.
     *
     * @return a set of keys for the additional data
     */
    Set<String> getAddDataKeys();

    /**
     * Returns additional data from the context
     *
     * @param key - the key for the additional data
     * @return the value of the additional data
     */
    Object addData(String key);

    /**
     * Sets additional data in the context. Additional data is saved in the session-store together with the
     * other session data. So it can be used to store data other multiple requests.
     *
     * @param key   - the key for the additional data
     * @param value - the value of the additional data
     */
    void addData(String key, Object value);

    /**
     * Deletes additional data from the context. Additional data is saved in the session-store together with the
     * other session data. So it can be used to store data other multiple requests.
     *
     * @param key - the key for the additional data
     */
    void deleteAddData(String key);

    /**
     * Returns the keys of the parameters from the context. Parameters are a map of key-value pairs that can be used to
     * store and retrieve custom data related to the current context. Parameters are only valid within the current
     * request and can be used to share information between different event-handlers/lifecycle hooks.
     *
     * @return a set of keys for the parameters
     */
    Set<String> getParamsKeys();

    /**
     * Returns parameters from the context
     *
     * @param key - the key for the parameter
     * @return the value of the parameter
     */
    Object param(String key);

    /**
     * Sets parameters in the context. Parameters are saved in the session-store together with the
     * other session data. So it can be used to store data other multiple requests. Parameters are only valid within
     * the current request and can be used to share information between different event-handlers/lifecycle hooks.
     *
     * @param key   - the key for the parameter
     * @param value - the value of the parameter
     */
    void param(String key, Object value);

    /**
     * Deletes parameters from the context. Parameters are saved in the session-store together with the
     * other session data. So it can be used to store data other multiple requests. Parameters are only valid within
     * the current request and can be used to share information between different event-handlers/lifecycle hooks.
     *
     * @param key - the key for the parameter
     */
    void deleteParam(String key);

    /**
     * Returns the access class associated with this context. The automatically generated access class allows a typed
     * access to the form data.
     *
     * @return instance of the access class
     */
    AC getData();

    /**
     * Returns the data API. This allows a generic (not typed) access to the form data.
     *
     * @return generic form data access class
     */
    DataApi getDataApi();

    /**
     * Returns the scenario definition. The scenario definition contains metadata about the form, including value
     * help IDs and other configuration information.
     *
     * @return scenario definition
     */
    ScenarioDefinition getScenarioDefinition();

    /**
     * Returns the authentication token associated with the current context. The authentication token contains
     * information about the authenticated user and their roles/permissions.
     *
     * @return access token used for authentication and authorization
     */
    Object getAuthentication();

    /**
     * Returns the user associated with the current context. The user object contains information about the
     * authenticated user, including their username, roles, and other relevant details.
     *
     * @return user object representing the authenticated user
     */
    User getUser();

    /**
     * Returns the source element position. The source element position indicates the position of the element that
     * triggered the current event or lifecycle hook. This can be used to determine which element in the form caused
     * the current context to be created.
     *
     * @return position (row, key) of the current event
     */
    ElementPos getSource();

    /**
     * Returns the personalization value for a given key. Personalization values are user-specific settings that can be
     * used to customize the behavior or appearance of the form. These values are stored in the session-store and
     * can be retrieved using this method.
     *
     * @return personalization value for the given key
     */
    Settings getP13NValue(String key);

    /**
     * Returns the task instance ID if the context is related to a task
     *
     * @return
     */
    String getTaskInstanceId();
}
