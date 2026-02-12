

package com.sap.openapi.scim.api;

import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.cloud.sdk.services.openapi.core.AbstractOpenApiService;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;

import com.sap.openapi.scim.model.AuthorizationError;
import com.sap.openapi.scim.model.GenericError;
import com.sap.openapi.scim.model.ScimGroup;
import com.sap.openapi.scim.model.ScimGroupPatch;
import com.sap.openapi.scim.model.ScimGroups;

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
 * User Management (System for Cross-domain Identity Management (SCIM)) in version 1.0.0.
 *
 * Provides functions to administrate the Authorization and Trust Management service (XSUAA) of SAP BTP, Cloud Foundry environment. You can provision users from identity providers and manage roles and role collections. Use this API to manage shadow users; users the service provisions from your identity provider to the subaccount. For more information, see [Switch Off Automatic Creation of Shadow Users](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/d8525671e8b14147b96ef497e1e1af80.html). The System for Cross-domain Identity Management (SCIM) interfaces for users and groups supplement the relevant UAA [users](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#users)and [groups](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#groups) interfaces. Groups in the Authorization and Trust Management service are mapped to role collections. See also [SAP Note 2760424]( https://launchpad.support.sap.com/#/notes/2760424). To enable access to this API, create an OAuth 2.0 client for the XSUAA service instance. To create the client, enable the apiaccess plan. For more information, see [Access UAA Admin APIs](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/ebc9113a520e495ea5fb759b9a7929f2.html).
 */
public class ScimGroupsRoleCollectionsApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the User Management (System for Cross-domain Identity Management (SCIM)).
     *
     * @param httpDestination The destination that API should be used with
     */
    public ScimGroupsRoleCollectionsApi( @Nonnull final Destination httpDestination )
    {
        super(httpDestination);
    }

    /**
     * Instantiates this API class to invoke operations on the User Management (System for Cross-domain Identity Management (SCIM)) based on a given {@link ApiClient}.
     *
     * @param apiClient
     *            ApiClient to invoke the API on
     */
    @Beta
    public ScimGroupsRoleCollectionsApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

    
    /**
     * <p>Returns role collections of the current subaccount.</p>
     *<p>Returns all role collections of the current subaccount. The System for Cross-domain Identity Management (SCIM) interface for groups supplements the relevant UAA [groups](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#groups) interface. Groups in the Authorization and Trust Management service are mapped to role collections.</p>
     * <p><b>200</b> - OK - The API returns the list of role collections.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no role collections could be found that match your request.
     * @param count  (optional)
        Specifies the maximum number of search results per page. The default value is 100. The service returns a maximum of 500 results.
     * @param startIndex  (optional)
        Specifies the index of the first resource in the current set of search results. Default value is 1.
     * @param sortOrder  (optional)
        Specifies the sort order for the query results either ascending or descending for the attribute defined by the sortBy parameter. The default value is ascending.
     * @param sortBy  (optional)
        Enter displayName to sort the returned responses by this attribute. The sorting is not case-sensitive. Only the displayName value is supported. Otherwise resources are sorted by last_modified by default. The parameter sorts the results either ascending or descending as defined by the sortOrder parameter.
     * @return ScimGroups
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public ScimGroups getAllGroupsUsingGET( @Nullable final Integer count,  @Nullable final Integer startIndex,  @Nullable final String sortOrder,  @Nullable final String sortBy) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/Groups").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "count", count));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "sortOrder", sortOrder));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "sortBy", sortBy));
        

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "apiaccess" };

        final ParameterizedTypeReference<ScimGroups> localVarReturnType = new ParameterizedTypeReference<ScimGroups>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Returns role collections of the current subaccount.</p>
     * <p>Returns all role collections of the current subaccount. The System for Cross-domain Identity Management (SCIM) interface for groups supplements the relevant UAA [groups](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#groups) interface. Groups in the Authorization and Trust Management service are mapped to role collections.</p>
     * <p><b>200</b> - OK - The API returns the list of role collections.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no role collections could be found that match your request.
     * @return ScimGroups
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public ScimGroups getAllGroupsUsingGET() throws OpenApiRequestException {
        return getAllGroupsUsingGET(null, null, null, null);
    }
    /**
     * <p>Returns a specific role collection.</p>
     * <p>Returns a role collection specified by the ID. The System for Cross-domain Identity Management (SCIM) interface for groups supplements the relevant UAA [groups](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#groups) interface. Groups in the Authorization and Trust Management service are mapped to role collections.</p>
     * <p><b>200</b> - OK - The API returned the role collection.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no role collections could be found that match your request.
     * @param id
     *      The ID of the role collection.
     * @return ScimGroup
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public ScimGroup getGroupUsingGET( @Nonnull final String id) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new OpenApiRequestException("Missing the required parameter 'id' when calling getGroupUsingGET");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("Id", id);
        final String localVarPath = UriComponentsBuilder.fromPath("/Groups/{Id}").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "apiaccess" };

        final ParameterizedTypeReference<ScimGroup> localVarReturnType = new ParameterizedTypeReference<ScimGroup>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Updates members or description of a role collection.</p>
     *<p>Adds or removes members from an existing role collection specified by the role collection ID. You can also update the description of the role collection. Provide an integer in the If-Match field. The System for Cross-domain Identity Management (SCIM) interface for groups supplements the relevant UAA [groups](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#groups) interface.</p>
     * <p><b>200</b> - OK - The role collection was updated.
     * <p><b>204</b> - No Content - The role collection doesn&#39;t exist.
     * <p><b>400</b> - Bad Request - The request was poorly formed. Possible reasons, a bad If-Match value or poorly for JSON in the body.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * @param id  (required)
        The ID of the role collection.
     * @param patch  (required)
        The content of the role collection to patch. You can only change the description of the role collection and add or remove members.
     * @param ifMatch  (optional)
        Enter an integer. Otherwise, this field has no effect.
     * @return ScimGroupPatch
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nullable
    public ScimGroupPatch patchGroupUsingPATCH( @Nonnull final String id,  @Nonnull final ScimGroupPatch patch,  @Nullable final String ifMatch) throws OpenApiRequestException {
        final Object localVarPostBody = patch;
        
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new OpenApiRequestException("Missing the required parameter 'id' when calling patchGroupUsingPATCH");
        }
        
        // verify the required parameter 'patch' is set
        if (patch == null) {
            throw new OpenApiRequestException("Missing the required parameter 'patch' when calling patchGroupUsingPATCH");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("Id", id);
        final String localVarPath = UriComponentsBuilder.fromPath("/Groups/{Id}").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        if (ifMatch != null)
            localVarHeaderParams.add("If-Match", apiClient.parameterToString(ifMatch));

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "apiaccess" };

        final ParameterizedTypeReference<ScimGroupPatch> localVarReturnType = new ParameterizedTypeReference<ScimGroupPatch>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.PATCH, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Updates members or description of a role collection.</p>
     * <p>Adds or removes members from an existing role collection specified by the role collection ID. You can also update the description of the role collection. Provide an integer in the If-Match field. The System for Cross-domain Identity Management (SCIM) interface for groups supplements the relevant UAA [groups](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#groups) interface.</p>
     * <p><b>200</b> - OK - The role collection was updated.
     * <p><b>204</b> - No Content - The role collection doesn&#39;t exist.
     * <p><b>400</b> - Bad Request - The request was poorly formed. Possible reasons, a bad If-Match value or poorly for JSON in the body.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * @param id
     *      The ID of the role collection.
     * @param patch
     *      The content of the role collection to patch. You can only change the description of the role collection and add or remove members.
     * @return ScimGroupPatch
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nullable
    public ScimGroupPatch patchGroupUsingPATCH( @Nonnull final String id,  @Nonnull final ScimGroupPatch patch) throws OpenApiRequestException {
        return patchGroupUsingPATCH(id, patch, null);
    }

    /**
     * <p>Updates or deletes the members or description of a role collection.</p>
     *<p>Adds or removes the members of an existing role collection specified by the ID. You can also update the description of the role collection. Provide an integer value in the If-Match field. The System for Cross-domain Identity Management (SCIM) interface for groups supplements the relevant UAA [groups](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#groups) interface.</p>
     * <p><b>200</b> - OK - The API updated the role collection.
     * <p><b>201</b> - Created - The API updated the role collection.
     * <p><b>400</b> - Bad Request - The request was poorly formed. Possible reasons, a bad If-Match value or a poorly formed JSON in the body.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no role collection could be found that matches the request.
     * @param id  (required)
        The ID of the role collection.
     * @param group  (required)
        The content of the role collection object. Only the description and member attributes are evaluated. For the member, specify type USER and the ID of the user as the value to identify the user to assign. Use the /Users endpoint to get the ID of the user. Any members not listed in the JSON you submit are removed from the role collection assignment.
     * @param ifMatch  (optional)
        Enter an integer. Otherwise, this field has no effect.
     * @return ScimGroup
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nullable
    public ScimGroup updateGroupUsingPUT( @Nonnull final String id,  @Nonnull final ScimGroup group,  @Nullable final String ifMatch) throws OpenApiRequestException {
        final Object localVarPostBody = group;
        
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new OpenApiRequestException("Missing the required parameter 'id' when calling updateGroupUsingPUT");
        }
        
        // verify the required parameter 'group' is set
        if (group == null) {
            throw new OpenApiRequestException("Missing the required parameter 'group' when calling updateGroupUsingPUT");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("Id", id);
        final String localVarPath = UriComponentsBuilder.fromPath("/Groups/{Id}").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        if (ifMatch != null)
            localVarHeaderParams.add("If-Match", apiClient.parameterToString(ifMatch));

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "apiaccess" };

        final ParameterizedTypeReference<ScimGroup> localVarReturnType = new ParameterizedTypeReference<ScimGroup>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.PUT, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Updates or deletes the members or description of a role collection.</p>
     * <p>Adds or removes the members of an existing role collection specified by the ID. You can also update the description of the role collection. Provide an integer value in the If-Match field. The System for Cross-domain Identity Management (SCIM) interface for groups supplements the relevant UAA [groups](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#groups) interface.</p>
     * <p><b>200</b> - OK - The API updated the role collection.
     * <p><b>201</b> - Created - The API updated the role collection.
     * <p><b>400</b> - Bad Request - The request was poorly formed. Possible reasons, a bad If-Match value or a poorly formed JSON in the body.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no role collection could be found that matches the request.
     * @param id
     *      The ID of the role collection.
     * @param group
     *      The content of the role collection object. Only the description and member attributes are evaluated. For the member, specify type USER and the ID of the user as the value to identify the user to assign. Use the /Users endpoint to get the ID of the user. Any members not listed in the JSON you submit are removed from the role collection assignment.
     * @return ScimGroup
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nullable
    public ScimGroup updateGroupUsingPUT( @Nonnull final String id,  @Nonnull final ScimGroup group) throws OpenApiRequestException {
        return updateGroupUsingPUT(id, group, null);
    }
}
