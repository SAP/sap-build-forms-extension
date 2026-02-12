package com.sap.bfx.workflow.sbpa;

import com.sap.cloud.sdk.cloudplatform.connectivity.HttpDestination;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.bfx.btp.ConnectivityUtils;
import com.sap.bfx.workflow.WorkflowAdapter;
import com.sap.bfx.workflow.TaskInputContext;
import com.sap.bfx.workflow.TaskInstance;
import com.sap.bfx.workflow.TaskOutputContext;
import com.sap.openapi.sbpaworkflow.api.OperationsApi;
import com.sap.openapi.sbpaworkflow.api.UserTaskInstancesApi;
import com.sap.openapi.sbpaworkflow.model.UpdateTaskInstancePayload;
import com.sap.openapi.sbpaworkflow.model.WorkflowInstance;
import com.sap.openapi.sbpaworkflow.model.WorkflowInstanceStartPayload;
import com.sap.openapi.sbpaworkflow.model.WorkflowInstanceUpdatePayload;
import org.apache.commons.collections4.map.LRUMap;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Component
public class SBPAWorkflowAdapter implements WorkflowAdapter {

    private LRUMap<String, Pair<String, String>> wfDefNameToWfDefIdEnvId = new LRUMap<>(1000);
    private LRUMap<String, String> wfDefIdToEnvId = new LRUMap<>(1000);

    /**
     * @param taskInstanceId
     * @return
     */
    @Override
    public TaskInputContext findFormByTask(String destinationName, String taskInstanceId) {
        UserTaskInstancesApi utiApi = new UserTaskInstancesApi(ConnectivityUtils.getHttpDestinationWithForwardUserToken(destinationName));
        Object contextObj = utiApi.v1TaskInstancesTaskInstanceIdContextGet(taskInstanceId);
        if (contextObj instanceof TaskInputContext taskInputContext) {
            return taskInputContext;
        } else if (contextObj instanceof Map<?, ?> contextMap) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> castedContextMap = (Map<String, Object>) contextMap;
                return TaskInputContext.mapToTaskInputContext(castedContextMap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public String startWorkflowByWfDefName(String technicalDestinationName, String workflowDefinitionName, String destinationName, Object workflowStartContext) {
        if (!wfDefNameToWfDefIdEnvId.containsKey(workflowDefinitionName)) {
            findAndFillWfDefId(technicalDestinationName, workflowDefinitionName, null);
        }
        Pair<String, String> wfDefPair = wfDefNameToWfDefIdEnvId.get(workflowDefinitionName);
        return startWorkflowByWfDefId(wfDefPair.getLeft(), wfDefPair.getRight(), destinationName, workflowStartContext);
    }

    private void findAndFillWfDefId(String technicalDestinationName, String workflowDefinitionName, String workflowDefinitionId) {
        HttpDestination technicalDestination = ConnectivityUtils.getHttpDestination(technicalDestinationName);
        // Hint: maintain and set api-key programmatically inside WorkflowDefinitionsApiExtended until
        // https://help.sap.com/docs/build-process-automation/sap-build-process-automation/update-service-instance?locale=en-US&q=apiKey
        // is not implemented and applicable
        String apiKey = (!technicalDestination.get(WorkflowConstants.API_KEY).isEmpty()) ? technicalDestination.get(WorkflowConstants.API_KEY).get().toString() : null;
        WorkflowDefinitionsApiExtended wfDefApiExt = new WorkflowDefinitionsApiExtended(technicalDestination);
        List<WorkflowDefinitionExtended> wfDefsExt = wfDefApiExt.v1WorkflowDefinitionsExtendedGet(null, apiKey, WorkflowConstants.NAME_ASC, 0, 1000, WorkflowConstants.ALLPAGES);
        for (WorkflowDefinitionExtended wfDefExt : wfDefsExt) {
            if ((null != workflowDefinitionName && wfDefExt.getName().matches(workflowDefinitionName)) || (null != workflowDefinitionId && wfDefExt.getId().matches(workflowDefinitionId))) {
                wfDefNameToWfDefIdEnvId.put(wfDefExt.getName(), new ImmutablePair<>(wfDefExt.getId(), wfDefExt.getEnvironmentId()));
                wfDefIdToEnvId.put(wfDefExt.getId(), wfDefExt.getEnvironmentId());
                return;
            }
        }
        throw new NoSuchElementException(MessageFormat.format("WorkflowDefinition with name \'\'{0}\'\' was not found", workflowDefinitionName));
    }

    @Override
    public String startWorkflowByWfDefId(String workflowDefinitionId, String environmentId, String destinationName, Object workflowStartContext) {
        WorkflowInstancesApiExtended wfApiExt = new WorkflowInstancesApiExtended(ConnectivityUtils.getHttpDestinationWithForwardUserToken(destinationName));
        WorkflowInstanceStartPayload wfStartPayload = new WorkflowInstanceStartPayload();
        wfStartPayload.setDefinitionId(workflowDefinitionId);
        wfStartPayload.setContext(workflowStartContext);
        WorkflowInstance wfInstance = wfApiExt.v1WorkflowInstancesPost(environmentId, wfStartPayload);
        return wfInstance.getId();
    }

    @Override
    public Boolean completeTask(String destinationName, String taskInstanceId, TaskOutputContext taskOutputContext) {
        UserTaskInstancesApi utiApi = new UserTaskInstancesApi(ConnectivityUtils.getHttpDestinationWithForwardUserToken(destinationName));
        UpdateTaskInstancePayload utiPayload = new UpdateTaskInstancePayload();
        utiPayload.setDecision(WorkflowConstants.SUBMIT);
        utiPayload.setStatus(UpdateTaskInstancePayload.StatusEnum.COMPLETED);
        utiPayload.setContext(taskOutputContext);
        OpenApiResponse openApiResponse = utiApi.v1TaskInstancesTaskInstanceIdPatch(taskInstanceId, utiPayload);
        if (openApiResponse.getStatusCode() == 204) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public Boolean claimTask(String destinationName, String taskInstanceId) {
        OperationsApi opApi = new OperationsApi(ConnectivityUtils.getHttpDestinationWithForwardUserToken(destinationName));
        OpenApiResponse openApiResponse = opApi.claimSAPOriginSAPOriginInstanceIDInstanceIDPost(WorkflowConstants.NA, taskInstanceId);
        if (openApiResponse.getStatusCode() == 202) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public Boolean releaseTask(String destinationName, String taskInstanceId) {
        OperationsApi opApi = new OperationsApi(ConnectivityUtils.getHttpDestinationWithForwardUserToken(destinationName));
        OpenApiResponse openApiResponse = opApi.releaseSAPOriginSAPOriginInstanceIDInstanceIDPost(WorkflowConstants.NA, taskInstanceId);
        if (openApiResponse.getStatusCode() == 202) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public String getTaskStatus(String destinationName, String taskInstanceId) {
        UserTaskInstancesApi utiApi = new UserTaskInstancesApi(ConnectivityUtils.getHttpDestinationWithForwardUserToken(destinationName));
        com.sap.openapi.sbpaworkflow.model.TaskInstance sbpaTaskInstance = utiApi.v1TaskInstancesTaskInstanceIdGet(taskInstanceId);
        return sbpaTaskInstance.getStatus().getValue();
    }

    @Override
    public Boolean isTaskExecutable(String destinationName, String taskInstanceId, String currentUser, boolean initSession) {
        UserTaskInstancesApi utiApi = new UserTaskInstancesApi(ConnectivityUtils.getHttpDestinationWithForwardUserToken(destinationName));
        com.sap.openapi.sbpaworkflow.model.TaskInstance sbpaTaskInstance = utiApi.v1TaskInstancesTaskInstanceIdGet(taskInstanceId);
        switch (sbpaTaskInstance.getStatus()) {
            case CANCELED:
            case COMPLETED:
                return false;
            case READY:
                return (initSession) ? true : false;
            case RESERVED:
                return sbpaTaskInstance.getProcessor().matches(currentUser);
            default:
                throw new RuntimeException("Task status is unknown for task " + taskInstanceId);
        }
    }

    @Override
    public TaskInstance getTaskInstance(String destinationName, String taskInstanceId) {
        UserTaskInstancesApi utiApi = new UserTaskInstancesApi(ConnectivityUtils.getHttpDestinationWithForwardUserToken(destinationName));
        com.sap.openapi.sbpaworkflow.model.TaskInstance sbpaTaskInstance = utiApi.v1TaskInstancesTaskInstanceIdGet(taskInstanceId, null, "attributes");
        return mapToTaskInstance(sbpaTaskInstance);
    }

    private TaskInstance mapToTaskInstance(com.sap.openapi.sbpaworkflow.model.TaskInstance sbpaTaskInstance) {
        TaskInstance taskInstance = new TaskInstance();
        taskInstance.setActivityId(sbpaTaskInstance.getId());
        taskInstance.setClaimedAt(sbpaTaskInstance.getClaimedAt());
        taskInstance.setCompletedAt(sbpaTaskInstance.getCompletedAt());
        taskInstance.setCreatedAt(sbpaTaskInstance.getCreatedAt());
        taskInstance.setCreatedBy(sbpaTaskInstance.getCreatedBy());
        taskInstance.setDescription(sbpaTaskInstance.getDescription());
        taskInstance.setDueDate(sbpaTaskInstance.getDueDate());
        taskInstance.setId(sbpaTaskInstance.getId());
        taskInstance.setPriority(sbpaTaskInstance.getPriority().getValue());
        taskInstance.setProcessor(sbpaTaskInstance.getProcessor());
        taskInstance.setRecipientUsers(sbpaTaskInstance.getRecipientUsers());
        taskInstance.setRecipientGroups(sbpaTaskInstance.getRecipientGroups());
        taskInstance.setStatus(sbpaTaskInstance.getStatus().getValue());
        taskInstance.setSubject(sbpaTaskInstance.getSubject());
        taskInstance.setWorkflowDefinitionId(sbpaTaskInstance.getWorkflowInstanceId());
        taskInstance.setWorkflowInstanceId(sbpaTaskInstance.getWorkflowInstanceId());
        taskInstance.setPriority(sbpaTaskInstance.getPriority().getValue());
        taskInstance.setDefinitionId(sbpaTaskInstance.getDefinitionId());
        taskInstance.setLastChangedAt(sbpaTaskInstance.getLastChangedAt());
        taskInstance.setApplicationScope(sbpaTaskInstance.getApplicationScope());
        taskInstance.setUserInterfaceUri(sbpaTaskInstance.getUserInterfaceUri());
        if (!sbpaTaskInstance.getAttributes().isEmpty()) {
            taskInstance.setAttributes(sbpaTaskInstance.getAttributes().stream().map(attr -> {
                return new TaskInstance.Attributes(attr.getId(), attr.getLabel(), attr.getValue());
            }).collect(Collectors.toList()));
        } else {
            taskInstance.setAttributes(new ArrayList<>());
        }
        return taskInstance;
    }

    @Override
    public Boolean cancelWorkflowByWfInstId(String technicalDestinationName, String workflowInstanceId) {
        HttpDestination technicalDestination = ConnectivityUtils.getHttpDestination(technicalDestinationName);
        // Hint: maintain and set api-key programmatically inside WorkflowDefinitionsApiExtended until
        // https://help.sap.com/docs/build-process-automation/sap-build-process-automation/update-service-instance?locale=en-US&q=apiKey
        // is not implemented and applicable
        String apiKey = (!technicalDestination.get(WorkflowConstants.API_KEY).isEmpty()) ? technicalDestination.get(WorkflowConstants.API_KEY).get().toString() : null;
        WorkflowInstancesApiExtended wfApiExt = new WorkflowInstancesApiExtended(technicalDestination);
        WorkflowInstanceUpdatePayload wfUpdatePayload = new WorkflowInstanceUpdatePayload();
        wfUpdatePayload.setStatus(WorkflowInstanceUpdatePayload.StatusEnum.CANCELED);
        wfUpdatePayload.setCascade(true);
        OpenApiResponse openApiResponse = wfApiExt.v1WorkflowInstancesWorkflowInstanceIdPatch(workflowInstanceId, apiKey, wfUpdatePayload);
        if (openApiResponse.getStatusCode() == 202) {
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    @Override
    public String getWfInstIdByTaskInstId(String destinationName, String taskInstanceId) {
        UserTaskInstancesApi utiApi = new UserTaskInstancesApi(ConnectivityUtils.getHttpDestinationWithForwardUserToken(destinationName));
        com.sap.openapi.sbpaworkflow.model.TaskInstance taskInstance = utiApi.v1TaskInstancesTaskInstanceIdGet(taskInstanceId);
        return taskInstance.getWorkflowInstanceId();
    }

    @Override
    public Pair<String, String> getWfInstIdEnvIdByTaskInstId(String technicalDestinationName, String destinationName, String taskInstanceId) {
        UserTaskInstancesApi utiApi = new UserTaskInstancesApi(ConnectivityUtils.getHttpDestinationWithForwardUserToken(destinationName));
        com.sap.openapi.sbpaworkflow.model.TaskInstance taskInstance = utiApi.v1TaskInstancesTaskInstanceIdGet(taskInstanceId);
        if (!wfDefIdToEnvId.containsKey(taskInstance.getWorkflowDefinitionId())) {
            findAndFillWfDefId(technicalDestinationName, null, taskInstance.getWorkflowDefinitionId());
        }
        return new ImmutablePair<>(taskInstance.getWorkflowInstanceId(), wfDefIdToEnvId.get(taskInstance.getWorkflowDefinitionId()));
    }

    @Override
    public Pair<String, String> getRootWfInstIdEnvIdByWfInstId(String technicalDestinationName, String workflowInstanceId) {
        HttpDestination technicalDestination = ConnectivityUtils.getHttpDestination(technicalDestinationName);
        // Hint: maintain and set api-key programmatically inside WorkflowDefinitionsApiExtended until
        // https://help.sap.com/docs/build-process-automation/sap-build-process-automation/update-service-instance?locale=en-US&q=apiKey
        // is not implemented and applicable
        String apiKey = (!technicalDestination.get(WorkflowConstants.API_KEY).isEmpty()) ? technicalDestination.get(WorkflowConstants.API_KEY).get().toString() : null;
        WorkflowInstancesApiExtended wfApiExt = new WorkflowInstancesApiExtended(technicalDestination);
        WorkflowInstance wfInstance = wfApiExt.v1WorkflowInstancesWorkflowInstanceIdGet(workflowInstanceId, apiKey);
        if (!wfInstance.getId().matches(wfInstance.getRootInstanceId())) {
            wfInstance = wfApiExt.v1WorkflowInstancesWorkflowInstanceIdGet(wfInstance.getRootInstanceId(), apiKey);
        }
        if (!wfDefIdToEnvId.containsKey(wfInstance.getDefinitionId())) {
            findAndFillWfDefId(technicalDestinationName, null, wfInstance.getDefinitionId());
        }
        return new ImmutablePair<>(wfInstance.getId(), wfDefIdToEnvId.get(wfInstance.getDefinitionId()));
    }
}
