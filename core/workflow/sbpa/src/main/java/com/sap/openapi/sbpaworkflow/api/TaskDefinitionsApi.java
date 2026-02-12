

package com.sap.openapi.sbpaworkflow.api;

import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.cloud.sdk.services.openapi.core.AbstractOpenApiService;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;

import com.sap.openapi.sbpaworkflow.model.TaskDefinitionListInner;
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
public class TaskDefinitionsApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the Workflow.
     *
     * @param httpDestination The destination that API should be used with
     */
    public TaskDefinitionsApi( @Nonnull final Destination httpDestination )
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
    public TaskDefinitionsApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

    
    /**
     * <p>Retrieve definitions by query parameters</p>
     *<p>Retrieves task definitions by query parameters.  A task definition is identified by the ID of the respective activity within a workflow definition (for example, usertask1) and the workflow definition ID. The workflow definition ID is version independent. That means, this API assumes that task definitions are semantically the same if they span several workflow versions and therefore have the same identifier. The latest workflow definition version is expected to contain the leading property values of the task definition.  At the moment, filtering is limited to the $skip and $top parameters for paging through the available task definitions.  The returned task definitions are sorted in descending order of their creation time.  Roles permitted to execute this operation:   - Global roles: ProcessAutomationAdmin</p>
     * <p><b>200</b> - Returns a list of task definitions.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @param acceptLanguage  (optional)
        Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param $skip  (optional, default to 0)
        The number of records you want to skip from the beginning. You can skip at most 4000 records. To indicate a result range that starts, for example, at 1001, combine the $skip with the $top parameter. If not specified, no records are skipped. You can use this parameter only once. Refer also to the $top parameter.
     * @param $top  (optional, default to 100)
        The number of records you want to show. You can get at most 1000 records per API call. To indicate a result range that starts, for example, at 1001, combine the $top with the $skip parameter. If not specified, 100 records are returned. You can use this parameter only once. Refer also to the $skip parameter.
     * @param $inlinecount  (optional, default to none)
        Specify whether the total count of the task definitions should be returned as a value of the X-Total-Count response header. To enable the header, use the &#39;allpages&#39; setting. To disable the header, use the &#39;none&#39; setting. The values are case-sensitive. You can use this parameter only once.
     * @param $expand  (optional)
        You can request custom task attributes to become part of the task output by specifying the value &#39;attributeDefinitions&#39; for the &#39;$expand&#39; parameter. Otherwise, if the &#39;$expand&#39; parameter is not specified, the &#39;attributeDefinitions&#39; field is not included into the output. Note that labels as well as the order of the custom task attributes in which they are returned, are taken from the latest versions of the workflow definitions where the corresponding task definition exists.
     * @return List&lt;TaskDefinitionListInner&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<TaskDefinitionListInner> v1TaskDefinitionsGet( @Nullable final String acceptLanguage,  @Nullable final Integer $skip,  @Nullable final Integer $top,  @Nullable final String $inlinecount,  @Nullable final String $expand) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/task-definitions").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$skip", $skip));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$top", $top));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "$inlinecount", $inlinecount));
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

        final ParameterizedTypeReference<List<TaskDefinitionListInner>> localVarReturnType = new ParameterizedTypeReference<List<TaskDefinitionListInner>>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Retrieve definitions by query parameters</p>
     * <p>Retrieves task definitions by query parameters.  A task definition is identified by the ID of the respective activity within a workflow definition (for example, usertask1) and the workflow definition ID. The workflow definition ID is version independent. That means, this API assumes that task definitions are semantically the same if they span several workflow versions and therefore have the same identifier. The latest workflow definition version is expected to contain the leading property values of the task definition.  At the moment, filtering is limited to the $skip and $top parameters for paging through the available task definitions.  The returned task definitions are sorted in descending order of their creation time.  Roles permitted to execute this operation:   - Global roles: ProcessAutomationAdmin</p>
     * <p><b>200</b> - Returns a list of task definitions.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources. 
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * @return List&lt;TaskDefinitionListInner&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<TaskDefinitionListInner> v1TaskDefinitionsGet() throws OpenApiRequestException {
        return v1TaskDefinitionsGet(null, null, null, null, null);
    }
}
