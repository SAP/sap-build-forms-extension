package com.sap.bfx.callback;

import com.sap.bfx.definition.ScenarioDefinition;
import com.sap.bfx.p13n.Settings;
import com.sap.bfx.session.ElementPos;
import com.sap.bfx.session.Session;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.*;

/**
 * Implementation of Context interface
 *
 * @param <AC>
 */
public class ContextImpl<AC extends AccessClass> implements Context<AC> {
    ApiFactory apiFactory;
    AccessClassFactory acFactory;

    ScenarioDefinition scenarioDefinition;
    Session session;
    String displayState;
    Locale locale;
    Map<String, Object> params = new HashMap<>();
    AC data;
    DataApi dataApi;
    AbstractAuthenticationToken token;
    ElementPos source;
    String taskInstanceId;

    /**
     * @param apiCls
     * @param <T>
     * @return
     */
    @Override
    public <T extends Api> T getApi(Class<T> apiCls) {
        return apiFactory.getApi(apiCls, session.getForm());
    }

    /**
     * @param key
     * @return
     */
    @Override
    public Object getAddData(String key) {
        return params.get(key);
    }

    /**
     * @return
     */
    @Override
    public String getDisplayState() {
        return displayState;
    }

    /**
     * @param state
     */
    @Override
    public void setDisplayState(String state) {
        this.displayState = state;
    }

    /**
     * @return
     */
    @Override
    public Locale getLocale() {
        return locale;
    }

    /**
     * @param locale
     */
    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    /**
     * @return
     */
    @Override
    public Set<String> getAddDataKeys() {
        return params.keySet();
    }

    /**
     * @param key
     * @param value
     */
    @Override
    public void setAddData(String key, Object value) {
        params.put(key, value);
    }

    /**
     * @return
     */
    @Override
    public AC getData() {
        if (data == null) {
            data = acFactory.createAccessClass(scenarioDefinition, session.getForm());
        }
        return data;
    }

    /**
     * @param value
     */
    public void setData(final AC value) {
        data = value;
    }

    /**
     * @return
     */
    @Override
    public DataApi getDataApi() {
        if (dataApi == null) {
            dataApi = acFactory.createDataApi(scenarioDefinition, session.getForm());
        }
        return dataApi;
    }

    /**
     * @param value
     */
    public void setDataApi(final DataApi value) {
        dataApi = value;
    }

    /**
     * @return
     */
    @Override
    public ScenarioDefinition getScenarioDefinition() {
        return scenarioDefinition;
    }

    /**
     * @return
     */
    @Override
    public AbstractAuthenticationToken getToken() {
        return token;
    }

    /**
     * @return
     */
    @Override
    public ElementPos getSource() {
        return source;
    }

    @Override
    public Settings getP13NValue(String s) {
        Collection<Settings> settings = session.getSettings();
        return settings.stream()
                .filter(setting -> setting.getKey().equals(s))
                .findFirst()
                .orElse(null);
    }

    /**
     * @return
     */
    @Override
    public String getTaskInstanceId() {
        return taskInstanceId;
    }
}
