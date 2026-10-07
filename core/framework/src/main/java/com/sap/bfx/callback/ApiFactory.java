package com.sap.bfx.callback;

import com.sap.bfx.exception.ExceptionUtils;
import com.sap.bfx.session.Form;
import com.sap.bfx.session.FormsService;
import com.sap.bfx.valuehelp.ValueHelpService;
import com.sap.bfx.workflow.WorkflowApi;
import com.sap.bfx.workflow.WorkflowService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

/**
 * Factory class for creating API instances based on the requested API class and form context.
 * Supports creation of FormsApi, WorkflowApi, ValuehelpApi, and FeedApi instances.
 */
@Service
public class ApiFactory {

    private final FormsService formService;
    private final WorkflowService workflowService;
    private final ValueHelpService valueHelpService;
    private final FeedService feedService;

    /**
     * Constructor for ApiFactory.
     *
     * @param formService      Reference of FormsService
     * @param workflowService  Reference of WorkflowService
     * @param valueHelpService Reference of ValueHelpService
     * @param feedService      Reference of FeedService
     */
    @Autowired
    public ApiFactory(FormsService formService, WorkflowService workflowService,
                      ValueHelpService valueHelpService, @Nullable FeedService feedService) {
        this.formService = formService;
        this.workflowService = workflowService;
        this.valueHelpService = valueHelpService;
        this.feedService = feedService;
    }

    /**
     * Returns an instance of the requested API class, initialized with the provided form context.
     *
     * @param apiCls the class of the API to create (e.g., FormsApi.class, WorkflowApi.class, ValuehelpApi.class, FeedApi.class)
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
            return (T) new ValuehelpApiImpl(valueHelpService);
        }
        if (FeedApi.class.equals(apiCls)) {
            if (feedService == null) throw ExceptionUtils.from("FeedApi requested but no FeedService bean is available");
            return (T) new FeedApiImpl(feedService, form.getId());
        }

        throw ExceptionUtils.from("Unsupported API class requested in ApiFactory.getApi(): " + apiCls.getName());
    }
}
