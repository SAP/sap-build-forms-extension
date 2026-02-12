

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
import com.sap.openapi.sbpaworkflow.model.TaskCustomAttributeDatas;
import com.sap.openapi.sbpaworkflow.model.TaskDefinition;
import com.sap.openapi.sbpaworkflow.model.TaskDescription;
import com.sap.openapi.sbpaworkflow.model.TaskInstance;
import com.sap.openapi.sbpaworkflow.model.TaskInstances;
import com.sap.openapi.sbpaworkflow.model.UnauthorizedError;
import com.sap.openapi.sbpaworkflow.model.WorkflowLogs;

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
public class TaskCollectionApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the Inbox.
     *
     * @param httpDestination The destination that API should be used with
     */
    public TaskCollectionApi( @Nonnull final Destination httpDestination )
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
    public TaskCollectionApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

    
    /**
     * <p>Retrieves the number of the tasks for the current user.</p>
     *<p>Retrieves the number of tasks for the current user. The request can be parameterized.</p>
     * <p><b>200</b> - Returns the number of task instances for the current user.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param $filter  (optional, default to Status eq &#39;READY&#39; or Status eq &#39;RESERVED&#39;)
        Specify the filter attribute for tasks using the following format: $filter&#x3D;attribute eq &#39;value&#39;.  Filtering on &#39;CreatedOn&#39; and &#39;CompletionDeadline&#39; supports the greater-equal (&#39;ge&#39;) and lower-equal (&#39;le&#39;) operator. All other attributes only support filtering on equality (&#39;eq&#39;).  Multiple filters on different attributes can be combined with &#39;and&#39; (for example: &#x60;Status eq &#39;READY&#39; and Priority eq &#39;HIGH&#39;&#x60;).  Filtering the TaskDefinitionID, Status, Priority, and PriorityNumber attributes supports multiple values that are combined with &#39;or&#39; (for example: &#x60;Status eq &#39;READY&#39; or Status eq &#39;RESERVED&#39;&#x60;).  You can only filter the Status attribute using the &#39;READY&#39;, &#39;RESERVED&#39;, &#39;IN_PROGRESS&#39;, and &#39;EXECUTED&#39; values for comparison with the &#39;eq&#39; operator. Note: &#39;IN_PROGRESS&#39; and &#39;EXECUTED&#39; are supported, but no tasks have this value for the Status attribute. Other SAP workflow systems that implement the same API also support the &#39;COMPLETED&#39; and &#39;FOR_RESUBMISSION&#39; attribute values. If you refer to these, or any other value not mentioned previously, the request fails with HTTP response code 400.
     * @return Integer
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public Integer taskCollectionCountGet( @Nullable final String $filter) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/TaskCollection/$count").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$filter", $filter));
        

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
     * <p>Retrieves the number of the tasks for the current user.</p>
     * <p>Retrieves the number of tasks for the current user. The request can be parameterized.</p>
     * <p><b>200</b> - Returns the number of task instances for the current user.
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
    public Integer taskCollectionCountGet() throws OpenApiRequestException {
        return taskCollectionCountGet(null);
    }

    /**
     * <p>Retrieves a list of the tasks for the current user</p>
     *<p>Retrieves a list of the tasks for the current user. The request can be parameterized.</p>
     * <p><b>200</b> - Returns a list of task instances for the current user.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $expand  (optional, default to Description)
        Expand attributes of the task. The enumeration of allowed values is subject to change. Separate multiple values by a comma.
     * @param $skip  (optional, default to 0)
        Specify the number of records you want to skip from the beginning. You can skip at most 4000 records. To indicate a result range that starts, for example, at 1001, combine the $skip with the $top parameter. If not specified, no records are skipped. Refer also to the $top parameter.
     * @param $top  (optional, default to 100)
        Specify the number of records you want to show. You can see at most 1000 records per API call. To indicate a result range that starts, for example, at 1001, combine the $top with the $skip parameter. If not specified, 100 records are returned. Refer also to the $skip parameter.
     * @param $filter  (optional, default to Status eq &#39;READY&#39; or Status eq &#39;RESERVED&#39;)
        Specify the filter attribute for tasks using the following format: &#x60;$filter&#x3D;attribute eq &#39;value&#39;&#x60;. More complex filter predicates are only supported as described in the following.  Filtering on &#39;CreatedOn&#39; and &#39;CompletionDeadline&#39; supports the greater-equal (&#39;ge&#39;) and lower-equal (&#39;le&#39;) operator. All other attributes only support filtering on equality (&#39;eq&#39;).  Multiple filters on different attributes can be combined with &#39;and&#39; (for example: &#x60;Status eq &#39;READY&#39; and Priority eq &#39;HIGH&#39;&#x60;).  Filtering the TaskDefinitionID, Status, Priority, and PriorityNumber attributes supports multiple values that are combined with &#39;or&#39; (for example: &#x60;Status eq &#39;READY&#39; or Status eq &#39;RESERVED&#39;&#x60;).  You can only filter the Status attribute using the &#39;READY&#39;, &#39;RESERVED&#39;, &#39;IN_PROGRESS&#39;, and &#39;EXECUTED&#39; values for comparison with the &#39;eq&#39; operator. Note: &#39;IN_PROGRESS&#39; and &#39;EXECUTED&#39; are supported, but no tasks have this value for the Status attribute. Other SAP workflow systems that implement the same API also support the &#39;COMPLETED&#39; and &#39;FOR_RESUBMISSION&#39; attribute values. If you refer to these, or any other value not mentioned previously, the request fails with HTTP response code 400.  Filter predicates which are based on the &#39;CustomAttributeData&#39; navigation property must provide the name of the task attribute and its value combined with &#39;and&#39;, for example &#x60;CustomAttributeData/Name eq &#39;ProjectId&#39; and CustomAttributeData/Value eq &#39;PRO_247&#39;&#x60;. These predicates can be combined with &#39;and&#39; to filter on different attributes. They can be combined with &#39;or&#39; to filter multiple values, for example: &#x60;(CustomAttributeData/Name eq &#39;ProjectId&#39; and CustomAttributeData/Value eq &#39;PRO_247&#39;) or (CustomAttributeData/Name eq &#39;ProjectId&#39; and CustomAttributeData/Value eq &#39;PRO_007&#39;)&#x60;. For optimal performance, filter also the &#39;TaskDefinitionID&#39; attribute of candidate tasks. If more than 50 different task definitions match the query based on the &#39;CustomAttributeData/Name&#39; predicates, you must specify a predicate on the &#39;TaskDefinitionID&#39; attribute.
     * @param $orderby  (optional, default to CreatedOn desc)
        Specify the attribute you want to sort by and the order separated by a space. Up to two attributes can be used (for example: &#39;CreatedOn desc, Priority desc&#39;). Using more attributes is not allowed. Results are sorted in the given sequence.  When sorting by TaskDefinitionName, it has to be the first in the sequence.
     * @param $inlinecount  (optional, default to none)
        Specify whether to return the number of results in the result body. When the value is &#39;allpages&#39;, the number of results is returned in the &#39;__count&#39; attribute of the response body.
     * @param $format  (optional, default to xml)
        Specify the format of the result. 
     * @return TaskInstances
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskInstances taskCollectionGet( @Nullable final String acceptLanguage,  @Nullable final String $expand,  @Nullable final Integer $skip,  @Nullable final Integer $top,  @Nullable final String $filter,  @Nullable final String $orderby,  @Nullable final String $inlinecount,  @Nullable final String $format) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/TaskCollection").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$expand", $expand));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$skip", $skip));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$top", $top));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$filter", $filter));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$orderby", $orderby));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$inlinecount", $inlinecount));
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

        final ParameterizedTypeReference<TaskInstances> localVarReturnType = new ParameterizedTypeReference<TaskInstances>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieves a list of the tasks for the current user</p>
     * <p>Retrieves a list of the tasks for the current user. The request can be parameterized.</p>
     * <p><b>200</b> - Returns a list of task instances for the current user.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @return TaskInstances
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskInstances taskCollectionGet() throws OpenApiRequestException {
        return taskCollectionGet(null, null, null, null, null, null, null, null);
    }

    /**
     * <p>Retrieves the custom attribute data for a given task</p>
     *<p>Retrieves the custom attribute data for a given task. The request can be parameterized.</p>
     * <p><b>200</b> - Returns the custom attribute data of a given task.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param saPOrigin  (required)
        The SID discriminator. Set to &#39;NA&#39; for all entities. For more information, see the Task Consumption Model documentation in SAP Note [2304317](https://launchpad.support.sap.com/#/notes/2304317/E).
     * @param instanceID  (required)
        The ID of the task.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $format  (optional, default to xml)
        Specify the format of the result. 
     * @return TaskCustomAttributeDatas
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskCustomAttributeDatas taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDCustomAttributeDataGet( @Nonnull final String saPOrigin,  @Nonnull final String instanceID,  @Nullable final String acceptLanguage,  @Nullable final String $format) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'saPOrigin' is set
        if (saPOrigin == null) {
            throw new OpenApiRequestException("Missing the required parameter 'saPOrigin' when calling taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDCustomAttributeDataGet");
        }
        
        // verify the required parameter 'instanceID' is set
        if (instanceID == null) {
            throw new OpenApiRequestException("Missing the required parameter 'instanceID' when calling taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDCustomAttributeDataGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("SAP__Origin", saPOrigin);
        localVarPathParams.put("InstanceID", instanceID);
        final String localVarPath = UriComponentsBuilder.fromPath("/TaskCollection(SAP__Origin='{SAP__Origin}',InstanceID='{InstanceID}')/CustomAttributeData").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<TaskCustomAttributeDatas> localVarReturnType = new ParameterizedTypeReference<TaskCustomAttributeDatas>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieves the custom attribute data for a given task</p>
     * <p>Retrieves the custom attribute data for a given task. The request can be parameterized.</p>
     * <p><b>200</b> - Returns the custom attribute data of a given task.
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
     *      The ID of the task.
     * @return TaskCustomAttributeDatas
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskCustomAttributeDatas taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDCustomAttributeDataGet( @Nonnull final String saPOrigin,  @Nonnull final String instanceID) throws OpenApiRequestException {
        return taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDCustomAttributeDataGet(saPOrigin, instanceID, null, null);
    }

    /**
     * <p>Retrieves the description of a task</p>
     *<p>Retrieves the description of a task.</p>
     * <p><b>200</b> - Returns the description for a given task.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param saPOrigin  (required)
        The SID discriminator. Set to &#39;NA&#39; for all entities. For more information, see the Task Consumption Model documentation in SAP Note [2304317](https://launchpad.support.sap.com/#/notes/2304317/E).
     * @param instanceID  (required)
        The ID of the task.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $format  (optional, default to xml)
        Specify the format of the result. 
     * @return TaskDescription
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskDescription taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDDescriptionGet( @Nonnull final String saPOrigin,  @Nonnull final String instanceID,  @Nullable final String acceptLanguage,  @Nullable final String $format) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'saPOrigin' is set
        if (saPOrigin == null) {
            throw new OpenApiRequestException("Missing the required parameter 'saPOrigin' when calling taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDDescriptionGet");
        }
        
        // verify the required parameter 'instanceID' is set
        if (instanceID == null) {
            throw new OpenApiRequestException("Missing the required parameter 'instanceID' when calling taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDDescriptionGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("SAP__Origin", saPOrigin);
        localVarPathParams.put("InstanceID", instanceID);
        final String localVarPath = UriComponentsBuilder.fromPath("/TaskCollection(SAP__Origin='{SAP__Origin}',InstanceID='{InstanceID}')/Description").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<TaskDescription> localVarReturnType = new ParameterizedTypeReference<TaskDescription>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieves the description of a task</p>
     * <p>Retrieves the description of a task.</p>
     * <p><b>200</b> - Returns the description for a given task.
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
     *      The ID of the task.
     * @return TaskDescription
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskDescription taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDDescriptionGet( @Nonnull final String saPOrigin,  @Nonnull final String instanceID) throws OpenApiRequestException {
        return taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDDescriptionGet(saPOrigin, instanceID, null, null);
    }

    /**
     * <p>Retrieves a task for the current user</p>
     *<p>Retrieves a task for the current user. The request can be parameterized.</p>
     * <p><b>200</b> - Returns a task.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param saPOrigin  (required)
        The SID discriminator. Set to &#39;NA&#39; for all entities. For more information, see the Task Consumption Model documentation in SAP Note [2304317](https://launchpad.support.sap.com/#/notes/2304317/E).
     * @param instanceID  (required)
        The ID of the task.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $format  (optional, default to xml)
        Specify the format of the result. 
     * @return TaskInstance
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskInstance taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDGet( @Nonnull final String saPOrigin,  @Nonnull final String instanceID,  @Nullable final String acceptLanguage,  @Nullable final String $format) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'saPOrigin' is set
        if (saPOrigin == null) {
            throw new OpenApiRequestException("Missing the required parameter 'saPOrigin' when calling taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDGet");
        }
        
        // verify the required parameter 'instanceID' is set
        if (instanceID == null) {
            throw new OpenApiRequestException("Missing the required parameter 'instanceID' when calling taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("SAP__Origin", saPOrigin);
        localVarPathParams.put("InstanceID", instanceID);
        final String localVarPath = UriComponentsBuilder.fromPath("/TaskCollection(SAP__Origin='{SAP__Origin}',InstanceID='{InstanceID}')").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<TaskInstance> localVarReturnType = new ParameterizedTypeReference<TaskInstance>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieves a task for the current user</p>
     * <p>Retrieves a task for the current user. The request can be parameterized.</p>
     * <p><b>200</b> - Returns a task.
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
     *      The ID of the task.
     * @return TaskInstance
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskInstance taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDGet( @Nonnull final String saPOrigin,  @Nonnull final String instanceID) throws OpenApiRequestException {
        return taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDGet(saPOrigin, instanceID, null, null);
    }

    /**
     * <p>Retrieves the task definition data for a given task</p>
     *<p>Retrieves the task definition data for a given task. The request can be parameterized.</p>
     * <p><b>200</b> - Returns the task definition data of a given task.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param saPOrigin  (required)
        The SID discriminator. Set to &#39;NA&#39; for all entities. For more information, see the Task Consumption Model documentation in SAP Note [2304317](https://launchpad.support.sap.com/#/notes/2304317/E).
     * @param instanceID  (required)
        The ID of the task.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $format  (optional, default to xml)
        Specify the format of the result. 
     * @return TaskDefinition
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskDefinition taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDTaskDefinitionDataGet( @Nonnull final String saPOrigin,  @Nonnull final String instanceID,  @Nullable final String acceptLanguage,  @Nullable final String $format) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'saPOrigin' is set
        if (saPOrigin == null) {
            throw new OpenApiRequestException("Missing the required parameter 'saPOrigin' when calling taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDTaskDefinitionDataGet");
        }
        
        // verify the required parameter 'instanceID' is set
        if (instanceID == null) {
            throw new OpenApiRequestException("Missing the required parameter 'instanceID' when calling taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDTaskDefinitionDataGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("SAP__Origin", saPOrigin);
        localVarPathParams.put("InstanceID", instanceID);
        final String localVarPath = UriComponentsBuilder.fromPath("/TaskCollection(SAP__Origin='{SAP__Origin}',InstanceID='{InstanceID}')/TaskDefinitionData").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<TaskDefinition> localVarReturnType = new ParameterizedTypeReference<TaskDefinition>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieves the task definition data for a given task</p>
     * <p>Retrieves the task definition data for a given task. The request can be parameterized.</p>
     * <p><b>200</b> - Returns the task definition data of a given task.
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
     *      The ID of the task.
     * @return TaskDefinition
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskDefinition taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDTaskDefinitionDataGet( @Nonnull final String saPOrigin,  @Nonnull final String instanceID) throws OpenApiRequestException {
        return taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDTaskDefinitionDataGet(saPOrigin, instanceID, null, null);
    }

    /**
     * <p>Retrieves the history of the workflow for the given task</p>
     *<p>Retrieves the history of the workflow for the given task, with detailed information about the tasks that are part of the workflow.</p>
     * <p><b>200</b> - Returns the workflow logs for a given task.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param saPOrigin  (required)
        The SID discriminator. Set to &#39;NA&#39; for all entities. For more information, see the Task Consumption Model documentation in SAP Note [2304317](https://launchpad.support.sap.com/#/notes/2304317/E).
     * @param instanceID  (required)
        The ID of the task.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $format  (optional, default to xml)
        Specify the format of the result. 
     * @return WorkflowLogs
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowLogs taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDWorkflowLogsGet( @Nonnull final String saPOrigin,  @Nonnull final String instanceID,  @Nullable final String acceptLanguage,  @Nullable final String $format) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'saPOrigin' is set
        if (saPOrigin == null) {
            throw new OpenApiRequestException("Missing the required parameter 'saPOrigin' when calling taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDWorkflowLogsGet");
        }
        
        // verify the required parameter 'instanceID' is set
        if (instanceID == null) {
            throw new OpenApiRequestException("Missing the required parameter 'instanceID' when calling taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDWorkflowLogsGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("SAP__Origin", saPOrigin);
        localVarPathParams.put("InstanceID", instanceID);
        final String localVarPath = UriComponentsBuilder.fromPath("/TaskCollection(SAP__Origin='{SAP__Origin}',InstanceID='{InstanceID}')/WorkflowLogs").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<WorkflowLogs> localVarReturnType = new ParameterizedTypeReference<WorkflowLogs>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieves the history of the workflow for the given task</p>
     * <p>Retrieves the history of the workflow for the given task, with detailed information about the tasks that are part of the workflow.</p>
     * <p><b>200</b> - Returns the workflow logs for a given task.
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
     *      The ID of the task.
     * @return WorkflowLogs
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowLogs taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDWorkflowLogsGet( @Nonnull final String saPOrigin,  @Nonnull final String instanceID) throws OpenApiRequestException {
        return taskCollectionSAPOriginSAPOriginInstanceIDInstanceIDWorkflowLogsGet(saPOrigin, instanceID, null, null);
    }
}
