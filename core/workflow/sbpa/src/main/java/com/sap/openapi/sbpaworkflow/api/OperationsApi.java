

package com.sap.openapi.sbpaworkflow.api;

import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.cloud.sdk.services.openapi.core.AbstractOpenApiService;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;

import com.sap.openapi.sbpaworkflow.model.TCMError400;
import com.sap.openapi.sbpaworkflow.model.TCMError403;
import com.sap.openapi.sbpaworkflow.model.TCMError404;
import com.sap.openapi.sbpaworkflow.model.TCMError405;
import com.sap.openapi.sbpaworkflow.model.TCMError406;
import com.sap.openapi.sbpaworkflow.model.TCMError500;
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
 * Inbox in version v1.
 *
 * This API provides the functionality for building \"own inbox\" capabilities on top of SAP Build Process Automation. It is based on the task consumption model (TCM) as documented in SAP Note [2304317](https://launchpad.support.sap.com/#/notes/2304317/E).  Only users who have the ProcessAutomationParticipant role assigned are able to use the Inbox API.  SAP Build Process Automation currently supports only a subset of the Task Consumption Model capabilities. Only the functionality included in this document is available.
 */
public class OperationsApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the Inbox.
     *
     * @param httpDestination The destination that API should be used with
     */
    public OperationsApi( @Nonnull final Destination httpDestination )
    {
        super(httpDestination);
    }

    /**
     * Instantiates this API class to invoke operations on the Inbox based on a given {@link ApiClient}.
     *
     * @param apiClient
     *            ApiClient to invoke the API on
     */
    @Beta
    public OperationsApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

        /**
     * <p>Claims the given task to the current user</p>
     * <p>Claims the given task to the current user. Afterwards, the task is in status \&quot;RESERVED\&quot; and can be handled only by the processor.</p>
     * <p><b>202</b> - Task has been claimed.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param saPOrigin
     *      The SID discriminator. Set to &#39;NA&#39; for all entities. For more information, see the Task Consumption Model documentation in SAP Note [2304317](https://launchpad.support.sap.com/#/notes/2304317/E).
     * @param instanceID
     *      The ID of the task to be claimed.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse claimSAPOriginSAPOriginInstanceIDInstanceIDPost( @Nonnull final String saPOrigin,  @Nonnull final String instanceID) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'saPOrigin' is set
        if (saPOrigin == null) {
            throw new OpenApiRequestException("Missing the required parameter 'saPOrigin' when calling claimSAPOriginSAPOriginInstanceIDInstanceIDPost");
        }
        
        // verify the required parameter 'instanceID' is set
        if (instanceID == null) {
            throw new OpenApiRequestException("Missing the required parameter 'instanceID' when calling claimSAPOriginSAPOriginInstanceIDInstanceIDPost");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("SAP__Origin", saPOrigin);
        localVarPathParams.put("InstanceID", instanceID);
        final String localVarPath = UriComponentsBuilder.fromPath("/Claim?SAP__Origin='{SAP__Origin}'&InstanceID='{InstanceID}'").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/xml"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        apiClient.invokeAPI(localVarPath, HttpMethod.POST, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        return new OpenApiResponse(apiClient);
    }
    /**
     * <p>Forwards the given task to the specified user</p>
     * <p>Forwards the given task to the specified user. Afterwards, the task is in status \&quot;RESERVED\&quot; and only the new processor can work on it. In addition, this user becomes one of the task recipients, that means, the task remains accessible even after it was released. Note that the passed user ID is not validated. If the given user is not present, an administrator is required to reassign the task. Forwarding is a configuration option for user tasks. You can deactivate forwarding.</p>
     * <p><b>202</b> - Task has been forwarded.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param saPOrigin
     *      The SID discriminator. Set to &#39;NA&#39; for all entities. For more information, see the Task Consumption Model documentation in SAP Note [2304317](https://launchpad.support.sap.com/#/notes/2304317/E).
     * @param instanceID
     *      The ID of the task to be forwarded.
     * @param forwardTo
     *      The ID of the user to be forwarded to.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse forwardSAPOriginSAPOriginInstanceIDInstanceIDForwardToForwardToPost( @Nonnull final String saPOrigin,  @Nonnull final String instanceID,  @Nonnull final String forwardTo) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'saPOrigin' is set
        if (saPOrigin == null) {
            throw new OpenApiRequestException("Missing the required parameter 'saPOrigin' when calling forwardSAPOriginSAPOriginInstanceIDInstanceIDForwardToForwardToPost");
        }
        
        // verify the required parameter 'instanceID' is set
        if (instanceID == null) {
            throw new OpenApiRequestException("Missing the required parameter 'instanceID' when calling forwardSAPOriginSAPOriginInstanceIDInstanceIDForwardToForwardToPost");
        }
        
        // verify the required parameter 'forwardTo' is set
        if (forwardTo == null) {
            throw new OpenApiRequestException("Missing the required parameter 'forwardTo' when calling forwardSAPOriginSAPOriginInstanceIDInstanceIDForwardToForwardToPost");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("SAP__Origin", saPOrigin);
        localVarPathParams.put("InstanceID", instanceID);
        localVarPathParams.put("ForwardTo", forwardTo);
        final String localVarPath = UriComponentsBuilder.fromPath("/Forward?SAP__Origin='{SAP__Origin}'&InstanceID='{InstanceID}'&ForwardTo='{ForwardTo}'").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/xml"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        apiClient.invokeAPI(localVarPath, HttpMethod.POST, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        return new OpenApiResponse(apiClient);
    }
    /**
     * <p>Releases the given task so it can be claimed by other users</p>
     * <p>Releases a given task so it can be claimed by other users. Afterwards, the task is in status \&quot;READY\&quot; and can be claimed by any user who is assigned as a potential owner.</p>
     * <p><b>202</b> - Task has been released.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param saPOrigin
     *      The SID discriminator. Set to &#39;NA&#39; for all entities. For more information, see the Task Consumption Model documentation in SAP Note [2304317](https://launchpad.support.sap.com/#/notes/2304317/E).
     * @param instanceID
     *      The ID of the task to be released.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse releaseSAPOriginSAPOriginInstanceIDInstanceIDPost( @Nonnull final String saPOrigin,  @Nonnull final String instanceID) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'saPOrigin' is set
        if (saPOrigin == null) {
            throw new OpenApiRequestException("Missing the required parameter 'saPOrigin' when calling releaseSAPOriginSAPOriginInstanceIDInstanceIDPost");
        }
        
        // verify the required parameter 'instanceID' is set
        if (instanceID == null) {
            throw new OpenApiRequestException("Missing the required parameter 'instanceID' when calling releaseSAPOriginSAPOriginInstanceIDInstanceIDPost");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("SAP__Origin", saPOrigin);
        localVarPathParams.put("InstanceID", instanceID);
        final String localVarPath = UriComponentsBuilder.fromPath("/Release?SAP__Origin='{SAP__Origin}'&InstanceID='{InstanceID}'").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/xml"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        apiClient.invokeAPI(localVarPath, HttpMethod.POST, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        return new OpenApiResponse(apiClient);
    }
}
