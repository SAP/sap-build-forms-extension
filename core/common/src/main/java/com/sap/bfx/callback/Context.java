package com.sap.bfx.callback;

import com.sap.bfx.definition.ScenarioDefinition;
import com.sap.bfx.p13n.Settings;
import com.sap.bfx.session.ElementPos;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Locale;
import java.util.Set;

public interface Context<AC extends AccessClass> {

    /**
     * Get API instance. Currently these are:
     * <ul>
     *     <it>FormsApi</it>
     *     <it>WorkflowApi</it>
     * </ul>
     *
     * @param apiCls
     * @param <T>
     * @return
     */
    <T extends Api> T getApi(Class<T> apiCls);

    /**
     * Returns the current state of the display form
     *
     * @return
     */
    String getDisplayState();

    /**
     * Sets the current state of the display forms
     *
     * @param state
     */
    void setDisplayState(final String state);

    /**
     * @return
     */
    Locale getLocale();

    /**
     * @param locale
     */
    void setLocale(final Locale locale);

    /**
     * @return
     */
    Set<String> getAddDataKeys();

    /**
     * @param key
     * @return
     */
    Object getAddData(String key);

    /**
     * @param key
     * @param value
     */
    void setAddData(String key, Object value);

    /**
     * @return
     */
    AC getData();

    /**
     * @return
     */
    DataApi getDataApi();

    /**
     * @return
     */
    ScenarioDefinition getScenarioDefinition();

    /**
     * @return
     */
    abstract AbstractAuthenticationToken getToken();

    /**
     * @return
     */
    abstract ElementPos getSource();

    /**
     * @return
     */
    Settings getP13NValue(String key);

    /**
     * Returns the task instance ID if the context is related to a task
     *
     * @return
     */
    String getTaskInstanceId();
}
