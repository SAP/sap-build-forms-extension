package com.sap.bfx.callback;

import com.sap.bfx.definition.ProcessState;
import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.session.Form;
import com.sap.bfx.session.FormsService;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

/**
 * Implementation of the FormsApi interface that provides methods to manage forms.
 */
@Slf4j
public class FormsApiImpl implements FormsApi {

    private FormsService formsService;
    private Form form;

    /**
     * Constructor to initialize the FormsApiImpl with the necessary services and form.
     *
     * @param formsService the service to manage forms
     * @param form         the form instance to be managed
     */
    public FormsApiImpl(final FormsService formsService, final Form form) {
        this.formsService = formsService;
        this.form = form;
    }

    /**
     * Saves the current form using the FormsService.
     * Throws a runtime exception if an error occurs during saving.
     */
    @Override
    public void save() {
        try {
            formsService.save(form);
        } catch (Throwable e) {
            throw ExceptionUtils.from("Error saving form: " + form.getId(), e);
        }
    }

    @Override
    public void delete() {
        try {
            formsService.delete(form.getId());
        } catch (Throwable e) {
            throw ExceptionUtils.from("Error deleting form: " + form.getId(), e);
        }
    }

    /**
     * Returns the id of the current form.
     *
     * @return
     */
    @Override
    public String getFormId() {
        return form.getId();
    }

    /**
     * @return
     */
    @Override
    public String getFormRefId() {
        return form.getRefId();
    }

    /**
     * @param formRefId
     */
    @Override
    public void setFormRefId(String formRefId) {
        form.setRefId(formRefId);
    }

    /**
     * @return
     */
    @Override
    public String getChangedBy() {
        return form.getChangedBy();
    }

    /**
     * @return
     */
    @Override
    public Instant getChangedAt() {
        return form.getChangedAt();
    }

    /**
     * @return
     */
    @Override
    public String getDescription() {
        return form.getDescription();
    }

    /**
     * @param description
     */
    @Override
    public void setDescription(String description) {
        form.setDescription(description);
    }

    /**
     * @return
     */
    @Override
    public Instant getFinishedAt() {
        return form.getFinishedAt();
    }

    /**
     * @param finishedAt
     */
    @Override
    public void setFinishedAt(Instant finishedAt) {
        form.setFinishedAt(finishedAt);
    }

    /**
     * @return
     */
    @Override
    public String getFunctionalId() {
        return form.getFunctionalId();
    }

    /**
     * @param functionalId
     */
    @Override
    public void setFunctionalId(String functionalId) {
        form.setFunctionalId(functionalId);
    }

    /**
     * @return
     */
    @Override
    public String getStartedBy() {
        return form.getStartedBy();
    }

    /**
     * @param startedBy
     */
    @Override
    public void setStartedBy(String startedBy) {
        form.setStartedBy(startedBy);
    }

    /**
     * @return
     */
    @Override
    public Instant getStartedAt() {
        return form.getStartedAt();
    }

    /**
     * @param startedAt
     */
    @Override
    public void setStartedAt(Instant startedAt) {
        form.setStartedAt(startedAt);
    }

    /**
     * @return
     */
    @Override
    public ProcessState getProcessState() {
        return form.getState();
    }

    /**
     * @param processState
     */
    @Override
    public void setProcessState(ProcessState processState) {
        form.setState(processState);
    }

    /**
     * @return
     */
    @Override
    public String getDetailState() {
        return form.getDetailState();
    }

    /**
     * @param detailState
     */
    @Override
    public void setDetailState(String detailState) {
        form.setDetailState(detailState);
    }

    /**
     * @return
     */
    @Override
    public String getTemplateName() {
        return form.getTemplateName();
    }

    /**
     * @param templateName
     */
    @Override
    public void setTemplateName(String templateName) {
        form.setTemplateName(templateName);
    }

    /**
     * @return
     */
    @Override
    public long getVersion() {
        return form.getVersion();
    }
}
