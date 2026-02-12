

package com.sap.openapi.sbpaworkflow.api;

import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.cloud.sdk.services.openapi.core.AbstractOpenApiService;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;

import com.sap.openapi.sbpaworkflow.model.SubstitutesRule;
import com.sap.openapi.sbpaworkflow.model.SubstitutesRules;
import com.sap.openapi.sbpaworkflow.model.SubstitutionDeleteRule;
import com.sap.openapi.sbpaworkflow.model.SubstitutionError400;
import com.sap.openapi.sbpaworkflow.model.SubstitutionError403;
import com.sap.openapi.sbpaworkflow.model.SubstitutionError404;
import com.sap.openapi.sbpaworkflow.model.SubstitutionError405;
import com.sap.openapi.sbpaworkflow.model.SubstitutionError406;
import com.sap.openapi.sbpaworkflow.model.SubstitutionError500;
import com.sap.openapi.sbpaworkflow.model.SubstitutionRule;
import com.sap.openapi.sbpaworkflow.model.SubstitutionRuleCreatePayload;
import com.sap.openapi.sbpaworkflow.model.SubstitutionRules;
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
public class SubstitutionApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the Inbox.
     *
     * @param httpDestination The destination that API should be used with
     */
    public SubstitutionApi( @Nonnull final Destination httpDestination )
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
    public SubstitutionApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

        /**
     * <p>Delete a substitution rule with specified ID for the current user</p>
     * <p>Delete a substitution rule with specified ID for the current user. The request can be parameterized.</p>
     * <p><b>202</b> - Accepted the request to delete the substitution rule with the specified ID.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param substitutionRuleID
     *      The ID of the substitution rule to be deleted.
     * @return SubstitutionDeleteRule
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public SubstitutionDeleteRule deleteSubstitutionRuleSubstitutionRuleIDSubstitutionRuleIDPost( @Nonnull final String substitutionRuleID) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'substitutionRuleID' is set
        if (substitutionRuleID == null) {
            throw new OpenApiRequestException("Missing the required parameter 'substitutionRuleID' when calling deleteSubstitutionRuleSubstitutionRuleIDSubstitutionRuleIDPost");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("SubstitutionRuleID", substitutionRuleID);
        final String localVarPath = UriComponentsBuilder.fromPath("/DeleteSubstitutionRule?SubstitutionRuleID='{SubstitutionRuleID}'").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<SubstitutionDeleteRule> localVarReturnType = new ParameterizedTypeReference<SubstitutionDeleteRule>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.POST, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
    /**
     * <p>Support enabling and disabling of passive substitution rules</p>
     * <p>Support enabling and disabling of passive substitution rules. The request can be parameterized.</p>
     * <p><b>202</b> - Accepted the request to enable or disable the substitution rule with the specified id.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param substitutionRuleID
     *      The ID of the passive substitution rule to be enabled or disabled.
     * @param enabled
     *      Indicates whether the substitution rule is enabled.
     * @return SubstitutesRule
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public SubstitutesRule enableSubstitutionRuleSubstitutionRuleIDSubstitutionRuleIDEnabledEnabledPost( @Nonnull final String substitutionRuleID,  @Nonnull final Boolean enabled) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'substitutionRuleID' is set
        if (substitutionRuleID == null) {
            throw new OpenApiRequestException("Missing the required parameter 'substitutionRuleID' when calling enableSubstitutionRuleSubstitutionRuleIDSubstitutionRuleIDEnabledEnabledPost");
        }
        
        // verify the required parameter 'enabled' is set
        if (enabled == null) {
            throw new OpenApiRequestException("Missing the required parameter 'enabled' when calling enableSubstitutionRuleSubstitutionRuleIDSubstitutionRuleIDEnabledEnabledPost");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("SubstitutionRuleID", substitutionRuleID);
        localVarPathParams.put("Enabled", enabled);
        final String localVarPath = UriComponentsBuilder.fromPath("/EnableSubstitutionRule?SubstitutionRuleID='{SubstitutionRuleID}'&Enabled={Enabled}").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<SubstitutesRule> localVarReturnType = new ParameterizedTypeReference<SubstitutesRule>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.POST, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
    /**
     * <p>Retrieves a list of the substitution rules where the currently logged-in user is a substitute.</p>
     * <p>Retrieves a list of the substitution rules where the currently logged-in user is a substitute. The request can not be parameterized.</p>
     * <p><b>200</b> - Retrieves a list of the substitution rules where the currently logged-in user is a substitute.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @return SubstitutesRules
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public SubstitutesRules substitutesRuleCollectionGet() throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/SubstitutesRuleCollection").build().toUriString();

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

        final ParameterizedTypeReference<SubstitutesRules> localVarReturnType = new ParameterizedTypeReference<SubstitutesRules>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
    /**
     * <p>Retrieves a list of the substitution rules for the current user</p>
     * <p>Retrieves a list of the substitution rules for the current user. The request can not be parameterized.</p>
     * <p><b>200</b> - Returns a list of substitution rules for the current user.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @return SubstitutionRules
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public SubstitutionRules substitutionRuleCollectionGet() throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/SubstitutionRuleCollection").build().toUriString();

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

        final ParameterizedTypeReference<SubstitutionRules> localVarReturnType = new ParameterizedTypeReference<SubstitutionRules>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
    /**
     * <p>Creates a substitution rule for the current user</p>
     * <p>Creates a substitution rule for the current user. The request can be parameterized.</p>
     * <p><b>201</b> - Substitution rule for the current user has been created.
     * <p><b>400</b> - The syntax of the sent request is invalid.
     * <p><b>401</b> - Unauthorized. You do not have provided valid authentication credentials to access the resource.
     * <p><b>403</b> - The user who sent the request is not authorized to access the requested data or to perform the requested action.
     * <p><b>404</b> - The requested object could not be found.
     * <p><b>405</b> - The action to be executed is not supported by the OData service.
     * <p><b>406</b> - The requested object cannot be returned in the specified format according to the accept headers.
     * <p><b>500</b> - An error occurred while processing the request.
     * @param body
     *      Specify the new substitution rule.
     * @return SubstitutionRule
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public SubstitutionRule substitutionRuleCollectionPost( @Nonnull final SubstitutionRuleCreatePayload body) throws OpenApiRequestException {
        final Object localVarPostBody = body;
        
        // verify the required parameter 'body' is set
        if (body == null) {
            throw new OpenApiRequestException("Missing the required parameter 'body' when calling substitutionRuleCollectionPost");
        }
        
        final String localVarPath = UriComponentsBuilder.fromPath("/SubstitutionRuleCollection").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json", "application/xml"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "Oauth2_AuthorizationCode" };

        final ParameterizedTypeReference<SubstitutionRule> localVarReturnType = new ParameterizedTypeReference<SubstitutionRule>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.POST, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }
}
