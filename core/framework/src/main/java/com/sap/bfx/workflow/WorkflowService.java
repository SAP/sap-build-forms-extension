package com.sap.bfx.workflow;

import com.sap.bfx.callback.AbstractAdapterHandlingService;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WorkflowService extends AbstractAdapterHandlingService<WorkflowAdapter> {

    /**
     * @param applicationContext
     */
    @Autowired
    public WorkflowService(ApplicationContext applicationContext) {
        super(applicationContext, WorkflowAdapter.class);
    }

    /**
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public TaskInputContext findFormByTask(final String destinationName, final String taskInstanceId) {
        return findFormByTask(null, destinationName, taskInstanceId);
    }

    /**
     * @param adapterName
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public TaskInputContext findFormByTask(final String adapterName, final String destinationName, final String taskInstanceId) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.findFormByTask(destinationName, taskInstanceId);
    }

    /**
     * @param technicalDestinationName
     * @param workflowDefinitionName
     * @param destinationName
     * @param workflowStartContext
     * @return
     */
    public String startWorkflowByWfDefName(final String technicalDestinationName, final String workflowDefinitionName, final String destinationName, final Object workflowStartContext) {
        return startWorkflowByWfDefName(null, technicalDestinationName, workflowDefinitionName, destinationName, workflowStartContext);
    }

    /**
     * @param adapterName
     * @param technicalDestinationName
     * @param workflowDefinitionName
     * @param destinationName
     * @param workflowStartContext
     * @return
     */
    public String startWorkflowByWfDefName(final String adapterName, final String technicalDestinationName, final String workflowDefinitionName, final String destinationName, final Object workflowStartContext) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.startWorkflowByWfDefName(technicalDestinationName, workflowDefinitionName, destinationName, workflowStartContext);
    }

    /**
     * @param workflowDefinitionId
     * @param environmentId
     * @param destinationName
     * @param workflowStartContext
     * @return
     */
    public String startWorkflowByWfDefId(final String workflowDefinitionId, final String environmentId, final String destinationName, final Object workflowStartContext) {
        return startWorkflowByWfDefId(null, workflowDefinitionId, environmentId, destinationName, workflowStartContext);
    }

    /**
     * @param adapterName
     * @param workflowDefinitionId
     * @param environmentId
     * @param destinationName
     * @param workflowStartContext
     * @return
     */
    public String startWorkflowByWfDefId(final String adapterName, final String workflowDefinitionId, final String environmentId, final String destinationName, final Object workflowStartContext) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.startWorkflowByWfDefId(workflowDefinitionId, environmentId, destinationName, workflowStartContext);
    }

    /**
     * @param destinationName
     * @param taskInstanceId
     * @param taskOutputContext
     * @return
     */
    public Boolean completeTask(final String destinationName, final String taskInstanceId, final TaskOutputContext taskOutputContext) {
        return completeTask(null, destinationName, taskInstanceId, taskOutputContext);
    }

    /**
     * @param adapterName
     * @param destinationName
     * @param taskInstanceId
     * @param taskOutputContext
     * @return
     */
    public Boolean completeTask(final String adapterName, final String destinationName, final String taskInstanceId, final TaskOutputContext taskOutputContext) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.completeTask(destinationName, taskInstanceId, taskOutputContext);
    }

    /**
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public Boolean claimTask(final String destinationName, final String taskInstanceId) {
        return claimTask(null, destinationName, taskInstanceId);
    }

    /**
     * @param adapterName
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public Boolean claimTask(final String adapterName, final String destinationName, final String taskInstanceId) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.claimTask(destinationName, taskInstanceId);
    }

    /**
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public Boolean releaseTask(final String destinationName, final String taskInstanceId) {
        return releaseTask(null, destinationName, taskInstanceId);
    }

    /**
     * @param adapterName
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public Boolean releaseTask(final String adapterName, final String destinationName, final String taskInstanceId) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.releaseTask(destinationName, taskInstanceId);
    }

    /**
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public String getTaskStatus(final String destinationName, final String taskInstanceId) {
        return getTaskStatus(null, destinationName, taskInstanceId);
    }

    /**
     * @param adapterName
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public String getTaskStatus(final String adapterName, final String destinationName, final String taskInstanceId) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.getTaskStatus(destinationName, taskInstanceId);
    }

    /**
     * @param destinationName
     * @param taskInstanceId
     * @param currentUser
     * @return
     */
    public Boolean isTaskExecutable(final String destinationName, final String taskInstanceId, final String currentUser, final boolean initSession) {
        return isTaskExecutable(null, destinationName, taskInstanceId, currentUser, initSession);
    }

    /**
     * @param adapterName
     * @param destinationName
     * @param taskInstanceId
     * @param currentUser
     * @return
     */
    public Boolean isTaskExecutable(final String adapterName, final String destinationName, final String taskInstanceId, final String currentUser, final boolean initSession) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.isTaskExecutable(destinationName, taskInstanceId, currentUser, initSession);
    }

    /**
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public TaskInstance getTaskInstance(final String destinationName, final String taskInstanceId) {
        return getTaskInstance(null, destinationName, taskInstanceId);
    }

    /**
     * @param adapterName
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public TaskInstance getTaskInstance(final String adapterName, final String destinationName, final String taskInstanceId) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.getTaskInstance(destinationName, taskInstanceId);
    }

    /**
     * @param technicalDestinationName
     * @param workflowInstanceId
     * @return
     */
    public Boolean cancelWorkflowByWfInstId(final String technicalDestinationName, final String workflowInstanceId) {
        return cancelWorkflowByWfInstId(null, technicalDestinationName, workflowInstanceId);
    }

    /**
     * @param adapterName
     * @param technicalDestinationName
     * @param workflowInstanceId
     * @return
     */
    public Boolean cancelWorkflowByWfInstId(final String adapterName, final String technicalDestinationName, final String workflowInstanceId) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.cancelWorkflowByWfInstId(technicalDestinationName, workflowInstanceId);
    }

    /**
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public String getWfInstIdByTaskInstId(final String destinationName, final String taskInstanceId) {
        return getWfInstIdByTaskInstId(null, destinationName, taskInstanceId);
    }

    /**
     * @param adapterName
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public String getWfInstIdByTaskInstId(final String adapterName, final String destinationName, final String taskInstanceId) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.getWfInstIdByTaskInstId(destinationName, taskInstanceId);
    }

    /**
     * @param technicalDestinationName
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public Pair<String, String> getWfInstIdEnvIdByTaskInstId(final String technicalDestinationName, final String destinationName, final String taskInstanceId) {
        return getWfInstIdEnvIdByTaskInstId(null, technicalDestinationName, destinationName, taskInstanceId);
    }

    /**
     * @param adapterName
     * @param technicalDestinationName
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    public Pair<String, String> getWfInstIdEnvIdByTaskInstId(final String adapterName, final String technicalDestinationName, final String destinationName, final String taskInstanceId) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.getWfInstIdEnvIdByTaskInstId(technicalDestinationName, destinationName, taskInstanceId);
    }

    /**
     * @param technicalDestinationName
     * @param workflowInstanceId
     * @return
     */
    public Pair<String, String> getRootWfInstIdEnvIdByWfInstId(final String technicalDestinationName, final String workflowInstanceId) {
        return getRootWfInstIdEnvIdByWfInstId(null, technicalDestinationName, workflowInstanceId);
    }

    /**
     * @param adapterName
     * @param technicalDestinationName
     * @param workflowInstanceId
     * @return
     */
    public Pair<String, String> getRootWfInstIdEnvIdByWfInstId(final String adapterName, final String technicalDestinationName, final String workflowInstanceId) {
        final var adapter = this.getAdapter(adapterName);
        return adapter.getRootWfInstIdEnvIdByWfInstId(technicalDestinationName, workflowInstanceId);
    }

}
