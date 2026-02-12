

package com.sap.openapi.sbpaworkflow.api;

import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.cloud.sdk.services.openapi.core.AbstractOpenApiService;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;

import com.sap.openapi.sbpaworkflow.model.ConflictError;
import com.sap.openapi.sbpaworkflow.model.CustomAttribute;
import com.sap.openapi.sbpaworkflow.model.TechnicalError;
import com.sap.openapi.sbpaworkflow.model.UnauthorizedError;
import com.sap.openapi.sbpaworkflow.model.WorkflowInstance;
import com.sap.openapi.sbpaworkflow.model.WorkflowInstanceErrorMessage;
import com.sap.openapi.sbpaworkflow.model.WorkflowInstanceExecutionLog;
import com.sap.openapi.sbpaworkflow.model.WorkflowInstanceStartPayload;
import com.sap.openapi.sbpaworkflow.model.WorkflowInstanceUpdatePayload;
import com.sap.openapi.sbpaworkflow.model.WorkflowInstancesUpdatePayload;

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
public class WorkflowInstancesApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the Workflow.
     *
     * @param httpDestination The destination that API should be used with
     */
    public WorkflowInstancesApi( @Nonnull final Destination httpDestination )
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
    public WorkflowInstancesApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

    
    /**
     * <p>Retrieve all instances by query parameters</p>
     *<p>Retrieves workflow instances by parameters. If no parameters are specified, all RUNNING, or ERRONEOUS instances are returned. Parameters for different attributes of the instance are evaluated using the logical &#39;and&#39; operator. If multiple parameters are specified for the same attribute or a parameter is specified multiple times, results are matched using the logical &#39;or&#39; operator, unless noted otherwise. Empty parameters are treated as if they were not given.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin </p>
     * <p><b>200</b> - Returns a list of workflow instances.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $orderby  (optional, default to startedAt desc)
        The attribute and direction by which workflow instances are sorted. The sort attribute and direction are separated by a space. The parameter value is case-sensitive. You can only specify one ordering parameter, which contains attribute and direction. If sort by definitionVersion is enabled, then definitionId is also added to the sorting query. If not specified, the results are sorted by the &#39;startedAt&#39; attribute, in descending order.   * id asc - Ascending, from A to Z case-sensitive.  * id desc - Descending, from Z to A case-sensitive.  * definitionId asc - Ascending, from A to Z case-sensitive.  * definitionId desc - Descending, from Z to A case-sensitive.  * definitionVersion asc - Ascending, from A to Z case-sensitive.  * definitionVersion desc - Descending, from Z to A case-sensitive.  * startedAt asc - Ascending, from A to Z case-sensitive.  * startedAt desc - Descending, from Z to A case-sensitive.  * completedAt asc - Ascending, workflow instance completion or cancelation time from the oldest to the most recent, not yet COMPLETED workflow instances first.  * completedAt desc - Descending, from the most recently COMPLETED or CANCELED workflow instance to the oldest ones, not yet COMPLETED workflow instances last.  * startedBy asc - Ascending, from A to Z case-sensitive.  * startedBy desc - Descending, from Z to A case-sensitive.  * subject asc - Ascending, from A to Z case-sensitive, workflow instances without subject first.  * subject desc - Descending, from Z to A case-sensitive, workflow instances without subject last.  * businessKey asc - Ascending, from A to Z case-sensitive, workflow instances without business key first.  * businessKey desc - Descending, from Z to A case-sensitive, workflow instances without business key last.
     * @param $skip  (optional, default to 0)
        Specify the number of records you want to skip from the beginning. You can skip at most 4000 records. To indicate a result range that starts, for example, at 1001, combine the $skip with the $top parameter. If not specified, no records are skipped. Refer also to the $top parameter.
     * @param $top  (optional, default to 100)
        Specify the number of records you want to show. You can get at most 1000 records per API call. To indicate a result range that starts, for example, at 1001, combine the $top with the $skip parameter. If not specified, 100 records are returned. Refer also to the $skip parameter.
     * @param $inlinecount  (optional, default to none)
        Specify whether the total count of the workflow instances, which match the search criteria, should be returned as a value of the X-Total-Count response header. To enable the header, use the &#39;allpages&#39; setting. To disable the header, use the &#39;none&#39; setting. The values are case-sensitive. 
     * @param $expand  (optional)
        You can request custom workflow attributes to become part of the workflow instance output by specifying the value &#39;attributes&#39; for the &#39;$expand&#39; parameter. Otherwise, if the &#39;$expand&#39; parameter is not specified, the &#39;attributes&#39; field is not included into the output of the workflow instance. Note that labels as well as the order of the custom workflow attributes in which they are returned, are taken from the latest versions of the workflow definitions where these attributes are present. 
     * @param id  (optional)
        Specify the workflow instance ID. The workflow instance ID is 36 characters long.
     * @param definitionId  (optional)
        Specify the ID of the workflow definition. The ID is at most 255 characters long.
     * @param definitionVersion  (optional)
        Specify the version of the workflow definition.
     * @param status  (optional)
        Specify the status of the workflow instance. The values are not case-sensitive. The status values as required by this API have the following corresponding terms in user interfaces of SAP Build Process Automation:   * RUNNING - Running   * ERRONEOUS - Error   * SUSPENDED - On Hold   * CANCELED - Canceled   * COMPLETED - Completed
     * @param startedAt  (optional)
        Specify at what time the workflow instance was started. Supports two date formats 1) yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2) yyyyMMddHHmmss.SSS.
     * @param startedFrom  (optional)
        Specify the beginning (inclusive) of the time range during which a returned workflow was started.  Supports two date formats:  1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 1. yyyyMMddHHmmss.SSS  The parameter operates on the &#39;startedAt&#39; attribute of a workflow instance. It is combined with the following parameters, if available: &#39;startedUpTo&#39; parameter using the logical &#39;and&#39; operator; &#39;startedAt&#39; parameter using the logical &#39;or&#39; operator. 
     * @param startedUpTo  (optional)
        Specify the end (inclusive) of the time range during which a returned workflow was started.  Supports two date formats:  1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 1. yyyyMMddHHmmss.SSS  The parameter operates on the &#39;startedAt&#39; attribute of a workflow instance. It is combined with the following parameters, if available: &#39;startedFrom&#39; parameter using the logical &#39;and&#39; operator; &#39;startedAt&#39; parameter using the logical &#39;or&#39; operator. 
     * @param completedAt  (optional)
        Specify at what time the workflow instance was changed to status COMPLETED or CANCELED.  Supports two date formats:  1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 1. yyyyMMddHHmmss.SSS 
     * @param completedFrom  (optional)
        Specify the beginning (inclusive) of the time range during which a returned workflow was changed to status COMPLETED or CANCELED.  Supports two date formats:  1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 1. yyyyMMddHHmmss.SSS  The parameter operates on the &#39;completedAt&#39; attribute of a workflow instance. It is combined with the following parameters, if available: &#39;completedUpTo&#39; parameter using the logical &#39;and&#39; operator; &#39;completedAt&#39; parameter using the logical &#39;or&#39; operator. 
     * @param completedUpTo  (optional)
        Specify the end (inclusive) of the time range during which a returned workflow was changed to status COMPLETED or CANCELED.  Supports two date formats:  1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 1. yyyyMMddHHmmss.SSS  The parameter operates on the &#39;completedAt&#39; attribute of a workflow instance. It is combined with the following parameters, if available: &#39;completedFrom&#39; parameter using the logical &#39;and&#39; operator; &#39;completedAt&#39; parameter using the logical &#39;or&#39; operator. 
     * @param startedBy  (optional)
        Specify the user who started the workflow instance, in UTF-8 encoding. The user ID is at most 255 characters long.
     * @param subject  (optional)
        Specify the subject of the workflow instance, in UTF-8 encoding. The subject of a workflow instance is limited to 255 characters.
     * @param containsText  (optional)
        Specify the text to search for, in UTF-8 encoding. Searches the id, definitionId, subject, startedBy, and businessKey attributes of the workflow instances. The maximum length of these attributes is 255 characters. 
     * @param businessKey  (optional)
        Specify the business key of the workflow instance, in UTF-8 encoding. The business key of a workflow instance is limited to 255 characters.
     * @param rootInstanceId  (optional)
        Specify the workflow root instance ID. The workflow root instance ID is 36 characters long.
     * @param parentInstanceId  (optional)
        Specify the workflow parent instance ID. The workflow parent instance ID is 36 characters long or it can be null.
     * @param projectId  (optional)
        Specify the SAP Build Process Automation project ID of the workflow instance. The project ID of a workflow instance is limited to 255 characters. 
     * @param projectVersion  (optional)
        Specify the SAP Build Process Automation project version of the workflow instance. The project version of a workflow instance is limited to 64 characters. 
     * @param attributesExampleCustomAttribute  (optional)
        Example of a parameter that filters workflow instances by the value of the custom workflow attribute with the ID &#39;ExampleCustomAttribute&#39;. You can specify parameters for other attributes in an analogous way. If you specify a parameter several times, the logical operators apply as outlined in the overall description of this API. If custom workflow attributes with different IDs are present in the URL, only workflow instances which contain all of them with the values specified will be returned as a response to the request. If a query by custom workflow attributes matches too many workflow definitions, you have to limit the query further. Refer, for example, to parameter &#39;definitionId&#39;. 
     * @return List&lt;WorkflowInstance&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<WorkflowInstance> v1WorkflowInstancesGet( @Nullable final String acceptLanguage,  @Nullable final String $orderby,  @Nullable final Integer $skip,  @Nullable final Integer $top,  @Nullable final String $inlinecount,  @Nullable final String $expand,  @Nullable final String id,  @Nullable final String definitionId,  @Nullable final String definitionVersion,  @Nullable final String status,  @Nullable final String startedAt,  @Nullable final String startedFrom,  @Nullable final String startedUpTo,  @Nullable final String completedAt,  @Nullable final String completedFrom,  @Nullable final String completedUpTo,  @Nullable final String startedBy,  @Nullable final String subject,  @Nullable final String containsText,  @Nullable final String businessKey,  @Nullable final String rootInstanceId,  @Nullable final String parentInstanceId,  @Nullable final String projectId,  @Nullable final String projectVersion,  @Nullable final String attributesExampleCustomAttribute) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$orderby", $orderby));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$skip", $skip));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$top", $top));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$inlinecount", $inlinecount));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$expand", $expand));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "definitionId", definitionId));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "definitionVersion", definitionVersion));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "status", status));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "startedAt", startedAt));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "startedFrom", startedFrom));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "startedUpTo", startedUpTo));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "completedAt", completedAt));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "completedFrom", completedFrom));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "completedUpTo", completedUpTo));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "startedBy", startedBy));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "subject", subject));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "containsText", containsText));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "businessKey", businessKey));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "rootInstanceId", rootInstanceId));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "parentInstanceId", parentInstanceId));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "projectId", projectId));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "projectVersion", projectVersion));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "attributes.ExampleCustomAttribute", attributesExampleCustomAttribute));
        

        if (acceptLanguage != null)
            localVarHeaderParams.add("Accept-Language", apiClient.parameterToString(acceptLanguage));

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<List<WorkflowInstance>> localVarReturnType = new ParameterizedTypeReference<List<WorkflowInstance>>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve all instances by query parameters</p>
     * <p>Retrieves workflow instances by parameters. If no parameters are specified, all RUNNING, or ERRONEOUS instances are returned. Parameters for different attributes of the instance are evaluated using the logical &#39;and&#39; operator. If multiple parameters are specified for the same attribute or a parameter is specified multiple times, results are matched using the logical &#39;or&#39; operator, unless noted otherwise. Empty parameters are treated as if they were not given.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin </p>
     * <p><b>200</b> - Returns a list of workflow instances.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @return List&lt;WorkflowInstance&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<WorkflowInstance> v1WorkflowInstancesGet() throws OpenApiRequestException {
        return v1WorkflowInstancesGet(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    /**
     * <p>Delete instance by ID</p>
     * <p>Modifies the given workflow instances according to the specified operations. Currently, the only operation supported is the deletion of workflow instances. You can at most specify 10000 instances to delete in one API call.  Roles permitted to execute this operation: - Global roles: ProcessAutomationAdmin</p>
     * <p><b>200</b> - Requested operations were completed successfully. Note: You also receive this response code if you have not requested an operation on any of the workflow instances or if the list of workflow instances was empty. 
     * <p><b>202</b> - The request was successful and corresponding operations with process instances will be performed asynchronously. The response will include the header &#39;Location&#39; that points to a dedicated resource. Using this resource you can track the status of the operation.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>422</b> - The request body contains at least one entry for which you have not provided a workflow instance ID.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * @param body
     *      Specify the request body according to the given schema. Note that the length of the request body is limited to ensure optimal operation of the service.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse v1WorkflowInstancesPatch( @Nonnull final List<WorkflowInstancesUpdatePayload> body) throws OpenApiRequestException {
        final Object localVarPostBody = body;
        
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new OpenApiRequestException("Missing the required parameter 'body' when calling v1WorkflowInstancesPatch");
        }
        
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances").build().toUriString();

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

        final ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        apiClient.invokeAPI(localVarPath, HttpMethod.PATCH, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        return new OpenApiResponse(apiClient);
    }

    /**
     * <p>Start a new instance</p>
     *<p>Starts a new workflow instance of the provided workflow definition. Specify the ID of the workflow definition in the body. The workflow instance automatically starts based on the latest deployed version of the definition.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationParticipant </p>
     * <p><b>201</b> - Returns the newly created workflow instance. Note that subject and businessKey are &#39;null&#39; and are evaluated after the workflow instance was started. If the process has a start event output mapping, the two attributes are evaluated based on the result of the mapping. If the process does not have a start event output mapping, the attributes are evaluated based on the payload that was sent in the request. To receive these values, query the instance by ID after the mapping is done.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - Workflow definition not found. Either the payload does not contain a definitionId property or the specified ID is incorrect.
     * <p><b>422</b> - The workflow context in the request body contains invalid keys or values.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param body  (required)
        Specify the request body according to the given schema. Note that the length of the request body is limited to ensure optimal operation of the service.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @return WorkflowInstance
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowInstance v1WorkflowInstancesPost( @Nonnull final WorkflowInstanceStartPayload body,  @Nullable final String acceptLanguage) throws OpenApiRequestException {
        final Object localVarPostBody = body;
        
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new OpenApiRequestException("Missing the required parameter 'body' when calling v1WorkflowInstancesPost");
        }
        
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances").build().toUriString();

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

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<WorkflowInstance> localVarReturnType = new ParameterizedTypeReference<WorkflowInstance>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.POST, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
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
     * @param body
     *      Specify the request body according to the given schema. Note that the length of the request body is limited to ensure optimal operation of the service.
     * @return WorkflowInstance
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowInstance v1WorkflowInstancesPost( @Nonnull final WorkflowInstanceStartPayload body) throws OpenApiRequestException {
        return v1WorkflowInstancesPost(body, null);
    }

    /**
     * <p>Retrieve custom workflow attributes by workflow instance ID</p>
     *<p>Retrieves custom workflow attributes for a workflow instance. Labels as well as the order of the custom workflow attributes in which they are returned, are taken from the latest versions of the workflow definitions where these attributes are present.  Roles permitted to execute this operation: - Global roles: ProcessAutomationAdmin </p>
     * <p><b>200</b> - Returns the custom workflow attributes for the requested workflow instance.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param workflowInstanceId  (required)
        The ID of the workflow instance for which the custom workflow attributes should be retrieved. The ID is 36 characters long.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @return List&lt;CustomAttribute&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<CustomAttribute> v1WorkflowInstancesWorkflowInstanceIdAttributesGet( @Nonnull final String workflowInstanceId,  @Nullable final String acceptLanguage) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'workflowInstanceId' is set
        if (workflowInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'workflowInstanceId' when calling v1WorkflowInstancesWorkflowInstanceIdAttributesGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("workflowInstanceId", workflowInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances/{workflowInstanceId}/attributes").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<List<CustomAttribute>> localVarReturnType = new ParameterizedTypeReference<List<CustomAttribute>>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve custom workflow attributes by workflow instance ID</p>
     * <p>Retrieves custom workflow attributes for a workflow instance. Labels as well as the order of the custom workflow attributes in which they are returned, are taken from the latest versions of the workflow definitions where these attributes are present.  Roles permitted to execute this operation: - Global roles: ProcessAutomationAdmin </p>
     * <p><b>200</b> - Returns the custom workflow attributes for the requested workflow instance.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param workflowInstanceId
     *      The ID of the workflow instance for which the custom workflow attributes should be retrieved. The ID is 36 characters long.
     * @return List&lt;CustomAttribute&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<CustomAttribute> v1WorkflowInstancesWorkflowInstanceIdAttributesGet( @Nonnull final String workflowInstanceId) throws OpenApiRequestException {
        return v1WorkflowInstancesWorkflowInstanceIdAttributesGet(workflowInstanceId, null);
    }
    /**
     * <p>Retrieve instance context by path</p>
     * <p>Retrieves a fragment of the context for a workflow instance independent of its status.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin</p>
     * <p><b>200</b> - Returns a fragment of the context of the workflow instance.
     * <p><b>204</b> - The context path is undefined and does not exist.
     * <p><b>400</b> - The context path is too long.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param workflowInstanceId
     *      The workflow instance ID for which the context should be retrieved. The workflow instance ID is 36 characters long.
     * @param contextPath
     *      A path to the fragment of the context to be retrieved in simplified dot notation.  Features supported in dot notation: - Access to nested properties, e.g. a.b.c.d - Access to array elements, e.g. a.b[0] - Access to nested array elements, e.g. a.b[0][1]
     * @return Object
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nullable
    public Object v1WorkflowInstancesWorkflowInstanceIdContextContextPathGet( @Nonnull final String workflowInstanceId,  @Nonnull final String contextPath) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'workflowInstanceId' is set
        if (workflowInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'workflowInstanceId' when calling v1WorkflowInstancesWorkflowInstanceIdContextContextPathGet");
        }
        
        // verify the required parameter 'contextPath' is set
        if (contextPath == null) {
            throw new OpenApiRequestException("Missing the required parameter 'contextPath' when calling v1WorkflowInstancesWorkflowInstanceIdContextContextPathGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("workflowInstanceId", workflowInstanceId);
        localVarPathParams.put("contextPath", contextPath);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances/{workflowInstanceId}/context/{contextPath}").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<Object> localVarReturnType = new ParameterizedTypeReference<Object>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
    /**
     * <p>Retrieve instance context</p>
     * <p>Retrieves the context for a workflow instance independent of its status.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin</p>
     * <p><b>200</b> - Returns the context of the workflow instance.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param workflowInstanceId
     *      The workflow instance ID for which the context should be retrieved. The workflow instance ID is 36 characters long.
     * @return Object
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public Object v1WorkflowInstancesWorkflowInstanceIdContextGet( @Nonnull final String workflowInstanceId) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'workflowInstanceId' is set
        if (workflowInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'workflowInstanceId' when calling v1WorkflowInstancesWorkflowInstanceIdContextGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("workflowInstanceId", workflowInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances/{workflowInstanceId}/context").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<Object> localVarReturnType = new ParameterizedTypeReference<Object>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
    /**
     * <p>Update instance context</p>
     * <p>Modifies parts of the context for a workflow instance independent of its status. Allows updating the workflow context to remove sensitive data, leaving the workflow instance in place.  Take special care when using this API, because it might change the workflow context in ways that are incompatible with the expectations of the tasks in the workflow definition. Before changing the context, we recommend that you suspend the workflow instance and make sure that the execution has come to a halt, that is, that no further steps are being added to the execution log. Refer to PATCH on the parent resource and GET on the &#39;execution-logs&#39; sibling resource.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin</p>
     * <p><b>204</b> - The context has been updated.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>409</b> - The operation could not be executed because of another activity in the background. Please try again later.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param workflowInstanceId
     *      Specify the workflow instance ID for which the context should be modified. The workflow instance ID is 36 characters long.
     * @param body
     *      Specify the modified parts of the context. Note that the modification is limited by the allowed content length of the request body. The length is restricted to ensure optimal operation of the service.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse v1WorkflowInstancesWorkflowInstanceIdContextPatch( @Nonnull final String workflowInstanceId,  @Nonnull final Object body) throws OpenApiRequestException {
        final Object localVarPostBody = body;
        
        // verify the required parameter 'workflowInstanceId' is set
        if (workflowInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'workflowInstanceId' when calling v1WorkflowInstancesWorkflowInstanceIdContextPatch");
        }
        
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new OpenApiRequestException("Missing the required parameter 'body' when calling v1WorkflowInstancesWorkflowInstanceIdContextPatch");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("workflowInstanceId", workflowInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances/{workflowInstanceId}/context").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        apiClient.invokeAPI(localVarPath, HttpMethod.PATCH, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        return new OpenApiResponse(apiClient);
    }
    /**
     * <p>Overwrite instance context</p>
     * <p>Overrides the context for a workflow instance independent of its status.  Take special care when using this API, because it will override the workflow context, that is, it might change the workflow context in ways that are incompatible with the expectations of the tasks in the workflow definition. Before changing the context, we recommend that you suspend the workflow instance and check that the execution has come to a halt, that is, no further steps are being added to the execution log. Refer to PATCH on the parent resource and GET on the &#39;execution-logs&#39; sibling resource.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin</p>
     * <p><b>204</b> - The context has been updated.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>409</b> - The operation could not be executed because of another activity in the background. Please try again later.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param workflowInstanceId
     *      Specify the workflow instance ID for which the context should be overridden. The workflow instance ID is 36 characters long.
     * @param body
     *      Specify the new context according to the given schema. The length of the request body is limited to ensure optimal operation of the service.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse v1WorkflowInstancesWorkflowInstanceIdContextPut( @Nonnull final String workflowInstanceId,  @Nonnull final Object body) throws OpenApiRequestException {
        final Object localVarPostBody = body;
        
        // verify the required parameter 'workflowInstanceId' is set
        if (workflowInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'workflowInstanceId' when calling v1WorkflowInstancesWorkflowInstanceIdContextPut");
        }
        
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new OpenApiRequestException("Missing the required parameter 'body' when calling v1WorkflowInstancesWorkflowInstanceIdContextPut");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("workflowInstanceId", workflowInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances/{workflowInstanceId}/context").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        apiClient.invokeAPI(localVarPath, HttpMethod.PUT, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        return new OpenApiResponse(apiClient);
    }
    /**
     * <p>Retrieve error messages</p>
     * <p>Retrieves current error messages for a workflow instance.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationDeveloper, ProcessAutomationAdmin </p>
     * <p><b>200</b> - Returns a list of error messages.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param workflowInstanceId
     *      The workflow instance ID for which the error messages should be retrieved. The workflow instance ID is 36 characters long.
     * @return List&lt;WorkflowInstanceErrorMessage&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<WorkflowInstanceErrorMessage> v1WorkflowInstancesWorkflowInstanceIdErrorMessagesGet( @Nonnull final String workflowInstanceId) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'workflowInstanceId' is set
        if (workflowInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'workflowInstanceId' when calling v1WorkflowInstancesWorkflowInstanceIdErrorMessagesGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("workflowInstanceId", workflowInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances/{workflowInstanceId}/error-messages").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<List<WorkflowInstanceErrorMessage>> localVarReturnType = new ParameterizedTypeReference<List<WorkflowInstanceErrorMessage>>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve execution logs</p>
     *<p>Retrieves execution logs for a given workflow instance.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin </p>
     * <p><b>200</b> - Returns a list of execution logs.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param workflowInstanceId  (required)
        The workflow instance ID for which the execution logs should be retrieved. The workflow instance ID is 36 characters long.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @return List&lt;WorkflowInstanceExecutionLog&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<WorkflowInstanceExecutionLog> v1WorkflowInstancesWorkflowInstanceIdExecutionLogsGet( @Nonnull final String workflowInstanceId,  @Nullable final String acceptLanguage) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'workflowInstanceId' is set
        if (workflowInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'workflowInstanceId' when calling v1WorkflowInstancesWorkflowInstanceIdExecutionLogsGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("workflowInstanceId", workflowInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances/{workflowInstanceId}/execution-logs").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<List<WorkflowInstanceExecutionLog>> localVarReturnType = new ParameterizedTypeReference<List<WorkflowInstanceExecutionLog>>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve execution logs</p>
     * <p>Retrieves execution logs for a given workflow instance.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin </p>
     * <p><b>200</b> - Returns a list of execution logs.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param workflowInstanceId
     *      The workflow instance ID for which the execution logs should be retrieved. The workflow instance ID is 36 characters long.
     * @return List&lt;WorkflowInstanceExecutionLog&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<WorkflowInstanceExecutionLog> v1WorkflowInstancesWorkflowInstanceIdExecutionLogsGet( @Nonnull final String workflowInstanceId) throws OpenApiRequestException {
        return v1WorkflowInstancesWorkflowInstanceIdExecutionLogsGet(workflowInstanceId, null);
    }

    /**
     * <p>Retrieve workflow instance by ID</p>
     *<p>Retrieves the workflow instance with the specified workflow instance ID.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin</p>
     * <p><b>200</b> - The requested workflow instance.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param workflowInstanceId  (required)
        The ID of the workflow instance, which should be retrieved. The workflow instance ID is 36 characters long.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $expand  (optional)
        You can request custom workflow attributes to become part of the workflow instance output by specifying the value &#39;attributes&#39; for the &#39;$expand&#39; parameter. Otherwise, if the &#39;$expand&#39; parameter is not specified, the &#39;attributes&#39; field is not included into the output of the workflow instance. Note that labels as well as the order of the custom workflow attributes in which they are returned, are taken from the latest versions of the workflow definitions where these attributes are present. 
     * @return WorkflowInstance
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowInstance v1WorkflowInstancesWorkflowInstanceIdGet( @Nonnull final String workflowInstanceId,  @Nullable final String acceptLanguage,  @Nullable final String $expand) throws OpenApiRequestException {
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

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<WorkflowInstance> localVarReturnType = new ParameterizedTypeReference<WorkflowInstance>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
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
     * @param workflowInstanceId
     *      The ID of the workflow instance, which should be retrieved. The workflow instance ID is 36 characters long.
     * @return WorkflowInstance
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public WorkflowInstance v1WorkflowInstancesWorkflowInstanceIdGet( @Nonnull final String workflowInstanceId) throws OpenApiRequestException {
        return v1WorkflowInstancesWorkflowInstanceIdGet(workflowInstanceId, null, null);
    }
    /**
     * <p>Retrieve instance outputs</p>
     * <p>Retrieves the outputs for a workflow instance. Outputs are only available for workflow instances that are in status COMPLETED.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin</p>
     * <p><b>200</b> - Returns the outputs of the workflow instance.
     * <p><b>204</b> - No outputs have been provided for this workflow instance.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing workflow instance.
     * <p><b>409</b> - The operation could not be executed because the workflow instance is not in status COMPLETED. Please try again later.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param workflowInstanceId
     *      The workflow instance ID for which the outputs should be retrieved. The workflow instance ID is 36 characters long.
     * @return Object
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nullable
    public Object v1WorkflowInstancesWorkflowInstanceIdOutputsGet( @Nonnull final String workflowInstanceId) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'workflowInstanceId' is set
        if (workflowInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'workflowInstanceId' when calling v1WorkflowInstancesWorkflowInstanceIdOutputsGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("workflowInstanceId", workflowInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/workflow-instances/{workflowInstanceId}/outputs").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<Object> localVarReturnType = new ParameterizedTypeReference<Object>() {};
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
     * @param workflowInstanceId
     *      The ID of the workflow instance, which should be modified. The workflow instance ID is 36 characters long.
     * @param body
     *      Specify the request body according to the given schema. The length of the request body is limited to ensure optimal operation of the service.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse v1WorkflowInstancesWorkflowInstanceIdPatch( @Nonnull final String workflowInstanceId,  @Nonnull final WorkflowInstanceUpdatePayload body) throws OpenApiRequestException {
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

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        apiClient.invokeAPI(localVarPath, HttpMethod.PATCH, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        return new OpenApiResponse(apiClient);
    }
}
