

package com.sap.openapi.sbpaworkflow.api;

import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.cloud.sdk.services.openapi.core.AbstractOpenApiService;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;

import com.sap.openapi.sbpaworkflow.model.SampleContext;
import com.sap.openapi.sbpaworkflow.model.TechnicalError;
import com.sap.openapi.sbpaworkflow.model.UnauthorizedError;
import com.sap.openapi.sbpaworkflow.model.WorkflowDefinition;
import com.sap.openapi.sbpaworkflow.model.WorkflowDefinitionVersion;
import com.sap.openapi.sbpaworkflow.model.WorkflowModel;

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
public class WorkflowDefinitionsApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the Workflow.
     *
     * @param httpDestination The destination that API should be used with
     */
    public WorkflowDefinitionsApi( @Nonnull final Destination httpDestination )
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
    public WorkflowDefinitionsApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

    
    /**
     * <p>Delete workflow definition</p>
     *<p>Undeploys all versions of an existing workflow definition and deletes the corresponding workflow instances. Once the undeployment has started, you can no longer start a new workflow instance based on this workflow definition.  This only applies to classic workflows developed in SAP Business Application Studio. For other workflows and processes, refer to [Delete a Project](https://help.sap.com/docs/build-process-automation/sap-build-process-automation/delete-project).  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin </p>
     * <p><b>202</b> - The undeployment of the workflow definition has been accepted, but will be executed asynchronously. The response will include the header &#39;Location&#39; that points to a dedicated resource. You can use this resource to track the status of the undeployment.
     * <p><b>303</b> - Another undeployment of the workflow definition is already running. The response will include the header &#39;Location&#39; that points to  a job resource which you can use to track the status of the undeployment. Depending on your web client you may be automatically redirected to this resource and receive the status of the undeployment as the result of your request.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Workflow definition not found. Either the payload does not contain a definitionId property or the specified ID is incorrect.
     * <p><b>412</b> - At least one version of the workflow definition has at least one RUNNING instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param definitionId  (required)
        The ID of the workflow definition to be undeployed. The ID is at most 255 characters long.
     * @param cascade  (optional, default to false)
        Whether the undeployment of the workflow definition deletes active workflow instances. If the parameter is false or not specified and there are active workflow instances for the specified workflow definition, the undeployment is rejected. An instance is considered active if it is in status RUNNING, ERRONEOUS, or SUSPENDED. 
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse v1WorkflowDefinitionsDefinitionIdDelete( @Nonnull final String definitionId,  @Nullable final Boolean cascade) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'definitionId' is set
        if (definitionId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'definitionId' when calling v1WorkflowDefinitionsDefinitionIdDelete");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("definitionId", definitionId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-definitions/{definitionId}").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "cascade", cascade));
        

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        apiClient.invokeAPI(localVarPath, HttpMethod.DELETE, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        return new OpenApiResponse(apiClient);
    }

    /**
     * <p>Delete workflow definition</p>
     * <p>Undeploys all versions of an existing workflow definition and deletes the corresponding workflow instances. Once the undeployment has started, you can no longer start a new workflow instance based on this workflow definition.  This only applies to classic workflows developed in SAP Business Application Studio. For other workflows and processes, refer to [Delete a Project](https://help.sap.com/docs/build-process-automation/sap-build-process-automation/delete-project).  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin </p>
     * <p><b>202</b> - The undeployment of the workflow definition has been accepted, but will be executed asynchronously. The response will include the header &#39;Location&#39; that points to a dedicated resource. You can use this resource to track the status of the undeployment.
     * <p><b>303</b> - Another undeployment of the workflow definition is already running. The response will include the header &#39;Location&#39; that points to  a job resource which you can use to track the status of the undeployment. Depending on your web client you may be automatically redirected to this resource and receive the status of the undeployment as the result of your request.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Workflow definition not found. Either the payload does not contain a definitionId property or the specified ID is incorrect.
     * <p><b>412</b> - At least one version of the workflow definition has at least one RUNNING instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param definitionId
     *      The ID of the workflow definition to be undeployed. The ID is at most 255 characters long.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse v1WorkflowDefinitionsDefinitionIdDelete( @Nonnull final String definitionId) throws OpenApiRequestException {
        return v1WorkflowDefinitionsDefinitionIdDelete(definitionId, null);
    }

    /**
     * <p>Retrieve workflow definition by ID</p>
     *<p>Retrieves the latest version of the specified workflow definition.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin, ProcessAutomationDeveloper </p>
     * <p><b>200</b> - Returns a single workflow definition version.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Workflow definition not found. Either the payload does not contain a definitionId property or the specified ID is incorrect.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param definitionId  (required)
        The ID of the workflow definition for which the latest version should be retrieved. The ID is at most 255 characters long.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @return WorkflowDefinition
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowDefinition v1WorkflowDefinitionsDefinitionIdGet( @Nonnull final String definitionId,  @Nullable final String acceptLanguage) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'definitionId' is set
        if (definitionId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'definitionId' when calling v1WorkflowDefinitionsDefinitionIdGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("definitionId", definitionId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-definitions/{definitionId}").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        if (acceptLanguage != null)
            localVarHeaderParams.add("Accept-Language", apiClient.parameterToString(acceptLanguage));

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<WorkflowDefinition> localVarReturnType = new ParameterizedTypeReference<WorkflowDefinition>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve workflow definition by ID</p>
     * <p>Retrieves the latest version of the specified workflow definition.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin, ProcessAutomationDeveloper </p>
     * <p><b>200</b> - Returns a single workflow definition version.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Workflow definition not found. Either the payload does not contain a definitionId property or the specified ID is incorrect.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param definitionId
     *      The ID of the workflow definition for which the latest version should be retrieved. The ID is at most 255 characters long.
     * @return WorkflowDefinition
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowDefinition v1WorkflowDefinitionsDefinitionIdGet( @Nonnull final String definitionId) throws OpenApiRequestException {
        return v1WorkflowDefinitionsDefinitionIdGet(definitionId, null);
    }
    /**
     * <p>Retrieve workflow definition model by ID</p>
     * <p>Retrieves the model of the latest version of the specified workflow definition.   Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin, ProcessAutomationDeveloper </p>
     * <p><b>200</b> - The deployed workflow model.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Either the specified workflow definition or the JSON model for that definition was not found.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param definitionId
     *      The workflow definition ID for which the latest model should be retrieved. The workflow definition ID is at most 64 characters long.
     * @return WorkflowModel
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowModel v1WorkflowDefinitionsDefinitionIdModelGet( @Nonnull final String definitionId) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'definitionId' is set
        if (definitionId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'definitionId' when calling v1WorkflowDefinitionsDefinitionIdModelGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("definitionId", definitionId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-definitions/{definitionId}/model").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<WorkflowModel> localVarReturnType = new ParameterizedTypeReference<WorkflowModel>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
    /**
     * <p>Retrieve sample start context of workflow definition by ID</p>
     * <p>Retrieves the default start context of the latest version of the specified workflow definition.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationParticipant </p>
     * <p><b>200</b> - Returns the default start context of the latest version of the workflow definition.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Either the specified workflow definition or the default sample context within the definition was not found.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param definitionId
     *      The workflow definition ID for which the default start context should be retrieved. The workflow definition ID is at most 64 characters long.
     * @return SampleContext
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public SampleContext v1WorkflowDefinitionsDefinitionIdSampleContextsDefaultStartContextGet( @Nonnull final String definitionId) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'definitionId' is set
        if (definitionId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'definitionId' when calling v1WorkflowDefinitionsDefinitionIdSampleContextsDefaultStartContextGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("definitionId", definitionId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-definitions/{definitionId}/sample-contexts/default-start-context").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<SampleContext> localVarReturnType = new ParameterizedTypeReference<SampleContext>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve all versions of workflow definition by ID</p>
     *<p>Retrieves a list of all deployed versions of the specified workflow definition.   Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin, ProcessAutomationDeveloper </p>
     * <p><b>200</b> - Returns a list of deployed workflow definitions.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Workflow definition not found. Either the payload does not contain a definitionId property or the specified ID is incorrect.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param definitionId  (required)
        The ID of the workflow definition for which all versions should be retrieved. The ID is at most 64 characters long.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $orderby  (optional, default to createdAt desc)
        Specify the attribute you want to sort by and the order separated by a space. If the order is omitted it is ascending by default. If not specified, the results are sorted by the &#39;createdAt&#39; attribute in descending order.
     * @param $skip  (optional, default to 0)
        Specify the number of records you want to skip from the beginning. You can skip at most 4000 records. To indicate a result range that starts, for example, at 1001, combine the $skip with the $top parameter. If not specified, no records are skipped. Refer also to the $top parameter.
     * @param $top  (optional, default to 100)
        Specify the number of records you want to show. You can get at most 1000 records per API call. To indicate a result range that starts, for example, at 1001, combine the $top with the $skip parameter. If not specified, 100 records are returned. Refer also to the $skip parameter.
     * @param $inlinecount  (optional, default to none)
        Specify whether the total count of the workflow definition versions should be returned as the value of the X-Total-Count response header. To enable the header, use the &#39;allpages&#39; setting. To disable the header, use the &#39;none&#39; setting. The values are case-sensitive.
     * @return List&lt;WorkflowDefinitionVersion&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<WorkflowDefinitionVersion> v1WorkflowDefinitionsDefinitionIdVersionsGet( @Nonnull final String definitionId,  @Nullable final String acceptLanguage,  @Nullable final String $orderby,  @Nullable final Integer $skip,  @Nullable final Integer $top,  @Nullable final String $inlinecount) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'definitionId' is set
        if (definitionId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'definitionId' when calling v1WorkflowDefinitionsDefinitionIdVersionsGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("definitionId", definitionId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-definitions/{definitionId}/versions").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$orderby", $orderby));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$skip", $skip));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$top", $top));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$inlinecount", $inlinecount));
        

        if (acceptLanguage != null)
            localVarHeaderParams.add("Accept-Language", apiClient.parameterToString(acceptLanguage));

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<List<WorkflowDefinitionVersion>> localVarReturnType = new ParameterizedTypeReference<List<WorkflowDefinitionVersion>>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve all versions of workflow definition by ID</p>
     * <p>Retrieves a list of all deployed versions of the specified workflow definition.   Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin, ProcessAutomationDeveloper </p>
     * <p><b>200</b> - Returns a list of deployed workflow definitions.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Workflow definition not found. Either the payload does not contain a definitionId property or the specified ID is incorrect.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param definitionId
     *      The ID of the workflow definition for which all versions should be retrieved. The ID is at most 64 characters long.
     * @return List&lt;WorkflowDefinitionVersion&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<WorkflowDefinitionVersion> v1WorkflowDefinitionsDefinitionIdVersionsGet( @Nonnull final String definitionId) throws OpenApiRequestException {
        return v1WorkflowDefinitionsDefinitionIdVersionsGet(definitionId, null, null, null, null, null);
    }

    /**
     * <p>Retrieve workflow definition by ID and version number</p>
     *<p>Retrieves the specified version of the specified workflow definition.   Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin, ProcessAutomationDeveloper </p>
     * <p><b>200</b> - Returns a single workflow definition.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Either the workflow definition or the specified version within that definition was not found.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param definitionId  (required)
        The ID of the workflow definition which should be retrieved. The ID is at most 64 characters long.
     * @param versionNumber  (required)
        The version number of the workflow definition that should be retrieved. The version number consists of 1 - 10 digits.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @return WorkflowDefinitionVersion
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowDefinitionVersion v1WorkflowDefinitionsDefinitionIdVersionsVersionNumberGet( @Nonnull final String definitionId,  @Nonnull final String versionNumber,  @Nullable final String acceptLanguage) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'definitionId' is set
        if (definitionId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'definitionId' when calling v1WorkflowDefinitionsDefinitionIdVersionsVersionNumberGet");
        }
        
        // verify the required parameter 'versionNumber' is set
        if (versionNumber == null) {
            throw new OpenApiRequestException("Missing the required parameter 'versionNumber' when calling v1WorkflowDefinitionsDefinitionIdVersionsVersionNumberGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("definitionId", definitionId);
        localVarPathParams.put("versionNumber", versionNumber);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-definitions/{definitionId}/versions/{versionNumber}").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        if (acceptLanguage != null)
            localVarHeaderParams.add("Accept-Language", apiClient.parameterToString(acceptLanguage));

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<WorkflowDefinitionVersion> localVarReturnType = new ParameterizedTypeReference<WorkflowDefinitionVersion>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve workflow definition by ID and version number</p>
     * <p>Retrieves the specified version of the specified workflow definition.   Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin, ProcessAutomationDeveloper </p>
     * <p><b>200</b> - Returns a single workflow definition.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Either the workflow definition or the specified version within that definition was not found.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param definitionId
     *      The ID of the workflow definition which should be retrieved. The ID is at most 64 characters long.
     * @param versionNumber
     *      The version number of the workflow definition that should be retrieved. The version number consists of 1 - 10 digits.
     * @return WorkflowDefinitionVersion
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowDefinitionVersion v1WorkflowDefinitionsDefinitionIdVersionsVersionNumberGet( @Nonnull final String definitionId,  @Nonnull final String versionNumber) throws OpenApiRequestException {
        return v1WorkflowDefinitionsDefinitionIdVersionsVersionNumberGet(definitionId, versionNumber, null);
    }
    /**
     * <p>Retrieve workflow definition model by ID and version number</p>
     * <p>Retrieves the model of the specified version of the specified workflow definition.   Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin, ProcessAutomationDeveloper </p>
     * <p><b>200</b> - The deployed workflow model.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Either the specified workflow definition, the specified version within that definition or the JSON model for that definition version was not found.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param definitionId
     *      The workflow definition ID for which the model should be retrieved. The workflow definition ID is at most 64 characters long.
     * @param versionNumber
     *      The version number for which the model should be retrieved. The version number consists of 1 - 10 digits.
     * @return WorkflowModel
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowModel v1WorkflowDefinitionsDefinitionIdVersionsVersionNumberModelGet( @Nonnull final String definitionId,  @Nonnull final String versionNumber) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'definitionId' is set
        if (definitionId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'definitionId' when calling v1WorkflowDefinitionsDefinitionIdVersionsVersionNumberModelGet");
        }
        
        // verify the required parameter 'versionNumber' is set
        if (versionNumber == null) {
            throw new OpenApiRequestException("Missing the required parameter 'versionNumber' when calling v1WorkflowDefinitionsDefinitionIdVersionsVersionNumberModelGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("definitionId", definitionId);
        localVarPathParams.put("versionNumber", versionNumber);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-definitions/{definitionId}/versions/{versionNumber}/model").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<WorkflowModel> localVarReturnType = new ParameterizedTypeReference<WorkflowModel>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
    /**
     * <p>Retrieve sample start context of workflow definition by ID and version number</p>
     * <p>Retrieves the default start context of the specified version of the specified workflow definition.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationParticipant </p>
     * <p><b>200</b> - Returns the default start context of the specified version of the specified workflow definition.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Either the specified workflow definition, the specified version within that definition or the default sample context within that definition version was not found.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param definitionId
     *      The workflow definition ID for which the default start context should be retrieved. The workflow definition ID is at most 64 characters long.
     * @param versionNumber
     *      The version number for which the default start context should be retrieved. The version number consists of 1 - 10 digits.
     * @return SampleContext
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public SampleContext v1WorkflowDefinitionsDefinitionIdVersionsVersionNumberSampleContextsDefaultStartContextGet( @Nonnull final String definitionId,  @Nonnull final String versionNumber) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'definitionId' is set
        if (definitionId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'definitionId' when calling v1WorkflowDefinitionsDefinitionIdVersionsVersionNumberSampleContextsDefaultStartContextGet");
        }
        
        // verify the required parameter 'versionNumber' is set
        if (versionNumber == null) {
            throw new OpenApiRequestException("Missing the required parameter 'versionNumber' when calling v1WorkflowDefinitionsDefinitionIdVersionsVersionNumberSampleContextsDefaultStartContextGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("definitionId", definitionId);
        localVarPathParams.put("versionNumber", versionNumber);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-definitions/{definitionId}/versions/{versionNumber}/sample-contexts/default-start-context").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<SampleContext> localVarReturnType = new ParameterizedTypeReference<SampleContext>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve all workflow definitions</p>
     *<p>Retrieves a list of the latest version of each deployed workflow definition. The request can be parameterized.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin, ProcessAutomationDeveloper </p>
     * <p><b>200</b> - Returns a list of deployed workflow definitions.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $orderby  (optional, default to createdAt desc)
        Specify the attribute you want to sort by and the order separated by a space. If the order is omitted it is ascending by default. If not specified, the results are sorted by the &#39;createdAt&#39; attribute in descending order.
     * @param $skip  (optional, default to 0)
        Specify the number of records you want to skip from the beginning. You can skip at most 4000 records. To indicate a result range that starts, for example, at 1001, combine the $skip with the $top parameter. If not specified, no records are skipped. Refer also to the $top parameter.
     * @param $top  (optional, default to 100)
        Specify the number of records you want to show. You can get at most 1000 records per API call. To indicate a result range that starts, for example, at 1001, combine the $top with the $skip parameter. If not specified, 100 records are returned. Refer also to the $skip parameter.
     * @param $inlinecount  (optional, default to none)
        Specify whether the total count of the workflow definitions should be returned as the value of the X-Total-Count response header. To enable the header, use the &#39;allpages&#39; setting. To disable the header, use the &#39;none&#39; setting. The values are case-sensitive.
     * @return List&lt;WorkflowDefinition&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<WorkflowDefinition> v1WorkflowDefinitionsGet( @Nullable final String acceptLanguage,  @Nullable final String $orderby,  @Nullable final Integer $skip,  @Nullable final Integer $top,  @Nullable final String $inlinecount) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-definitions").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$orderby", $orderby));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$skip", $skip));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$top", $top));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$inlinecount", $inlinecount));
        

        if (acceptLanguage != null)
            localVarHeaderParams.add("Accept-Language", apiClient.parameterToString(acceptLanguage));

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<List<WorkflowDefinition>> localVarReturnType = new ParameterizedTypeReference<List<WorkflowDefinition>>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve all workflow definitions</p>
     * <p>Retrieves a list of the latest version of each deployed workflow definition. The request can be parameterized.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin, ProcessAutomationDeveloper </p>
     * <p><b>200</b> - Returns a list of deployed workflow definitions.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @return List&lt;WorkflowDefinition&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<WorkflowDefinition> v1WorkflowDefinitionsGet() throws OpenApiRequestException {
        return v1WorkflowDefinitionsGet(null, null, null, null, null);
    }
}
