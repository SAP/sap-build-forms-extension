

package com.sap.openapi.sbpaworkflow.api;

import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.cloud.sdk.services.openapi.core.AbstractOpenApiService;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;

import com.sap.openapi.sbpaworkflow.model.ConflictError;
import com.sap.openapi.sbpaworkflow.model.CustomAttribute;
import com.sap.openapi.sbpaworkflow.model.TaskInstance;
import com.sap.openapi.sbpaworkflow.model.TechnicalError;
import com.sap.openapi.sbpaworkflow.model.UnauthorizedError;
import com.sap.openapi.sbpaworkflow.model.UpdateTaskInstancePayload;
import com.sap.openapi.sbpaworkflow.model.UpdateTaskInstancesPayload;

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
public class UserTaskInstancesApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the Workflow.
     *
     * @param httpDestination The destination that API should be used with
     */
    public UserTaskInstancesApi( @Nonnull final Destination httpDestination )
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
    public UserTaskInstancesApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

    
    /**
     * <p>Retrieve instances by query parameters</p>
     *<p>Retrieves user task instances by parameters. If no parameters are specified, all instances with status READY, RESERVED, CANCELED, or COMPLETED are returned. Parameters for different attributes of the instance are evaluated using the logical &#39;and&#39; operator. If a parameter is specified multiple times, results are matched using the logical &#39;or&#39; operator, unless noted otherwise. Empty parameters are treated as if they were not specified. By default, returned tasks are sorted by creation time in ascending order.  Note: Certain selection criteria and response fields are not relevant for workflows that originate from the process builder of SAP Build Process Automation. They do exist for tasks that originate from other editors.  Roles permitted to execute this operation:   - Global roles: ProcessAutomationAdmin</p>
     * <p><b>200</b> - Returns a list of user task instances.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing task instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $skip  (optional, default to 0)
        The number of records you want to skip from the beginning. You can skip at most 4000 records. To indicate a result range that starts, for example, at 1001, combine the $skip with the $top parameter. If not specified, no records are skipped. You can use this parameter only once. Refer also to the $top parameter.
     * @param $top  (optional, default to 100)
        The number of records you want to show. You can get at most 1000 records per API call. To indicate a result range that starts, for example, at 1001, combine the $top with the $skip parameter. If not specified, 100 records are returned. You can use this parameter only once. Refer also to the $skip parameter.
     * @param $inlinecount  (optional, default to none)
        Specify whether the total count of the task instances, which match the search criteria, should be returned as a value of the X-Total-Count response header. To enable the header, use the &#39;allpages&#39; setting. To disable the header, use the &#39;none&#39; setting. The values are case-sensitive. You can use this parameter only once.
     * @param $expand  (optional)
        You can request custom task attributes to become part of the task output by specifying the value &#39;attributes&#39; for the &#39;$expand&#39; parameter. Otherwise, if the &#39;$expand&#39; parameter is not specified, the &#39;attributes&#39; field is not included into task output. Note that labels as well as the order of the custom task attributes in which they are returned, are taken from the latest versions of the workflow definitions where these attributes are present. 
     * @param $orderby  (optional, default to createdAt asc)
        The attribute and direction by which tasks are sorted. The sort attribute and direction are separated by a space. The parameter value is case-sensitive. You can only specify one ordering parameter, which contains attribute and direction. To stabilize the order of tasks the implicit second ordering parameter &#39;createdAt asc&#39; is applied.   * claimedAt asc - Ascending, task claim time from the oldest to the most recent, tasks with status READY (not yet claimed) first.  * claimedAt desc - Descending, from the most recently claimed tasks to the oldest ones, tasks with status READY (not yet claimed) last.  * completedAt asc - Ascending, task completion or cancelation time from the oldest to the most recent, not yet COMPLETED tasks first.  * completedAt desc - Descending, from the most recently COMPLETED or CANCELED tasks to the oldest ones, not yet COMPLETED tasks last.  * createdAt asc - Ascending, task creation time from the oldest to the most recent.  * createdAt desc - Descending, from the most recently created tasks to the oldest ones.  * lastChangedAt asc - Ascending, task last change time from the oldest to the most recent.  * lastChangedAt desc - Descending, from the most recently changed tasks to the oldest ones.  * dueDate asc - Ascending, tasks due date from the oldest to the most distant in future ones, tasks without due date first.  * dueDate desc - Descending, tasks due date from the most distant in future to the oldest ones, tasks without due date last.  * subject asc - Ascending, from A to Z case-sensitive.  * subject desc - Descending, from Z to A case-sensitive.  * description asc - Ascending, from A to Z case-sensitive, tasks without description first.  * description desc - Descending, from Z to A case-sensitive, tasks without description last.  * activityId asc - Ascending, from A to Z case-sensitive.  * activityId desc - Descending, from Z to A case-sensitive.  * id asc - Ascending, from A to Z case-sensitive.  * id desc - Descending, from Z to A case-sensitive.  * processor asc - Ascending, from A to Z case-sensitive, tasks without processor first.  * processor desc - Descending, from Z to A case-sensitive, tasks without processor last.  * workflowDefinitionId asc - Ascending, from A to Z case-sensitive.  * workflowDefinitionId desc - Descending, from Z to A case-sensitive.  * workflowInstanceId asc - Ascending, from A to Z case-sensitive.  * workflowInstanceId desc - Descending, from Z to A case-sensitive.  * priority asc - Ascending, tasks priority from lowest to highest.  * priority desc - Descending, tasks priority from highest to lowest.  * userInterfaceUri asc - Ascending, from A to Z case-sensitive.  * userInterfaceUri desc - Descending, from Z to A case-sensitive.
     * @param workflowInstanceId  (optional)
        The workflow instance ID for which the user task instances are returned. The workflow instance ID is 36 characters long.
     * @param workflowDefinitionId  (optional)
        The workflow definition ID for which the user task instances are returned.
     * @param processor  (optional)
        The processor of the user task instance.
     * @param id  (optional)
        The unique ID of the user task instance. The user task instance ID is 36 characters long.
     * @param activityId  (optional)
        The activityId of the user task instance. This field represents the ID of the user task definition.
     * @param description  (optional)
        The description of the user task instance in UTF-8 encoding. You must not specify texts that contain commas with this parameter.
     * @param subject  (optional)
        The subject of the user task instance in UTF-8 encoding. You must not specify texts that contain commas with this parameter.
     * @param createdAt  (optional)
        Time when the user task instance has been created. Evaluated using the logical &#39;or&#39; operator with the values of the &#39;createdFrom&#39; and &#39;createdUpTo&#39; parameters if they are specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param createdFrom  (optional)
        The start time (inclusive) of the time range when the user task instance was created. Must be less or equal to the value of the &#39;createdUpTo&#39; parameter if the latter is specified. Evaluated using the logical &#39;or&#39; operator with the value of the &#39;createdAt&#39; parameter if the latter is specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param createdUpTo  (optional)
        The end time (inclusive) of the time range when the user task instance was created. Must be greater or equal to the value of the &#39;createdFrom&#39; parameter if the latter is specified. Evaluated using the logical &#39;or&#39; operator with the value of the &#39;createdAt&#39; parameter if the latter is specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param claimedAt  (optional)
        Time when the user task instance was claimed. Evaluated using the logical &#39;or&#39; operator with the values of the &#39;claimedFrom&#39; and &#39;claimedUpTo&#39; parameters if they are specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param claimedFrom  (optional)
        The start time (inclusive) of the time range when the user task instance was claimed. Must be less or equal to the value of the &#39;claimedUpTo&#39; parameter if the latter is specified. Evaluated using the logical &#39;or&#39; operator with the value of the &#39;claimedAt&#39; parameter if the latter is specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param claimedUpTo  (optional)
        The end time (inclusive) of the time range when the user task instance was claimed. Must be greater or equal to the value of the &#39;claimedFrom&#39; parameter if the latter is specified. Evaluated using the logical &#39;or&#39; operator with the value of the &#39;claimedAt&#39; parameter if the latter is specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param completedAt  (optional)
        Time when the user task instance was COMPLETED or CANCELED. Evaluated using the logical &#39;or&#39; operator with the values of the &#39;completedFrom&#39; and &#39;completedUpTo&#39; parameters if they are specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param completedFrom  (optional)
        The start time (inclusive) of the time range when the user task instance was COMPLETED or CANCELED. Must be less or equal to the value of the &#39;completedUpTo&#39; parameter if the latter is specified. Evaluated using the logical &#39;or&#39; operator with the value of the &#39;completedAt&#39; parameter if the latter is specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param completedUpTo  (optional)
        The end time (inclusive) of the time range when the user task instance was COMPLETED or CANCELED. Must be greater or equal to the value of the &#39;completedFrom&#39; parameter if the latter is specified. Evaluated using the logical &#39;or&#39; operator with the value of the &#39;completedAt&#39; parameter if the latter is specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param lastChangedAt  (optional)
        Time when the user task instance has been changed for the last time. Evaluated using the logical &#39;or&#39; operator with the values of the &#39;lastChangedFrom&#39; and &#39;lastChangedTo&#39; parameters if they are specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param lastChangedFrom  (optional)
        The start time (inclusive) of the time range when the user task instance was changed for the last time. Must be less or equal to the value of the &#39;lastChangedUpTo&#39; parameter if the latter is specified. Evaluated using the logical &#39;or&#39; operator with the value of the &#39;lastChangedAt&#39; parameter if the latter is specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param lastChangedUpTo  (optional)
        The end time (inclusive) of the time range when the user task instance was changed for the last time. Must be greater or equal to the value of the &#39;lastChangedFrom&#39; parameter if the latter is specified. Evaluated using the logical &#39;or&#39; operator with the value of the &#39;lastChangedAt&#39; parameter if the latter is specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param dueDate  (optional)
        The due date of the user task instance. Evaluated using the logical &#39;or&#39; operator with the values of the &#39;dueDateFrom&#39; and &#39;dueDateUpTo&#39; parameters if they are specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param dueDateFrom  (optional)
        The start time (inclusive) of the due date time range for the user task instance. Must be less or equal to the value of the &#39;dueDateUpTo&#39; parameter if the latter is specified. Evaluated using the logical &#39;or&#39; operator with the value of the &#39;dueDate&#39; parameter if the latter is specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param dueDateUpTo  (optional)
        The end time (inclusive) of the due date time range for the user task instance. Must be greater or equal to the &#39;dueDateFrom&#39; parameter if the latter is specified. Evaluated using the logical &#39;or&#39; operator with the value of the &#39;dueDate&#39; parameter if the latter is specified.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param priority  (optional)
        The priority of the user task instance. The values are case-insensitive. 
     * @param status  (optional)
        The status of the user task instance. The values are case-insensitive. 
     * @param recipientUsers  (optional)
        The users who must be among the recipient users of the user task instance. A user task instance has to match at least one of the recipient users specified. Specify the users by repeating this parameter multiple times, with one user each. 
     * @param recipientGroups  (optional)
        The groups which must be among the recipient groups of the user task instance. A user task instance has to match at least one of the recipient groups specified. Specify the groups by repeating this parameter multiple times, with one user each. 
     * @param containsText  (optional)
        Text, which should be contained in at least one of the following fields of the user task instance:   * subject  * description  * activityId  * id  * processor  * workflowDefinitionId  * workflowInstanceId  Performed filtering is case-insensitive. You must not use this parameter more than once. 
     * @param attributesExampleCustomAttribute  (optional)
        Example of a parameter that filters task instances by the value of the custom task attribute with the ID &#39;ExampleCustomAttribute&#39;. You can specify parameters for other attributes in an analogous way. If you specify a parameter several times, the logical operators apply as outlined in the overall description of this API. If custom task attributes with different IDs are present in the URL, only tasks which contain all of them with the values specified will be returned as a response to the request. If a query by custom task attributes matches too many task definitions, you have to limit the query further. Refer, for example, to parameter &#39;definitionId&#39;. 
     * @param definitionId  (optional)
        The definition ID of the user task. It consists of the ID of the task defined in the workflow definition (task definition ID) as well as the workflow definition ID delimited by an &#39;@&#39; sign. For optimal performance, specify this parameter when searching for tasks by custom task attributes. If the custom task attribute IDs from the query match more than 50 task definitions, you have to specify one or more task definition IDs when searching for tasks by custom task attributes. 
     * @param userInterfaceUri  (optional)
        The userInterfaceUri of the user task. Please note that the value may have to be URL-encoded. 
     * @return List&lt;TaskInstance&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<TaskInstance> v1TaskInstancesGet( @Nullable final String acceptLanguage,  @Nullable final Integer $skip,  @Nullable final Integer $top,  @Nullable final String $inlinecount,  @Nullable final String $expand,  @Nullable final String $orderby,  @Nullable final String workflowInstanceId,  @Nullable final String workflowDefinitionId,  @Nullable final String processor,  @Nullable final String id,  @Nullable final String activityId,  @Nullable final String description,  @Nullable final String subject,  @Nullable final String createdAt,  @Nullable final String createdFrom,  @Nullable final String createdUpTo,  @Nullable final String claimedAt,  @Nullable final String claimedFrom,  @Nullable final String claimedUpTo,  @Nullable final String completedAt,  @Nullable final String completedFrom,  @Nullable final String completedUpTo,  @Nullable final String lastChangedAt,  @Nullable final String lastChangedFrom,  @Nullable final String lastChangedUpTo,  @Nullable final String dueDate,  @Nullable final String dueDateFrom,  @Nullable final String dueDateUpTo,  @Nullable final String priority,  @Nullable final String status,  @Nullable final String recipientUsers,  @Nullable final String recipientGroups,  @Nullable final String containsText,  @Nullable final String attributesExampleCustomAttribute,  @Nullable final String definitionId,  @Nullable final String userInterfaceUri) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/task-instances").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$skip", $skip));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$top", $top));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$inlinecount", $inlinecount));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$expand", $expand));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$orderby", $orderby));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "workflowInstanceId", workflowInstanceId));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "workflowDefinitionId", workflowDefinitionId));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "processor", processor));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "id", id));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "activityId", activityId));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "description", description));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "subject", subject));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "createdAt", createdAt));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "createdFrom", createdFrom));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "createdUpTo", createdUpTo));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "claimedAt", claimedAt));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "claimedFrom", claimedFrom));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "claimedUpTo", claimedUpTo));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "completedAt", completedAt));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "completedFrom", completedFrom));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "completedUpTo", completedUpTo));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "lastChangedAt", lastChangedAt));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "lastChangedFrom", lastChangedFrom));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "lastChangedUpTo", lastChangedUpTo));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "dueDate", dueDate));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "dueDateFrom", dueDateFrom));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "dueDateUpTo", dueDateUpTo));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "priority", priority));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "status", status));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "recipientUsers", recipientUsers));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "recipientGroups", recipientGroups));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "containsText", containsText));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "attributes.ExampleCustomAttribute", attributesExampleCustomAttribute));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "definitionId", definitionId));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "userInterfaceUri", userInterfaceUri));
        

        if (acceptLanguage != null)
            localVarHeaderParams.add("Accept-Language", apiClient.parameterToString(acceptLanguage));

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<List<TaskInstance>> localVarReturnType = new ParameterizedTypeReference<List<TaskInstance>>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve instances by query parameters</p>
     * <p>Retrieves user task instances by parameters. If no parameters are specified, all instances with status READY, RESERVED, CANCELED, or COMPLETED are returned. Parameters for different attributes of the instance are evaluated using the logical &#39;and&#39; operator. If a parameter is specified multiple times, results are matched using the logical &#39;or&#39; operator, unless noted otherwise. Empty parameters are treated as if they were not specified. By default, returned tasks are sorted by creation time in ascending order.  Note: Certain selection criteria and response fields are not relevant for workflows that originate from the process builder of SAP Build Process Automation. They do exist for tasks that originate from other editors.  Roles permitted to execute this operation:   - Global roles: ProcessAutomationAdmin</p>
     * <p><b>200</b> - Returns a list of user task instances.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing task instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @return List&lt;TaskInstance&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<TaskInstance> v1TaskInstancesGet() throws OpenApiRequestException {
        return v1TaskInstancesGet(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    /**
     * <p>Update a list of tasks</p>
     * <p>Updates the attributes of a list of user tasks. The user will need administrative privileges.  Note that patching a translated subject or description is not supported. That means, that GET requests that are executed on a translated user task do not display the patched text. Changing the context or status of a task instance is also not supported, even if this is possible via the patch endpoint for individual task instances. For more information, see the workflow service documentation on the [SAP Help Portal](https://help.sap.com/viewer/e157c391253b4ecd93647bf232d18a83/Cloud/en-US/fe41c54d61fa4710b34f2afe11b5d00e.html).  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin</p>
     * <p><b>202</b> - The request was successful and corresponding operations with task instances will be performed asynchronously. The response will include the header &#39;Location&#39; that points to a dedicated resource. Using this resource you can track the status of the operation.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing task instance.
     * <p><b>409</b> - The operation could not be executed because of another activity in the background. Please try again later.
     * <p><b>422</b> - The task cannot be updated for one of the following reasons: The values submitted for updating the property cannot be resolved or became invalid after resolution. The context provided in the request body contains prohibited fields or the status in the request body is invalid. 
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param body
     *      Specify the request body according to the given schema. The length of the request body is limited to ensure optimal operation of the service.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse v1TaskInstancesPatch( @Nonnull final List<UpdateTaskInstancesPayload> body) throws OpenApiRequestException {
        final Object localVarPostBody = body;
        
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new OpenApiRequestException("Missing the required parameter 'body' when calling v1TaskInstancesPatch");
        }
        
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/task-instances").build().toUriString();

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
     * <p>Retrieve custom task attributes by task instance ID</p>
     *<p>Retrieves custom task attributes of a user task. Labels as well as the order of the custom task attributes in which they are returned, are taken from the latest versions of the workflow definitions where these attributes are present.  Roles permitted to execute this operation: - Global roles: ProcessAutomationAdmin - Task-specific roles: recipientUsers, recipientGroups [Prerequisite: You are assigned to the ProcessAutomationParticipant global role.] </p>
     * <p><b>200</b> - Returns the custom task attributes for the requested user task.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing task instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param taskInstanceId  (required)
        The ID of the user task instance for which the custom task attributes should be retrieved. The ID is 36 characters long.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @return List&lt;CustomAttribute&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<CustomAttribute> v1TaskInstancesTaskInstanceIdAttributesGet( @Nonnull final String taskInstanceId,  @Nullable final String acceptLanguage) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'taskInstanceId' is set
        if (taskInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'taskInstanceId' when calling v1TaskInstancesTaskInstanceIdAttributesGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("taskInstanceId", taskInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/task-instances/{taskInstanceId}/attributes").buildAndExpand(localVarPathParams).toUriString();

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
     * <p>Retrieve custom task attributes by task instance ID</p>
     * <p>Retrieves custom task attributes of a user task. Labels as well as the order of the custom task attributes in which they are returned, are taken from the latest versions of the workflow definitions where these attributes are present.  Roles permitted to execute this operation: - Global roles: ProcessAutomationAdmin - Task-specific roles: recipientUsers, recipientGroups [Prerequisite: You are assigned to the ProcessAutomationParticipant global role.] </p>
     * <p><b>200</b> - Returns the custom task attributes for the requested user task.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing task instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param taskInstanceId
     *      The ID of the user task instance for which the custom task attributes should be retrieved. The ID is 36 characters long.
     * @return List&lt;CustomAttribute&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<CustomAttribute> v1TaskInstancesTaskInstanceIdAttributesGet( @Nonnull final String taskInstanceId) throws OpenApiRequestException {
        return v1TaskInstancesTaskInstanceIdAttributesGet(taskInstanceId, null);
    }
    /**
     * <p>Retrieve task context by ID</p>
     * <p>Retrieves the context of a user task.  Workflows that were created using the process builder of SAP Build Process Automation return the context as it was provided to the user task, that is, after input mapping. If there are no input mappings defined, this API returns the complete context.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin  - Task-specific roles: recipientUsers, recipientGroups [Prerequisite: You are assigned to the ProcessAutomationParticipant global role.]</p>
     * <p><b>200</b> - Returns the context of the specified user task.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing task instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param taskInstanceId
     *      The ID of the user task for which the context should be retrieved. The ID is 36 characters long.
     * @return Object
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public Object v1TaskInstancesTaskInstanceIdContextGet( @Nonnull final String taskInstanceId) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'taskInstanceId' is set
        if (taskInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'taskInstanceId' when calling v1TaskInstancesTaskInstanceIdContextGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("taskInstanceId", taskInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/task-instances/{taskInstanceId}/context").buildAndExpand(localVarPathParams).toUriString();

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
     * <p>Retrieve task by ID</p>
     *<p>Retrieves the user task instance with the specified task instance ID.  Roles permitted to execute this operation: - Global roles: ProcessAutomationAdmin - Task-specific roles: recipientUsers, recipientGroups [Prerequisite: You are assigned to the ProcessAutomationParticipant global role.]</p>
     * <p><b>200</b> - Returns the requested user task.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing task instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param taskInstanceId  (required)
        The ID of the user task instance which should be retrieved. The ID is 36 characters long.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $expand  (optional)
        You can request custom task attributes to become part of the task output by specifying the value &#39;attributes&#39; for the &#39;$expand&#39; parameter. Otherwise, if the &#39;$expand&#39; parameter is not specified, the &#39;attributes&#39; field is not included into task output. Note that labels as well as the order of the custom task attributes in which they are returned, are taken from the latest versions of the workflow definitions where these attributes are present. 
     * @return TaskInstance
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskInstance v1TaskInstancesTaskInstanceIdGet( @Nonnull final String taskInstanceId,  @Nullable final String acceptLanguage,  @Nullable final String $expand) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'taskInstanceId' is set
        if (taskInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'taskInstanceId' when calling v1TaskInstancesTaskInstanceIdGet");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("taskInstanceId", taskInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/task-instances/{taskInstanceId}").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<TaskInstance> localVarReturnType = new ParameterizedTypeReference<TaskInstance>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve task by ID</p>
     * <p>Retrieves the user task instance with the specified task instance ID.  Roles permitted to execute this operation: - Global roles: ProcessAutomationAdmin - Task-specific roles: recipientUsers, recipientGroups [Prerequisite: You are assigned to the ProcessAutomationParticipant global role.]</p>
     * <p><b>200</b> - Returns the requested user task.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing task instance.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param taskInstanceId
     *      The ID of the user task instance which should be retrieved. The ID is 36 characters long.
     * @return TaskInstance
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public TaskInstance v1TaskInstancesTaskInstanceIdGet( @Nonnull final String taskInstanceId) throws OpenApiRequestException {
        return v1TaskInstancesTaskInstanceIdGet(taskInstanceId, null, null);
    }
    /**
     * <p>Update task by ID</p>
     * <p>Updates the status of a user task, its properties, for example, the subject, and its context with the attributes provided in the request body. Depending on the provided attributes, the user might need administrative privileges for the task instance. Without administrative privileges, a user can only set the status to COMPLETED. Optionally, the user can change the context when completing the task.  Workflows validate and restrict the update of the context if they originate from the process builder of SAP Build Process Automation. If the validation fails, the response code is 422. See the detail fields of the response body on the specific validations that failed.  Note that patching a translated subject or description is not supported. That means, that GET requests that are executed on a translated user task do not display the patched text.  For more information, see the workflow service documentation on the [SAP Help Portal](https://help.sap.com/viewer/e157c391253b4ecd93647bf232d18a83/Cloud/en-US/fe41c54d61fa4710b34f2afe11b5d00e.html).  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin  - Task-specific roles: recipientUsers, recipientGroups [Prerequisite: You are assigned to the ProcessAutomationParticipant global role.]</p>
     * <p><b>204</b> - Depending on the request, either the task has been COMPLETED and the context updated, or the task properties have been updated.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>404</b> - URL not found. Check whether the URL is correct and whether you refer to an existing task instance.
     * <p><b>409</b> - The operation could not be executed because of another activity in the background. Please try again later.
     * <p><b>422</b> - The task cannot be updated for one of the following reasons: The values submitted for updating the property cannot be resolved or became invalid after resolution. The context provided in the request body contains prohibited fields or the status in the request body is invalid. 
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param taskInstanceId
     *      The ID of the user task instance that is to be updated. The ID is 36 characters long.
     * @param body
     *      Specify the request body according to the given schema. The length of the request body is limited to ensure optimal operation of the service.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse v1TaskInstancesTaskInstanceIdPatch( @Nonnull final String taskInstanceId,  @Nonnull final UpdateTaskInstancePayload body) throws OpenApiRequestException {
        final Object localVarPostBody = body;
        
        // verify the required parameter 'taskInstanceId' is set
        if (taskInstanceId == null) {
            throw new OpenApiRequestException("Missing the required parameter 'taskInstanceId' when calling v1TaskInstancesTaskInstanceIdPatch");
        }
        
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new OpenApiRequestException("Missing the required parameter 'body' when calling v1TaskInstancesTaskInstanceIdPatch");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("taskInstanceId", taskInstanceId);
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/task-instances/{taskInstanceId}").buildAndExpand(localVarPathParams).toUriString();

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
