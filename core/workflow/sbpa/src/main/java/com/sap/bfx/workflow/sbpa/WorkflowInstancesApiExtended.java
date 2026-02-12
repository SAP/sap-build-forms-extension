package com.sap.bfx.workflow.sbpa;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;
import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.openapi.sbpaworkflow.api.WorkflowInstancesApi;
import com.sap.openapi.sbpaworkflow.model.WorkflowInstance;
import com.sap.openapi.sbpaworkflow.model.WorkflowInstanceStartPayload;
import com.sap.openapi.sbpaworkflow.model.WorkflowInstanceUpdatePayload;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.web.util.UriUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkflowInstancesApiExtended extends WorkflowInstancesApi {

    public WorkflowInstancesApiExtended(@NotNull Destination httpDestination) {
        super(httpDestination);
    }

    public WorkflowInstancesApiExtended(@NotNull ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * <p>Start a new instance</p>
     * <p>Starts a new workflow instance of the provided workflow definition. Specify the ID of the workflow definition in the body. The workflow instance automatically starts based on the latest deployed version of the definition.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationParticipant </p>
     * <p><b>201</b> - Returns the newly created workflow instance. Note that subject and businessKey are &#39;null&#39; and are evaluated after the workflow instance was started. If the process has a start event output mapping, the two attributes are evaluated based on the result of the mapping. If the process does not have a start event output mapping, the attributes are evaluated based on the payload that was sent in the request. To receive these values, query the instance by ID after the mapping is done.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Workflow definition not found. Either the payload does not contain a definitionId property or the specified ID is incorrect.
     * <p><b>422</b> - The workflow context in the request body contains invalid keys or values.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources.
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     *
     * @param environmentId (required)
     *                      Specify the shared environment where the workflow instance is to start
     * @param body          Specify the request body according to the given schema. Note that the length of the request body is limited to ensure optimal operation of the service.
     * @return WorkflowInstance
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowInstance v1WorkflowInstancesPost(@Nonnull final String environmentId, @Nonnull final WorkflowInstanceStartPayload body) throws OpenApiRequestException {
        return v1WorkflowInstancesPost(environmentId, body, null);
    }

    /**
     * <p>Start a new instance</p>
     * <p>Starts a new workflow instance of the provided workflow definition. Specify the ID of the workflow definition in the body. The workflow instance automatically starts based on the latest deployed version of the definition.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationParticipant </p>
     * <p><b>201</b> - Returns the newly created workflow instance. Note that subject and businessKey are &#39;null&#39; and are evaluated after the workflow instance was started. If the process has a start event output mapping, the two attributes are evaluated based on the result of the mapping. If the process does not have a start event output mapping, the attributes are evaluated based on the payload that was sent in the request. To receive these values, query the instance by ID after the mapping is done.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Workflow definition not found. Either the payload does not contain a definitionId property or the specified ID is incorrect.
     * <p><b>422</b> - The workflow context in the request body contains invalid keys or values.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources.
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     *
     * @param environmentId  (required)
     *                       Specify the shared environment where the workflow instance is to start
     * @param body           (required)
     *                       Specify the request body according to the given schema. Note that the length of the request body is limited to ensure optimal operation of the service.
     * @param acceptLanguage (optional)
     *                       Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @return WorkflowInstance
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowInstance v1WorkflowInstancesPost(@Nonnull final String environmentId, @Nonnull final WorkflowInstanceStartPayload body, @Nullable final String acceptLanguage) throws OpenApiRequestException {
        final Object localVarPostBody = body;

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new OpenApiRequestException("Missing the required parameter 'body' when calling v1WorkflowInstancesPost");
        }

        String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances").build().toUriString();
        if (!environmentId.matches(WorkflowConstants.DEFAULT)) {
            // create path and map variables
            final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
            localVarPathParams.put("environmentId", UriUtils.encodeQueryParam(environmentId, "utf8"));
            localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances?environmentId={environmentId}").buildAndExpand(localVarPathParams).toUriString();
        }

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        if (acceptLanguage != null)
            localVarHeaderParams.add("Accept-Language", apiClient.parameterToString(acceptLanguage));

        final String[] localVarAccepts = {
                "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[]{"Oauth2_ClientCredentials", "Oauth2_AuthorizationCode"};

        final ParameterizedTypeReference<WorkflowInstance> localVarReturnType = new ParameterizedTypeReference<WorkflowInstance>() {
        };
        return apiClient.invokeAPI(localVarPath, HttpMethod.POST, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve workflow instance by ID</p>
     * <p>Retrieves the workflow instance with the specified workflow instance ID.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin</p>
     * <p><b>200</b> - The requested workflow instance.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources.
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     *
     * @param workflowInstanceId The ID of the workflow instance, which should be retrieved. The workflow instance ID is 36 characters long.
     * @param apiKey             (optional)
     *                           Provide an api-key for shared environment of SBPA. If api-key is available, relevant header will be set.
     * @return WorkflowInstance
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowInstance v1WorkflowInstancesWorkflowInstanceIdGet(@Nonnull final String workflowInstanceId, @Nullable final String apiKey) throws OpenApiRequestException {
        return v1WorkflowInstancesWorkflowInstanceIdGet(workflowInstanceId, null, apiKey, null);
    }

    /**
     * <p>Retrieve workflow instance by ID</p>
     * <p>Retrieves the workflow instance with the specified workflow instance ID.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin</p>
     * <p><b>200</b> - The requested workflow instance.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources.
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     *
     * @param workflowInstanceId (required)
     *                           The ID of the workflow instance, which should be retrieved. The workflow instance ID is 36 characters long.
     * @param acceptLanguage     (optional)
     *                           Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param apiKey             (optional)
     *                           Provide an api-key for shared environment of SBPA. If api-key is available, relevant header will be set.
     * @param $expand            (optional)
     *                           You can request custom workflow attributes to become part of the workflow instance output by specifying the value &#39;attributes&#39; for the &#39;$expand&#39; parameter. Otherwise, if the &#39;$expand&#39; parameter is not specified, the &#39;attributes&#39; field is not included into the output of the workflow instance. Note that labels as well as the order of the custom workflow attributes in which they are returned, are taken from the latest versions of the workflow definitions where these attributes are present.
     * @return WorkflowInstance
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowInstance v1WorkflowInstancesWorkflowInstanceIdGet(@Nonnull final String workflowInstanceId, @Nullable final String acceptLanguage, @Nullable final String apiKey, @Nullable final String $expand) throws OpenApiRequestException {
        final Object localVarPostBody = null;

        // verify the required parameter 'workflowInstanceId' is set
        if (workflowInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'workflowInstanceId' when calling v1WorkflowInstancesWorkflowInstanceIdGet");
        }

        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("workflowInstanceId", workflowInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances/{workflowInstanceId}").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$expand", $expand));


        if (acceptLanguage != null)
            localVarHeaderParams.add("Accept-Language", apiClient.parameterToString(acceptLanguage));

        if (apiKey != null) localVarHeaderParams.add(WorkflowConstants.API_KEY, apiKey);

        final String[] localVarAccepts = {
                "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[]{"Oauth2_ClientCredentials", "Oauth2_AuthorizationCode"};

        final ParameterizedTypeReference<WorkflowInstance> localVarReturnType = new ParameterizedTypeReference<WorkflowInstance>() {
        };
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Update instance</p>
     * <p>Modifies the properties of a given workflow instance, for example, sets its status to CANCELED or RUNNING.  Status changes may not take effect immediately, due to asynchronous processing of the request. When you change the status to CANCELED, note the following:  * Workflow instances in CANCELED status are considered final, that is, no further changes are allowed. This is valid as well for other APIs and the processing according to the workflow definition.  * Workflow instances in CANCELED status stop processing as soon as the system allows.  When you are changing the status to SUSPENDED, note the following:  * Status SUSPENDED manually and temporarily suspends processing.  * You can choose to suspend the specified instance or the whole cascade by setting boolean parameter \&quot;cascade\&quot;. By default, the parameter is false. When set to true, the operation is cascaded to its referenced subflow instances.  * Workflow instances in SUSPENDED status stop processing as soon as the system allows.  * Workflow instances remain in SUSPENDED status until a status change to RUNNING or CANCELED is requested.  * While the workflow instance status reported by the respective API might change with immediate effect, follow-up actions might only be successful, after asynchronous processing within the workflow instance actually has stopped. To check whether asynchronous processing is ongoing, analyze the execution logs or check the workflow definition structure.  When you are changing the status to RUNNING, note the following:  * For workflow instances in ERRONEOUS status, this retries the failed activities. If these activities continue failing, the workflow instance automatically moves again into ERRONEOUS status.  * If the workflow instance had previously been suspended while in ERRONEOUS status, failed activities, such as service tasks, are retried.  * You can choose to retry or resume the specified instance or the whole cascade by setting boolean parameter \&quot;cascade\&quot;. By default, the parameter is false. When set to true, the operation is cascaded to its referenced subflow instances.  When you propagate the status change to subflow instances with the &#39;cascade&#39; parameter, note the following:  * The effects outlined above are appropriately applied to the subflow instances. For example, instances in a final status like CANCELED are not changed by the API.  The status values in relation to this API have the following corresponding terms in user interfaces of SAP Build Process Automation: * RUNNING - Running * ERRONEOUS - Error * SUSPENDED - On Hold * CANCELED - Canceled * COMPLETED - Completed  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin</p>
     * <p><b>202</b> - The request is successful and the properties will be changed asynchronously.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>409</b> - The operation could not be executed because of another activity in the background. Please try again later.
     * <p><b>422</b> - The status provided in the request body was invalid.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources.
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     *
     * @param workflowInstanceId The ID of the workflow instance, which should be modified. The workflow instance ID is 36 characters long.
     * @param apiKey             (optional)
     *                           Provide an api-key for shared environment of SBPA. If api-key is available, relevant header will be set.
     * @param body               Specify the request body according to the given schema. The length of the request body is limited to ensure optimal operation of the service.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse v1WorkflowInstancesWorkflowInstanceIdPatch(@Nonnull final String workflowInstanceId, @Nullable final String apiKey, @Nonnull final WorkflowInstanceUpdatePayload body) throws OpenApiRequestException {
        final Object localVarPostBody = body;

        // verify the required parameter 'workflowInstanceId' is set
        if (workflowInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'workflowInstanceId' when calling v1WorkflowInstancesWorkflowInstanceIdPatch");
        }

        // verify the required parameter 'body' is set
        if (body == null) {
            throw new OpenApiRequestException("Missing the required parameter 'body' when calling v1WorkflowInstancesWorkflowInstanceIdPatch");
        }

        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("workflowInstanceId", workflowInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances/{workflowInstanceId}").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        if (apiKey != null) localVarHeaderParams.add(WorkflowConstants.API_KEY, apiKey);

        final String[] localVarAccepts = {
                "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {
                "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[]{"Oauth2_ClientCredentials", "Oauth2_AuthorizationCode"};

        final ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {
        };
        apiClient.invokeAPI(localVarPath, HttpMethod.PATCH, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        return new OpenApiResponse(apiClient);
    }
}
