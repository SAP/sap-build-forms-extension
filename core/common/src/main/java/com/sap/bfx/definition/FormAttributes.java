package com.sap.bfx.definition;

import java.time.Instant;

public interface FormAttributes {

    /**
     * Get form ID
     *
     * @return form ID
     */
    String getId();

    /**
     * Set form ID
     *
     * @param value form ID
     */
    void setId(final String value);

    /**
     * Get version
     *
     * @return version
     */
    long getVersion();

    /**
     * Set version
     *
     * @param value
     */
    void setVersion(final long value);

    /**
     * Get scenario name
     *
     * @return scenario name
     */
    String getScenarioName();

    /**
     * Set scenario name
     *
     * @param value scenario name
     */
    void setScenarioName(final String value);

    /**
     * Get scenario version
     *
     * @return scenario version
     */
    int getScenarioVersion();

    /**
     * Set scenario version
     *
     * @param value scenario version
     */
    void setScenarioVersion(final int value);

    /**
     * Get template name
     *
     * @return template name
     */
    String getRefId();

    /**
     * Set template name
     *
     * @param value template name
     */
    void setRefId(final String value);

    /**
     * Get workflow adapter
     *
     * @return workflow adapter
     */
    String getWorkflowAdapter();

    /**
     * Set workflow adapter
     *
     * @param value workflow adapter
     */
    void setWorkflowAdapter(final String value);

    /**
     *
     * @return
     */
    String getChangedBy();

    /**
     *
     * @return
     */
    Instant getChangedAt();

    /**
     *
     * @return
     */
    String getTemplateName();

    /**
     *
     * @param value
     */
    void setTemplateName(final String value);

    /**
     *
     * @return
     */
    String getDescription();

    /**
     *
     * @param value
     */
    void setDescription(final String value);

    /**
     *
     * @return
     */
    Instant getStartedAt();

    /**
     *
     * @param value
     */
    void setStartedAt(final Instant value);

    /**
     *
     * @return
     */
    Instant getFinishedAt();

    /**
     *
     * @param value
     */
    void setFinishedAt(final Instant value);

    /**
     *
     * @return
     */
    String getStartedBy();

    /**
     *
     * @param value
     */
    void setStartedBy(final String value);

    /**
     *
     * @return
     */
    String getFunctionalId();

    /**
     *
     * @param value
     */
    void setFunctionalId(final String value);

    /**
     *
     * @return
     */
    ProcessState getState();

    /**
     *
     * @param value
     */
    void setState(final ProcessState value);

    /**
     *
     * @return
     */
    String getDetailState();

    /**
     *
     * @param value
     */
    void setDetailState(final String value);
}
