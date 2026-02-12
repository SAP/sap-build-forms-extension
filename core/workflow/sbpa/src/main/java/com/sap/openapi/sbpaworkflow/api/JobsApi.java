

package com.sap.openapi.sbpaworkflow.api;

import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.cloud.sdk.services.openapi.core.AbstractOpenApiService;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;

import com.sap.openapi.sbpaworkflow.model.Job;
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
public class JobsApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the Workflow.
     *
     * @param httpDestination The destination that API should be used with
     */
    public JobsApi( @Nonnull final Destination httpDestination )
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
    public JobsApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

        /**
     * <p>Retrieve Job status</p>
     * <p>Tracks the status of API requests, which the server executed asynchronously. Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin </p>
     * <p><b>200</b> - Returns the status of the job
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>404</b> - Job not found. You may have provided an incorrect job ID or the job may have already completed.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * @param jobId
     *      The ID of the job for which you check the status. Typically, the ID has been retrieved from another API request that was processed asynchronously. The ID is at most 36 characters long.
     * @return Job
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public Job v1JobsJobIdGet( @Nonnull final String jobId) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'jobId' is set
        if (jobId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'jobId' when calling v1JobsJobIdGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("jobId", jobId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/jobs/{jobId}").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<Job> localVarReturnType = new ParameterizedTypeReference<Job>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
