package com.sap.bfx.workflow;

import org.apache.commons.lang3.tuple.Pair;

import com.sap.bfx.callback.Api;

/**
 * Workflow API interface for starting workflows and managing tasks.
 */
public interface WorkflowApi extends Api {

    /**
     * Starts a workflow based on the workflow definition name.
     *
     * @param technicalDestinationName
     * @param workflowDefinitionName
     * @param destinationName
     * @param workflowStartContext
     * @return
     */
    String startWorkflowByWfDefName(String technicalDestinationName, String workflowDefinitionName, String destinationName, Object workflowStartContext);

    /**
     * Starts a workflow based on the workflow definition ID.
     *
     * @param workflowDefinitionId
     * @param environmentId
     * @param destinationName
     * @param workflowStartContext
     * @return
     */
    String startWorkflowByWfDefId(String workflowDefinitionId, String environmentId, String destinationName, Object workflowStartContext);

    /**
     * Finds the form associated with a specific task.
     *
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    TaskInputContext findFormByTask(String destinationName, String taskInstanceId);

    /**
     * Completes a task with the provided output context.
     *
     * @param destinationName
     * @param taskInstanceId
     * @param taskOutputContext
     * @return
     */
    Boolean completeTask(String destinationName, String taskInstanceId, TaskOutputContext taskOutputContext);

    /**
     * Claims a task for the current user.
     *
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    Boolean claimTask(final String destinationName, final String taskInstanceId);

    /**
     * Releases a previously claimed task.
     *
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    Boolean releaseTask(final String destinationName, final String taskInstanceId);

    /**
     * Retrieves the status of a specific task.
     *
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    String getTaskStatus(final String destinationName, final String taskInstanceId);

    /**
     * Checks if a task is executable by the current user.
     *
     * @param destinationName
     * @param taskInstanceId
     * @param currentUser
     * @param initSession
     * @return
     */
    Boolean isTaskExecutable(final String destinationName, final String taskInstanceId, final String currentUser, boolean initSession);

    /**
     * Retrieves the TaskInstance object for a specific task.
     *
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    TaskInstance getTaskInstance(final String destinationName, final String taskInstanceId);

    /**
     * @param technicalDestinationName
     * @param workflowInstanceId
     * @return
     */
    Boolean cancelWorkflowByWfInstId(final String technicalDestinationName, final String workflowInstanceId);

    /**
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    String getWfInstIdByTaskInstId(final String destinationName, final String taskInstanceId);

    /**
     * @param technicalDestinationName
     * @param destinationName
     * @param taskInstanceId
     * @return
     */
    Pair<String, String> getWfInstIdEnvIdByTaskInstId(final String technicalDestinationName, final String destinationName, final String taskInstanceId);

    /**
     * @param technicalDestinationName
     * @param workflowInstanceId
     * @return
     */
    Pair<String, String> getRootWfInstIdEnvIdByWfInstId(final String technicalDestinationName, final String workflowInstanceId);
}
