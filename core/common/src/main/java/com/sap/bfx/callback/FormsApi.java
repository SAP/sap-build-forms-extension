package com.sap.bfx.callback;

import com.sap.bfx.definition.ProcessState;

import java.time.Instant;

/**
 * Interface for Forms API that provides methods to load, save, and delete forms.
 * This interface is used to interact with form data in a structured way.
 */
public interface FormsApi extends Api {

    /**
     * Saves the current state of the form.
     * This method should be called to persist any changes made to the form.
     */
    void save();

    /**
     * Deletes the form.
     * This method should be called to remove the form from the system.
     */
    void delete();

    /**
     * Returns the ID of the form.
     *
     * @return the form ID
     */
    String getFormId();

    /**
     * Returns the reference ID of the form.
     *
     * @return the form reference ID
     */
    String getFormRefId();

    /**
     * Sets the reference ID of the form.
     *
     * @param formRefId the form reference ID to set
     */
    void setFormRefId(String formRefId);

    /**
     * Returns the user who made the last change
     *
     * @return
     */
    String getChangedBy();

    /**
     * Returns the timestamp of the last change
     *
     * @return
     */
    Instant getChangedAt();

    /**
     * Returns the description of the last change
     *
     * @return
     */
    String getDescription();

    /**
     * Sets the description
     *
     * @param description
     */
    void setDescription(String description);

    /**
     * Returns the time at which the process is finished
     *
     * @return
     */
    Instant getFinishedAt();

    /**
     * Sets the time at which the process is finished
     *
     * @param finishedAt
     */
    void setFinishedAt(Instant finishedAt);

    /**
     * Returns the functional ID of the process instance
     *
     * @return
     */
    String getFunctionalId();

    /**
     * Sets the functional ID of the process instance
     *
     * @param functionalId
     */
    void setFunctionalId(String functionalId);

    /**
     * Returns the user who started the process instance
     *
     * @return
     */
    String getStartedBy();

    /**
     * Sets the user who started the process instance
     *
     * @param startedBy
     */
    void setStartedBy(String startedBy);

    /**
     * Returns the time at which the process instance was started
     *
     * @return
     */
    Instant getStartedAt();

    /**
     * Sets the time at which the process instance was started
     *
     * @param startedAt
     */
    void setStartedAt(Instant startedAt);

    /**
     * Returns the current state of the process instance
     *
     * @return
     */
    ProcessState getProcessState();

    /**
     * Sets the current state of the process instance
     *
     * @param processState
     */
    void setProcessState(ProcessState processState);

    /**
     * Returns the detail state of the process instance
     *
     * @return
     */
    String getDetailState();

    /**
     * Sets the detail state of the process instance
     *
     * @param detailState
     */
    void setDetailState(String detailState);

    /**
     * Returns the name of the template used to create the process instance
     *
     * @return
     */
    String getTemplateName();

    /**
     * Sets the name of the template used to create the process instance
     *
     * @param templateName
     */
    void setTemplateName(String templateName);

    /**
     * Returns the version of the process instance
     *
     * @return
     */
    long getVersion();
}
