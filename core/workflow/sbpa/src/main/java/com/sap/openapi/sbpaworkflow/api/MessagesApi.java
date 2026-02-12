

package com.sap.openapi.sbpaworkflow.api;

import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.cloud.sdk.services.openapi.core.AbstractOpenApiService;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;

import com.sap.openapi.sbpaworkflow.model.ConflictError;
import com.sap.openapi.sbpaworkflow.model.ConsumingWorkflowInstance;
import com.sap.openapi.sbpaworkflow.model.SendMessagePayload;
import com.sap.openapi.sbpaworkflow.model.TechnicalError;
import com.sap.openapi.sbpaworkflow.model.UnauthorizedError;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.google.common.annotations.Beta;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;

/**
 * Workflow in version v1.
 *
 * This API uses the workflow capability of SAP Build Process Automation. With the API, you can, for example, start new workflow instances and work with tasks.  Note: These APIs are designed for loosely coupled clients. This means:  - If SAP Build Process Automation adds fields to responses, the API version number does not increase. Your client must ignore new fields. - The order of fields in responses and of entries in arrays may change. This applies unless the API provides an explicit means to specify the desired order. 
 */
public class MessagesApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the Workflow.
     *
     * @param httpDestination The destination that API should be used with
     */
    public MessagesApi( @Nonnull final Destination httpDestination )
    {
        super(httpDestination);
    }

    /**
     * Instantiates this API class to invoke operations on the Workflow based on a given {@link ApiClient}.
     *
     * @param apiClient
     *            ApiClient to invoke the API on
     */
    @Beta
    public MessagesApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

        /**
     * <p>Send message</p>
     * <p>Sends a message to a set of workflow instances for consumption in intermediate message events. The message is identified by the name specified in the workflow model (request body parameter &#39;definitionId&#39;) and parameters identifying the workflow instances that should consume the message.  From the process builder of SAP Build Process Automation, you currently cannot model intermediate message events. This API exists for workflows that originate from other editors.  The message is consumed by the workflow instances that match the following criteria:  * The instance can be a specific match when using its workflow instance ID (request body parameter &#39;workflowInstanceId&#39;). Or the instance is a generic match when using the ID of the workflow model together with the business key (request body parameters &#39;workflowDefinitionId&#39; respectively &#39;businessKey&#39;). You can either use the specific or generic match but not both in the same call.  * The workflow instance is not in the SUSPENDED state.  * The workflow instance currently waits at the intermediate message event referring to the specified message.  The business key of a workflow instance matches if the business key specified in the request body is the same.  Roles permitted to execute this operation:  - Global roles: WorkflowMessageSender</p>
     * <p><b>200</b> - Returns a list of workflow instance IDs that consumed the message.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>409</b> - The operation could not be executed because of another activity in the background. Please try again later.
     * <p><b>422</b> - The message was not consumed by the intermediate message event. Please see the messages API to verify that your message meets the requirements for successful consumption.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param body
     *      Specify the request body according to the given schema. The length of the request body is limited to ensure optimal operation of the service.
     * @return List&lt;ConsumingWorkflowInstance&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<ConsumingWorkflowInstance> v1MessagesPost( @Nonnull final SendMessagePayload body) throws OpenApiRequestException {
        final Object localVarPostBody = body;
        
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new OpenApiRequestException("Missing the required parameter 'body' when calling v1MessagesPost");
        }
        
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/messages").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<List<ConsumingWorkflowInstance>> localVarReturnType = new ParameterizedTypeReference<List<ConsumingWorkflowInstance>>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.POST, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
