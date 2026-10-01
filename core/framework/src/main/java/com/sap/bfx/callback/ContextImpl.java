package com.sap.bfx.callback;

import com.sap.bfx.definition.ScenarioDefinition;
import com.sap.bfx.p13n.Settings;
import com.sap.bfx.security.User;
import com.sap.bfx.session.ElementPos;
import com.sap.bfx.session.Session;

import java.util.*;

/**
 * Implementation of Context interface
 *
 * @param <AC>
 */
public class ContextImpl<AC extends AccessClass> implements Context<AC> {
    final Map<String, Object> params = new HashMap<>();
    ApiFactory apiFactory;
    AccessClassFactory acFactory;
    ScenarioDefinition scenarioDefinition;
    Session session;
    String displayState;
    Locale locale;
    AC data;
    DataApi dataApi;
    User user;
    ElementPos source;
    String taskInstanceId;
    Object authObject;

    @Override
    public <T extends Api> T getApi(Class<T> apiCls) {
        return apiFactory.getApi(apiCls, session.getForm());
    }

    @Override
    public String getDisplayState() {
        return displayState;
    }

    @Override
    public void setDisplayState(String state) {
        this.displayState = state;
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    @Override
    public Set<String> getAddDataKeys() {
        return session.getAdditionalData().keySet();
    }

    @Override
    public Object addData(String key) {
        return session.getAdditionalData().get(key);
    }

    @Override
    public void addData(String key, Object value) {
        session.getAdditionalData().put(key, value);
    }

    @Override
    public void deleteAddData(String key) {
        session.getAdditionalData().remove(key);
    }

    @Override
    public Set<String> getParamsKeys() {
        return params.keySet();
    }

    @Override
    public Object param(String key) {
        return params.get(key);
    }

    @Override
    public void param(String key, Object value) {
        params.put(key, value);
    }

    @Override
    public void deleteParam(String key) {
        params.remove(key);
    }

    @Override
    public AC getData() {
        if (data == null) {
            data = acFactory.createAccessClass(scenarioDefinition, session.getForm());
        }
        return data;
    }

    /**
     * Sets the access class data for the current context. This method allows you to provide a specific instance of
     * the access class, which can be used to manage and manipulate data related to the current scenario and form.
     *
     * @param value access class instance
     */
    public void setData(final AC value) {
        data = value;
    }

    @Override
    public DataApi getDataApi() {
        if (dataApi == null) {
            dataApi = acFactory.createDataApi(scenarioDefinition, session.getForm());
        }
        return dataApi;
    }

    /**
     * Sets the data API for the current context. This method allows you to provide a specific instance of
     * the data API, which can be used to manage and manipulate data related to the current scenario and form in
     * a generic way.
     *
     * @param value the DataApi object instane
     */
    public void setDataApi(final DataApi value) {
        dataApi = value;
    }

    @Override
    public ScenarioDefinition getScenarioDefinition() {
        return scenarioDefinition;
    }

    @Override
    public Object getAuthentication() {
        return authObject;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public ElementPos getSource() {
        return source;
    }

    @Override
    public Settings getP13NValue(String s) {
        Collection<Settings> settings = session.getSettings();
        return settings.stream().filter(setting -> setting.getKey().equals(s)).findFirst().orElse(null);
    }

    @Override
    public String getTaskInstanceId() {
        return taskInstanceId;
    }
}
