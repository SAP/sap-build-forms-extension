package com.sap.bfx.workflow.sbpa;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;
import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.openapi.sbpaworkflow.api.WorkflowDefinitionsApi;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class WorkflowDefinitionsApiExtended extends WorkflowDefinitionsApi {

    public WorkflowDefinitionsApiExtended(@NotNull Destination httpDestination) {
        super(httpDestination);
    }

    public WorkflowDefinitionsApiExtended(@NotNull ApiClient apiClient) {
        super(apiClient);
    }

    /**
     * <p>Retrieve all workflow definitions</p>
     * <p>Retrieves a list of the latest version of each deployed workflow definition. The request can be parameterized.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin, ProcessAutomationDeveloper </p>
     * <p><b>200</b> - Returns a list of deployed workflow definitions.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources.
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     *
     * @return List&lt;WorkflowDefinitionExtended&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<WorkflowDefinitionExtended> v1WorkflowDefinitionsExtendedGet() throws OpenApiRequestException {
        return v1WorkflowDefinitionsExtendedGet(null, null, null, null, null, null);
    }

    /**
     * <p>Retrieve all workflow definitions</p>
     * <p>Retrieves a list of the latest version of each deployed workflow definition. The request can be parameterized.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin, ProcessAutomationDeveloper </p>
     * <p><b>200</b> - Returns a list of deployed workflow definitions.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>429</b> - You have reached the usage limits that are configured for your tenant. You are performing too many requests or consume too many resources.
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     *
     * @param acceptLanguage (optional)
     *                       Provide a preferred language. If a translation is available, relevant texts are returned in this language.
     * @param apiKey         (optional)
     *                       Provide an api-key for shared environment of SBPA. If api-key is available, relevant header will be set.
     * @param $orderby       (optional, default to createdAt desc)
     *                       Specify the attribute you want to sort by and the order separated by a space. If the order is omitted it is ascending by default. If not specified, the results are sorted by the &#39;createdAt&#39; attribute in descending order.
     * @param $skip          (optional, default to 0)
     *                       Specify the number of records you want to skip from the beginning. You can skip at most 4000 records. To indicate a result range that starts, for example, at 1001, combine the $skip with the $top parameter. If not specified, no records are skipped. Refer also to the $top parameter.
     * @param $top           (optional, default to 100)
     *                       Specify the number of records you want to show. You can get at most 1000 records per API call. To indicate a result range that starts, for example, at 1001, combine the $top with the $skip parameter. If not specified, 100 records are returned. Refer also to the $skip parameter.
     * @param $inlinecount   (optional, default to none)
     *                       Specify whether the total count of the workflow definitions should be returned as the value of the X-Total-Count response header. To enable the header, use the &#39;allpages&#39; setting. To disable the header, use the &#39;none&#39; setting. The values are case-sensitive.
     * @return List&lt;WorkflowDefinitionExtended&gt;
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public List<WorkflowDefinitionExtended> v1WorkflowDefinitionsExtendedGet(@Nullable final String acceptLanguage, @Nullable final String apiKey, @Nullable final String $orderby, @Nullable final Integer $skip, @Nullable final Integer $top, @Nullable final String $inlinecount) throws OpenApiRequestException {
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

        if (apiKey != null) localVarHeaderParams.add(WorkflowConstants.API_KEY, apiKey);

        final String[] localVarAccepts = {
                "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = {};
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[]{"Oauth2_ClientCredentials", "Oauth2_AuthorizationCode"};

        final ParameterizedTypeReference<List<WorkflowDefinitionExtended>> localVarReturnType = new ParameterizedTypeReference<List<WorkflowDefinitionExtended>>() {
        };
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
