package com.sap.bfx.workflow;

import org.apache.commons.lang3.tuple.Pair;

import com.sap.bfx.callback.Adapter;


/**
 * Interface for workflow adapters that can provide form information for tasks.
 */
public interface WorkflowAdapter extends Adapter {

    /**
     * Finds the form associated with a specific task instance.
     *
     * @param destinationName the name of the destination
     * @param taskInstanceId  the ID of the task instance
     * @return an Optional containing the TaskInputContext if found, otherwise an empty Optional
     */
    TaskInputContext findFormByTask(final String destinationName, final String taskInstanceId);

    /**
     * Starts a workflow based on the workflow definition name.
     *
     * @param technicalDestinationName the technical name of the destination
     * @param workflowDefinitionName   the name of the workflow definition
     * @param destinationName          the name of the destination
     * @param workflowStartContext     the context for starting the workflow
     * @return the ID of the started workflow instance
     */
    String startWorkflowByWfDefName(final String technicalDestinationName, final String workflowDefinitionName,
                                    final String destinationName, final Object workflowStartContext);

    /**
     * Starts a workflow based on the workflow definition ID.
     *
     * @param workflowDefinitionId the ID of the workflow definition
     * @param environmentId        the environment ID
     * @param destinationName      the name of the destination
     * @param workflowStartContext the context for starting the workflow
     * @return the ID of the started workflow instance
     */
    String startWorkflowByWfDefId(final String workflowDefinitionId, final String environmentId,
                                  final String destinationName, final Object workflowStartContext);

    /**
     * Completes a task with the provided output context.
     *
     * @param destinationName   the name of the destination
     * @param taskInstanceId    the ID of the task instance
     * @param taskOutputContext the output context for completing the task
     * @return true if the task was completed successfully, false otherwise
     */
    Boolean completeTask(final String destinationName, final String taskInstanceId,
                         final TaskOutputContext taskOutputContext);

    /**
     * Claims a task for the current user.
     *
     * @param destinationName the name of the destination
     * @param taskInstanceId  the ID of the task instance
     * @return true if the task was claimed successfully, false otherwise
     */
    Boolean claimTask(final String destinationName, final String taskInstanceId);

    /**
     * Releases a claimed task.
     *
     * @param destinationName the name of the destination
     * @param taskInstanceId  the ID of the task instance
     * @return true if the task was released successfully, false otherwise
     */
    Boolean releaseTask(final String destinationName, final String taskInstanceId);

    /**
     * Retrieves the status of a specific task instance.
     *
     * @param destinationName the name of the destination
     * @param taskInstanceId  the ID of the task instance
     * @return the status of the task as a String
     */
    String getTaskStatus(final String destinationName, final String taskInstanceId);

    /**
     * Checks if a task is executable by the current user.
     *
     * @param destinationName the name of the destination
     * @param taskInstanceId  the ID of the task instance
     * @param currentUser     the current user
     * @param initSession     whether to initialize a session
     * @return true if the task is executable, false otherwise
     */
    Boolean isTaskExecutable(final String destinationName, final String taskInstanceId, final String currentUser,
                             final boolean initSession);

    /**
     * Retrieves the TaskInstance object for a specific task instance ID.
     *
     * @param destinationName the name of the destination
     * @param taskInstanceId  the ID of the task instance
     * @return the TaskInstance object
     */
    TaskInstance getTaskInstance(final String destinationName, final String taskInstanceId);

    Boolean cancelWorkflowByWfInstId(final String technicalDestinationName, final String workflowInstanceId);

    String getWfInstIdByTaskInstId(final String destinationName, final String taskInstanceId);

    Pair<String, String> getWfInstIdEnvIdByTaskInstId(final String technicalDestinationName, final String destinationName, final String taskInstanceId);

    Pair<String, String> getRootWfInstIdEnvIdByWfInstId(final String technicalDestinationName, final String workflowInstanceId);

}
