package com.sap.bfx.callback;

import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.session.Form;
import com.sap.bfx.session.FormsService;
import com.sap.bfx.workflow.WorkflowApi;
import com.sap.bfx.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Factory class for creating API instances based on the requested API class and form context.
 * Supports creation of FormsApi and WorkflowApi instances.
 */
@Service
public class ApiFactory {

    @Autowired
    FormsService formService;
    @Autowired
    WorkflowService workflowService;

    /**
     * Returns an instance of the requested API class, initialized with the provided form context.
     *
     * @param apiCls the class of the API to create (e.g., FormsApi.class, WorkflowApi.class or ValuehelpApi.class)
     * @param form   the form context to be used for API initialization
     * @param <T>    the type of the API
     * @return an instance of the requested API class
     */
    @SuppressWarnings("unchecked")
    <T extends Api> T getApi(final Class<T> apiCls, final Form form) {
        if (FormsApi.class.equals(apiCls)) {
            return (T) new FormsApiImpl(formService, form);
        }
        if (WorkflowApi.class.equals(apiCls)) {
            return (T) new WorkflowApiImpl(workflowService);
        }
        if (ValuehelpApi.class.equals(apiCls)) {
            return (T) new ValuehelpApiImpl();
        }

        throw ExceptionUtils.from("Unsupported API class requested in ApiFactory.getApi(): " + apiCls.getName());
    }
}
