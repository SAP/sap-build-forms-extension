package com.sap.bfx.workflow;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.bfx.exception.ExceptionUtils;
import lombok.Data;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Task Input Context
 */
@Data
public class TaskInputContext {
    @JsonProperty("formsProcessID")
    String formsProcessID;
    @JsonProperty("configID")
    String configID;
    @JsonProperty("formsProcessState")
    String formsProcessState;
    @JsonProperty("formsScenarioBaseUrl")
    String formsScenarioBaseUrl;
    @JsonProperty("mailTemplate")
    String mailTemplate;
    @JsonProperty("customTaskTitle")
    String customTaskTitle;
    @JsonProperty("parameters")
    List<ParameterItem> parameters;

    /**
     * Map to TaskInputContext
     *
     * @param map input map
     * @return TaskInputContext instance
     */
    public static TaskInputContext mapToTaskInputContext(Map<String, Object> map) {
        try {
            TaskInputContext instance = TaskInputContext.class.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> entry : map.entrySet()) {

                Field field = TaskInputContext.class.getDeclaredField(entry.getKey());
                field.setAccessible(true);
                field.set(instance, entry.getValue());
            }
            return instance;
        } catch (Exception e) {
            throw ExceptionUtils.from(e);
        }
    }
}
