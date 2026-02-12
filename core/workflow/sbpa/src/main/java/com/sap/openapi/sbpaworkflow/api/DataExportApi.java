

package com.sap.openapi.sbpaworkflow.api;

import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.cloud.sdk.services.openapi.core.AbstractOpenApiService;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;

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
public class DataExportApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the Workflow.
     *
     * @param httpDestination The destination that API should be used with
     */
    public DataExportApi( @Nonnull final Destination httpDestination )
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
    public DataExportApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

    
    /**
     * <p>Data Export</p>
     *<p>Requests the export of workflow definitions metadata, form definitions metadata, workflow instances, and task instances. The file and data structures used are subject to change.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin </p>
     * <p><b>200</b> - Returns all data related to workflow instances and task instances as well as the latest version of all workflow definitions and form definitions as a .zip-file. The exported file and data structures are subject to change. For more information, see the product documentation at: https://help.sap.com/viewer/e157c391253b4ecd93647bf232d18a83/Cloud/en-US/5df7521ad6f74a24b84ab448fb58c66b.html.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>429</b> - You cannot run concurrent requests of this type for the current account. Please retry later.
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * <p><b>503</b> - The limit of concurrent requests of this type has been exceeded. Please retry later.
     * @param type  (optional, default to ALL)
        The type of data to be exported.
     * @param createdFrom  (optional)
        The start time (inclusive) of the time range when the workflow instance or user task instance was created.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param createdUpTo  (optional)
        The end time (inclusive) of the time range when the workflow instance or user task instance was created.  Supports two date formats: 1. yyyy-MM-dd&#39;T&#39;HH:mm:ss.SSS&#39;Z&#39; 2. yyyyMMddHHmmss.SSS 
     * @param collectionQuery  (optional)
        The collection query filter to apply. If provided, this parameter overrides the default taken from the current access path (service instance or subscription). Refer to [Configure the Access to Workflow Data](https://help.sap.com/docs/workflow-capability/workflow-cloud-foundry/configure-access-to-workflow-data) for more information.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse v1ExportGet( @Nullable final String type,  @Nullable final String createdFrom,  @Nullable final String createdUpTo,  @Nullable final String collectionQuery) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/v1/export").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "type", type));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "createdFrom", createdFrom));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "createdUpTo", createdUpTo));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "collectionQuery", collectionQuery));
        

        final String[] localVarAccepts = { 
            "application/zip", "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_ClientCredentials", "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<Void> localVarReturnType = new ParameterizedTypeReference<Void>() {};
        apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
        return new OpenApiResponse(apiClient);
    }

    /**
     * <p>Data Export</p>
     * <p>Requests the export of workflow definitions metadata, form definitions metadata, workflow instances, and task instances. The file and data structures used are subject to change.  Roles permitted to execute this operation:  - Global roles: ProcessAutomationAdmin </p>
     * <p><b>200</b> - Returns all data related to workflow instances and task instances as well as the latest version of all workflow definitions and form definitions as a .zip-file. The exported file and data structures are subject to change. For more information, see the product documentation at: https://help.sap.com/viewer/e157c391253b4ecd93647bf232d18a83/Cloud/en-US/5df7521ad6f74a24b84ab448fb58c66b.html.
     * <p><b>400</b> - Incorrect format or structure of the provided request body.
     * <p><b>401</b> - Unauthorized. You have not provided valid authentication credentials to access the resource.
     * <p><b>403</b> - Access forbidden. You have not the required permissions to access the resource.
     * <p><b>429</b> - You cannot run concurrent requests of this type for the current account. Please retry later.
     * <p><b>500</b> - Internal server error. The operation you requested led to an error during execution.
     * <p><b>503</b> - The limit of concurrent requests of this type has been exceeded. Please retry later.
     * @return An OpenApiResponse containing the status code of the HttpResponse.
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public OpenApiResponse v1ExportGet() throws OpenApiRequestException {
        return v1ExportGet(null, null, null, null);
    }
}
