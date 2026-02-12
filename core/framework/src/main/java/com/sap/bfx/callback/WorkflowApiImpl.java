package com.sap.bfx.callback;

import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.workflow.TaskInputContext;
import com.sap.bfx.workflow.TaskInstance;
import com.sap.bfx.workflow.TaskOutputContext;
import com.sap.bfx.workflow.WorkflowApi;
import com.sap.bfx.workflow.WorkflowService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Implementation of the WorkflowApi interface that provides methods to manage workflows and tasks.
 */
@Slf4j
public class WorkflowApiImpl implements WorkflowApi {

    private WorkflowService workflowService;

    /**
     * Constructor to initialize the WorkflowApiImpl with the necessary services.
     *
     * @param workflowService the service to manage workflows and tasks
     */
    public WorkflowApiImpl(final WorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Override
    public String startWorkflowByWfDefName(String technicalDestinationName, String workflowDefinitionName,
                                           String destinationName, Object workflowStartContext) {
        try {
            return workflowService.startWorkflowByWfDefName(technicalDestinationName, workflowDefinitionName, destinationName, workflowStartContext);
        } catch (Throwable e) {
            throw ExceptionUtils.from("Error starting workflow " + workflowDefinitionName, e);
        }
    }

    @Override
    public String startWorkflowByWfDefId(String workflowDefinitionId, String environmentId, String destinationName, Object workflowStartContext) {
        try {
            return workflowService.startWorkflowByWfDefId(workflowDefinitionId, environmentId, destinationName, workflowStartContext);
        } catch (Throwable e) {
            throw ExceptionUtils.from("Error starting workflow with workflowDefinitionId "
                    + workflowDefinitionId + " and environmentId " + environmentId, e);
        }
    }

    @Override
    public TaskInputContext findFormByTask(String destinationName, String taskInstanceId) {
        try {
            return workflowService.findFormByTask(destinationName, taskInstanceId);
        } catch (Throwable e) {
            throw ExceptionUtils.from("Error finding task " + taskInstanceId, e);
        }
    }

    @Override
    public Boolean completeTask(String destinationName, String taskInstanceId, TaskOutputContext taskOutputContext) {
        try {
            return workflowService.completeTask(destinationName, taskInstanceId, taskOutputContext);
        } catch (Throwable e) {
            throw ExceptionUtils.from("Error completing task " + taskInstanceId, e);
        }
    }

    @Override
    public Boolean claimTask(String destinationName, String taskInstanceId) {
        try {
            return workflowService.claimTask(destinationName, taskInstanceId);
        } catch (Throwable e) {
            throw ExceptionUtils.from("Error claiming task " + taskInstanceId, e);
        }
    }

    @Override
    public Boolean releaseTask(String destinationName, String taskInstanceId) {
        try {
            return workflowService.releaseTask(destinationName, taskInstanceId);
        } catch (Throwable e) {
            throw ExceptionUtils.from("Error releasing task " + taskInstanceId, e);
        }
    }

    @Override
    public String getTaskStatus(String destinationName, String taskInstanceId) {
        try {
            return workflowService.getTaskStatus(destinationName, taskInstanceId);
        } catch (Throwable e) {
            throw ExceptionUtils.from("Error get status of task " + taskInstanceId, e);
        }
    }

    @Override
    public Boolean isTaskExecutable(String destinationName, String taskInstanceId, String currentUser, boolean initSession) {
        try {
            return workflowService.isTaskExecutable(destinationName, taskInstanceId, currentUser, initSession);
        } catch (Throwable e) {
            throw ExceptionUtils.from("Error to determine executable if task " + taskInstanceId, e);
        }
    }

    @Override
    public TaskInstance getTaskInstance(String destinationName, String taskInstanceId) {
        try {
            return workflowService.getTaskInstance(destinationName, taskInstanceId);
        } catch (Throwable e) {
            throw ExceptionUtils.from("Error releasing task " + taskInstanceId, e);
        }
    }

    @Override
    public Boolean cancelWorkflowByWfInstId(String technicalDestinationName, String workflowInstanceId) {
        try {
            return workflowService.cancelWorkflowByWfInstId(technicalDestinationName, workflowInstanceId);
        } catch (Throwable e) {
            log.error("Error cancel workflow instance {}", workflowInstanceId, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getWfInstIdByTaskInstId(String destinationName, String taskInstanceId) {
        try {
            return workflowService.getWfInstIdByTaskInstId(destinationName, taskInstanceId);
        } catch (Throwable e) {
            log.error("Error get workflow instance id of task instance id {}", taskInstanceId, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Pair<String, String> getWfInstIdEnvIdByTaskInstId(String technicalDestinationName, String destinationName, String taskInstanceId) {
        try {
            return workflowService.getWfInstIdEnvIdByTaskInstId(technicalDestinationName, destinationName, taskInstanceId);
        } catch (Throwable e) {
            log.error("Error get workflow instance id and environment id of task instance id {}", taskInstanceId, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Pair<String, String> getRootWfInstIdEnvIdByWfInstId(String technicalDestinationName, String workflowInstanceId) {
        try {
            return workflowService.getRootWfInstIdEnvIdByWfInstId(technicalDestinationName, workflowInstanceId);
        } catch (Throwable e) {
            log.error("Error get root workflow instance id of workflow instance id {}", workflowInstanceId, e);
            throw new RuntimeException(e);
        }
    }
}
