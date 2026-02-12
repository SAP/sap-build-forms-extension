

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
import com.sap.openapi.sbpaworkflow.model.TaskCustomAttributeDefinitions;
import com.sap.openapi.sbpaworkflow.model.TaskDefinitions;
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
public class TaskDefinitionCollectionApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the Inbox.
     *
     * @param httpDestination The destination that API should be used with
     */
    public TaskDefinitionCollectionApi( @Nonnull final Destination httpDestination )
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
    public TaskDefinitionCollectionApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

        /**
     * <p>Retrieves the number of task definitions for the current user</p>
     * <p>Retrieves the number of task definitions for the current user.</p>
     * <p><b>200</b> - Returns the number of task definitions for the current user.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @return Integer
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public Integer taskDefinitionCollectionCountGet() throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/TaskDefinitionCollection/$count").build().toUriString();

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

        final ParameterizedTypeReference<Integer> localVarReturnType = new ParameterizedTypeReference<Integer>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieves a list of the task definitions for the current user</p>
     *<p>Retrieves a list of the task definitions for the current user.</p>
     * <p><b>200</b> - Returns a list of task definitions for the current user.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @return TaskDefinitions
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskDefinitions taskDefinitionCollectionGet( @Nullable final String acceptLanguage) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/TaskDefinitionCollection").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        if (acceptLanguage != null)
            localVarHeaderParams.add("Accept-Language", apiClient.parameterToString(acceptLanguage));

        final String[] localVarAccepts = { 
            "application/json", "application/xml"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<TaskDefinitions> localVarReturnType = new ParameterizedTypeReference<TaskDefinitions>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieves a list of the task definitions for the current user</p>
     * <p>Retrieves a list of the task definitions for the current user.</p>
     * <p><b>200</b> - Returns a list of task definitions for the current user.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @return TaskDefinitions
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskDefinitions taskDefinitionCollectionGet() throws OpenApiRequestException {
        return taskDefinitionCollectionGet(null);
    }

    /**
     * <p>Retrieves a list of custom attributes for a task definition</p>
     *<p>Retrieves a list of custom attributes for a task definition.</p>
     * <p><b>200</b> - Returns a list of custom attributes for a task definition.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param saPOrigin  (required)
        The SID discriminator. Set to &#39;NA&#39; for all entities. For more information, see the Task Consumption Model documentation in SAP Note [2304317](https://launchpad.support.sap.com/#/notes/2304317/E).
     * @param taskDefinitionID  (required)
        The ID of the task definition.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $format  (optional, default to xml)
        Specify the format of the result. 
     * @return TaskCustomAttributeDefinitions
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskCustomAttributeDefinitions taskDefinitionCollectionSAPOriginSAPOriginTaskDefinitionIDTaskDefinitionIDCustomAttributeDefinitionDataGet( @Nonnull final String saPOrigin,  @Nonnull final String taskDefinitionID,  @Nullable final String acceptLanguage,  @Nullable final String $format) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'saPOrigin' is set
        if (saPOrigin == null) {
            throw new OpenApiRequestException("Missing the required parameter 'saPOrigin' when calling taskDefinitionCollectionSAPOriginSAPOriginTaskDefinitionIDTaskDefinitionIDCustomAttributeDefinitionDataGet");
        }
        
        // verify the required parameter 'taskDefinitionID' is set
        if (taskDefinitionID == null) {
            throw new OpenApiRequestException("Missing the required parameter 'taskDefinitionID' when calling taskDefinitionCollectionSAPOriginSAPOriginTaskDefinitionIDTaskDefinitionIDCustomAttributeDefinitionDataGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("SAP__Origin", saPOrigin);
        localVarPathParams.put("TaskDefinitionID", taskDefinitionID);
        final String localVarPath = UriComponentsBuilder.fromPath("/TaskDefinitionCollection(SAP__Origin='{SAP__Origin}',TaskDefinitionID='{TaskDefinitionID}')/CustomAttributeDefinitionData").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$format", $format));
        

        if (acceptLanguage != null)
            localVarHeaderParams.add("Accept-Language", apiClient.parameterToString(acceptLanguage));

        final String[] localVarAccepts = { 
            "application/json", "application/xml"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<TaskCustomAttributeDefinitions> localVarReturnType = new ParameterizedTypeReference<TaskCustomAttributeDefinitions>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieves a list of custom attributes for a task definition</p>
     * <p>Retrieves a list of custom attributes for a task definition.</p>
     * <p><b>200</b> - Returns a list of custom attributes for a task definition.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param saPOrigin
     *      The SID discriminator. Set to &#39;NA&#39; for all entities. For more information, see the Task Consumption Model documentation in SAP Note [2304317](https://launchpad.support.sap.com/#/notes/2304317/E).
     * @param taskDefinitionID
     *      The ID of the task definition.
     * @return TaskCustomAttributeDefinitions
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskCustomAttributeDefinitions taskDefinitionCollectionSAPOriginSAPOriginTaskDefinitionIDTaskDefinitionIDCustomAttributeDefinitionDataGet( @Nonnull final String saPOrigin,  @Nonnull final String taskDefinitionID) throws OpenApiRequestException {
        return taskDefinitionCollectionSAPOriginSAPOriginTaskDefinitionIDTaskDefinitionIDCustomAttributeDefinitionDataGet(saPOrigin, taskDefinitionID, null, null);
    }
}
