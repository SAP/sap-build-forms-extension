

package com.sap.openapi.scim.api;

import com.sap.cloud.sdk.services.openapi.core.OpenApiRequestException;
import com.sap.cloud.sdk.services.openapi.core.OpenApiResponse;
import com.sap.cloud.sdk.services.openapi.core.AbstractOpenApiService;
import com.sap.cloud.sdk.services.openapi.apiclient.ApiClient;

import com.sap.openapi.scim.model.AuthorizationError;
import com.sap.openapi.scim.model.GenericError;
import com.sap.openapi.scim.model.MethodError;
import com.sap.openapi.scim.model.ScimUser;
import com.sap.openapi.scim.model.ScimUserPOSTPUT;
import com.sap.openapi.scim.model.ScimUsers;

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
public class ScimUsersShadowUsersApi extends AbstractOpenApiService {
    /**
     * Instantiates this API class to invoke operations on the User Management (System for Cross-domain Identity Management (SCIM)).
     *
     * @param httpDestination The destination that API should be used with
     */
    public ScimUsersShadowUsersApi( @Nonnull final Destination httpDestination )
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
    public ScimUsersShadowUsersApi( @Nonnull final ApiClient apiClient )
    {
         super(apiClient);
    }

        /**
     * <p>Creates a user.</p>
     * <p>Creates a user with the properties specified in the body. **To create a group membership (assign role collections), use the endpoint [/Groups/{Id}/members](https://docs.cloudfoundry.org/api/uaa/version/74.4.0/index.html#add-member)**.</p>
     * <p><b>201</b> - Created - The API created the user.
     * <p><b>400</b> - Bad Request - The request was poorly formed.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>409</b> - Conflict - The SCIM resource already exists.
     * @param user
     *      user
     * @return ScimUserPOSTPUT
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public ScimUserPOSTPUT createUserUsingPOST( @Nonnull final ScimUserPOSTPUT user) throws OpenApiRequestException {
        final Object localVarPostBody = user;
        
        // verify the required parameter 'user' is set
        if (user == null) {
            throw new OpenApiRequestException("Missing the required parameter 'user' when calling createUserUsingPOST");
        }
        
        final String localVarPath = UriComponentsBuilder.fromPath("/Users").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { 
            "application/json"
        };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "apiaccess" };

        final ParameterizedTypeReference<ScimUserPOSTPUT> localVarReturnType = new ParameterizedTypeReference<ScimUserPOSTPUT>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.POST, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Deletes a specific user.</p>
     *<p>Deletes a user specified by the user ID. By default, the system creates shadow users during authentication. You can disable automatic creation of shadow users. When you use a system to provision identities, we recommend that you switch off automatic creation of shadow users for all identity providers. &lt;br/&gt;For more information, see [Switch Off Automatic Creation of Shadow Users](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/d8525671e8b14147b96ef497e1e1af80.html). &lt;br/&gt;If automatic creation is switched off, a user can&#39;t log in until the administrator creates a shadow user for the user. Data privacy regulations or policies may require you to delete shadow users, for example, when the user has left your organization. The System for Cross-domain Identity Management (SCIM) interface for users supplements the relevant UAA [users](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#users) interface.</p>
     * <p><b>200</b> - OK - The API deleted the user.
     * <p><b>204</b> - No Content - The user doesn&#39;t exist.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no users could be found that match your request.
     * <p><b>405</b> - Method Not Allowed - You must provide a user ID in your request.
     * @param id  (required)
        The ID of the user.
     * @param ifMatch  (optional)
        Enter the current version of the user as a whole number.
     * @return ScimUser
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nullable
    public ScimUser deleteUserUsingDELETE( @Nonnull final String id,  @Nullable final String ifMatch) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new OpenApiRequestException("Missing the required parameter 'id' when calling deleteUserUsingDELETE");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("Id", id);
        final String localVarPath = UriComponentsBuilder.fromPath("/Users/{Id}").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        if (ifMatch != null)
            localVarHeaderParams.add("If-Match", apiClient.parameterToString(ifMatch));

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "apiaccess" };

        final ParameterizedTypeReference<ScimUser> localVarReturnType = new ParameterizedTypeReference<ScimUser>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.DELETE, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Deletes a specific user.</p>
     * <p>Deletes a user specified by the user ID. By default, the system creates shadow users during authentication. You can disable automatic creation of shadow users. When you use a system to provision identities, we recommend that you switch off automatic creation of shadow users for all identity providers. &lt;br/&gt;For more information, see [Switch Off Automatic Creation of Shadow Users](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/d8525671e8b14147b96ef497e1e1af80.html). &lt;br/&gt;If automatic creation is switched off, a user can&#39;t log in until the administrator creates a shadow user for the user. Data privacy regulations or policies may require you to delete shadow users, for example, when the user has left your organization. The System for Cross-domain Identity Management (SCIM) interface for users supplements the relevant UAA [users](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#users) interface.</p>
     * <p><b>200</b> - OK - The API deleted the user.
     * <p><b>204</b> - No Content - The user doesn&#39;t exist.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no users could be found that match your request.
     * <p><b>405</b> - Method Not Allowed - You must provide a user ID in your request.
     * @param id
     *      The ID of the user.
     * @return ScimUser
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nullable
    public ScimUser deleteUserUsingDELETE( @Nonnull final String id) throws OpenApiRequestException {
        return deleteUserUsingDELETE(id, null);
    }

    /**
     * <p>Returns users of the current subaccount.</p>
     *<p>Returns all shadow users of the current subaccount. By default, the system creates shadow users during authentication. You can disable automatic creation of shadow users. When you use a system to provision identities, we recommend that you switch off automatic creation of shadow users for all identity providers. &lt;br/&gt;For more information, see [Switch Off Automatic Creation of Shadow Users](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/d8525671e8b14147b96ef497e1e1af80.html). &lt;br/&gt;The System for Cross-domain Identity Management (SCIM) interface for users supplements the relevant UAA [users](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#users) interface.</p>
     * <p><b>200</b> - OK - The API returns the list of users.
     * <p><b>400</b> - Bad Request - The request was poorly formed.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no users could be found that match your request.
     * @param count  (optional)
        Specifies the maximum number of search results per page. The default value is 100. The service returns a maximum of 500 results.
     * @param startIndex  (optional)
        Specifies the index of the first response page in the current set of search results.
     * @param sortOrder  (optional)
        Specifies the sort order for the query results either ascending or descending for the attribute defined by the sortBy parameter. The default value is ascending.
     * @param sortBy  (optional)
        Specifies the attribute to sort the returned responses. Sorts the results either ascending or descending as defined by the sortOrder parameter.
     * @param filter  (optional)
        Specifies a filter query applied to an attribute of a user. If the filter query is true, then the user is included in the returned response. Each filter query includes an attribute, an operator, and a value. Supported operators are eq (equal), co (contains). sw (starts with), pr (present or has value), gt (greater than), ge (greater than or equal), lt (less than), and le (less than or euqal).
     * @return ScimUsers
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public ScimUsers getAllUsersUsingGET( @Nullable final Integer count,  @Nullable final Integer startIndex,  @Nullable final String sortOrder,  @Nullable final String sortBy,  @Nullable final String filter) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        final String localVarPath = UriComponentsBuilder.fromPath("/Users").build().toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "count", count));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "startIndex", startIndex));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "sortOrder", sortOrder));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "sortBy", sortBy));
                localVarQueryParams.putAll(apiClient.parameterToMultiValueMap(null, "filter", filter));
        

        final String[] localVarAccepts = { 
            "application/json"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "apiaccess" };

        final ParameterizedTypeReference<ScimUsers> localVarReturnType = new ParameterizedTypeReference<ScimUsers>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Returns users of the current subaccount.</p>
     * <p>Returns all shadow users of the current subaccount. By default, the system creates shadow users during authentication. You can disable automatic creation of shadow users. When you use a system to provision identities, we recommend that you switch off automatic creation of shadow users for all identity providers. &lt;br/&gt;For more information, see [Switch Off Automatic Creation of Shadow Users](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/d8525671e8b14147b96ef497e1e1af80.html). &lt;br/&gt;The System for Cross-domain Identity Management (SCIM) interface for users supplements the relevant UAA [users](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#users) interface.</p>
     * <p><b>200</b> - OK - The API returns the list of users.
     * <p><b>400</b> - Bad Request - The request was poorly formed.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no users could be found that match your request.
     * @return ScimUsers
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public ScimUsers getAllUsersUsingGET() throws OpenApiRequestException {
        return getAllUsersUsingGET(null, null, null, null, null);
    }
    /**
     * <p>Returns a specific user.</p>
     * <p>Returns a user specified by the user ID (The ID of the user). By default, the system creates shadow users during authentication. You can disable automatic creation of shadow users. When you use a system to provision identities, we recommend that you switch off automatic creation of shadow users for all identity providers. &lt;br/&gt;For more information, see [Switch Off Automatic Creation of Shadow Users](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/d8525671e8b14147b96ef497e1e1af80.html). &lt;br/&gt;The System for Cross-domain Identity Management (SCIM) interface for users supplements the relevant UAA [users](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#users) interface.</p>
     * <p><b>200</b> - OK - The API returned the specified user.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no users could be found that match your request.
     * @param id
     *      The ID of the user.
     * @return ScimUser
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nonnull
    public ScimUser getUserUsingGET( @Nonnull final String id) throws OpenApiRequestException {
        final Object localVarPostBody = null;
        
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new OpenApiRequestException("Missing the required parameter 'id' when calling getUserUsingGET");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("Id", id);
        final String localVarPath = UriComponentsBuilder.fromPath("/Users/{Id}").buildAndExpand(localVarPathParams).toUriString();

        final MultiValueMap<String, String> localVarQueryParams = new LinkedMultiValueMap<String, String>();
        final HttpHeaders localVarHeaderParams = new HttpHeaders();
        final MultiValueMap<String, Object> localVarFormParams = new LinkedMultiValueMap<String, Object>();

        final String[] localVarAccepts = { 
            "*/*"
        };
        final List<MediaType> localVarAccept = apiClient.selectHeaderAccept(localVarAccepts);
        final String[] localVarContentTypes = { };
        final MediaType localVarContentType = apiClient.selectHeaderContentType(localVarContentTypes);

        final String[] localVarAuthNames = new String[] { "apiaccess" };

        final ParameterizedTypeReference<ScimUser> localVarReturnType = new ParameterizedTypeReference<ScimUser>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.GET, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Updates specific fields of a user.</p>
     *<p>Updates only the specified fields of a user specified by the user ID. **To update a group membership (assign role collections), use the endpoint [/Groups/{Id}/members](https://docs.cloudfoundry.org/api/uaa/version/74.4.0/index.html#add-member)**. By default, the system creates shadow users during authentication. You can disable automatic creation of shadow users. When you use a system to provision identities, we recommend that you switch off automatic creation of shadow users for all identity providers. &lt;br/&gt;For more information, see [Switch Off Automatic Creation of Shadow Users](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/d8525671e8b14147b96ef497e1e1af80.html). &lt;br/&gt;The System for Cross-domain Identity Management (SCIM) interface for users supplements the relevant UAA [users](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#users) interface.</p>
     * <p><b>200</b> - OK - The API updated the user.
     * <p><b>204</b> - No Content - The user doesn&#39;t exist.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no users could be found that match your request.
     * <p><b>409</b> - Conflict - You attempted to update the wrong version of the object. Check the value of If-Match.
     * @param id  (required)
        The id parameter of the user.
     * @param patch  (required)
        The content of the user to patch.
     * @param ifMatch  (optional, default to NaN)
        Enter the current version of the user as a whole number.
     * @return ScimUser
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nullable
    public ScimUser patchUserUsingPATCH( @Nonnull final String id,  @Nonnull final ScimUserPOSTPUT patch,  @Nullable final String ifMatch) throws OpenApiRequestException {
        final Object localVarPostBody = patch;
        
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new OpenApiRequestException("Missing the required parameter 'id' when calling patchUserUsingPATCH");
        }
        
        // verify the required parameter 'patch' is set
        if (patch == null) {
            throw new OpenApiRequestException("Missing the required parameter 'patch' when calling patchUserUsingPATCH");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("Id", id);
        final String localVarPath = UriComponentsBuilder.fromPath("/Users/{Id}").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<ScimUser> localVarReturnType = new ParameterizedTypeReference<ScimUser>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.PATCH, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Updates specific fields of a user.</p>
     * <p>Updates only the specified fields of a user specified by the user ID. **To update a group membership (assign role collections), use the endpoint [/Groups/{Id}/members](https://docs.cloudfoundry.org/api/uaa/version/74.4.0/index.html#add-member)**. By default, the system creates shadow users during authentication. You can disable automatic creation of shadow users. When you use a system to provision identities, we recommend that you switch off automatic creation of shadow users for all identity providers. &lt;br/&gt;For more information, see [Switch Off Automatic Creation of Shadow Users](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/d8525671e8b14147b96ef497e1e1af80.html). &lt;br/&gt;The System for Cross-domain Identity Management (SCIM) interface for users supplements the relevant UAA [users](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#users) interface.</p>
     * <p><b>200</b> - OK - The API updated the user.
     * <p><b>204</b> - No Content - The user doesn&#39;t exist.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no users could be found that match your request.
     * <p><b>409</b> - Conflict - You attempted to update the wrong version of the object. Check the value of If-Match.
     * @param id
     *      The id parameter of the user.
     * @param patch
     *      The content of the user to patch.
     * @return ScimUser
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nullable
    public ScimUser patchUserUsingPATCH( @Nonnull final String id,  @Nonnull final ScimUserPOSTPUT patch) throws OpenApiRequestException {
        return patchUserUsingPATCH(id, patch, null);
    }

    /**
     * <p>Updates the entire user.</p>
     *<p>Updates all fields of a user specified by the user ID. **To update a group membership (assign role collections), use the endpoint [/Groups/{Id}/members](https://docs.cloudfoundry.org/api/uaa/version/74.4.0/index.html#add-member)**. By default, the system creates shadow users during authentication. You can disable automatic creation of shadow users. When you use a system to provision identities, we recommend that you switch off automatic creation of shadow users for all identity providers. &lt;br/&gt;For more information, see [Switch Off Automatic Creation of Shadow Users](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/d8525671e8b14147b96ef497e1e1af80.html). &lt;br/&gt;If automatic creation is switched off, a user can&#39;t log in until the administrator creates a shadow user for the user with the PUT method. The System for Cross-domain Identity Management (SCIM) interface for users supplements the relevant UAA [users](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#users) interface.</p>
     * <p><b>200</b> - OK - The API updated the user.
     * <p><b>201</b> - Created - The API updated the user.
     * <p><b>400</b> - Bad Request - The request was poorly formed. Possible reasons, the wrong ID was submitted in the URL.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no users could be found that match your request.
     * <p><b>409</b> - Conflict - You attempted to update the wrong version of the object. Check the value of If-Match.
     * @param id  (required)
        The ID of the user.
     * @param user  (required)
        The content of the user.
     * @param ifMatch  (optional, default to NaN)
        Enter the current version of the user as a whole number.
     * @return ScimUserPOSTPUT
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nullable
    public ScimUserPOSTPUT updateUserUsingPUT( @Nonnull final String id,  @Nonnull final ScimUserPOSTPUT user,  @Nullable final String ifMatch) throws OpenApiRequestException {
        final Object localVarPostBody = user;
        
        // verify the required parameter 'id' is set
        if (id == null) {
            throw new OpenApiRequestException("Missing the required parameter 'id' when calling updateUserUsingPUT");
        }
        
        // verify the required parameter 'user' is set
        if (user == null) {
            throw new OpenApiRequestException("Missing the required parameter 'user' when calling updateUserUsingPUT");
        }
        
        // create path and map variables
        final Map<String, Object> localVarPathParams = new HashMap<String, Object>();
        localVarPathParams.put("Id", id);
        final String localVarPath = UriComponentsBuilder.fromPath("/Users/{Id}").buildAndExpand(localVarPathParams).toUriString();

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

        final ParameterizedTypeReference<ScimUserPOSTPUT> localVarReturnType = new ParameterizedTypeReference<ScimUserPOSTPUT>() {};
        return apiClient.invokeAPI(localVarPath, HttpMethod.PUT, localVarQueryParams, localVarPostBody, localVarHeaderParams, localVarFormParams, localVarAccept, localVarContentType, localVarAuthNames, localVarReturnType);
    }

    /**
     * <p>Updates the entire user.</p>
     * <p>Updates all fields of a user specified by the user ID. **To update a group membership (assign role collections), use the endpoint [/Groups/{Id}/members](https://docs.cloudfoundry.org/api/uaa/version/74.4.0/index.html#add-member)**. By default, the system creates shadow users during authentication. You can disable automatic creation of shadow users. When you use a system to provision identities, we recommend that you switch off automatic creation of shadow users for all identity providers. &lt;br/&gt;For more information, see [Switch Off Automatic Creation of Shadow Users](https://help.sap.com/docs/BTP/65de2977205c403bbc107264b8eccf4b/d8525671e8b14147b96ef497e1e1af80.html). &lt;br/&gt;If automatic creation is switched off, a user can&#39;t log in until the administrator creates a shadow user for the user with the PUT method. The System for Cross-domain Identity Management (SCIM) interface for users supplements the relevant UAA [users](https://docs.cloudfoundry.org/api/uaa/version/74.0.0/index.html#users) interface.</p>
     * <p><b>200</b> - OK - The API updated the user.
     * <p><b>201</b> - Created - The API updated the user.
     * <p><b>400</b> - Bad Request - The request was poorly formed. Possible reasons, the wrong ID was submitted in the URL.
     * <p><b>401</b> - Unauthorized - Access denied. Your authentication credentials have been refused.
     * <p><b>403</b> - Forbidden - Access denied. You don&#39;t have the authorizations required to access the resource.
     * <p><b>404</b> - Not Found - Possible reasons, no users could be found that match your request.
     * <p><b>409</b> - Conflict - You attempted to update the wrong version of the object. Check the value of If-Match.
     * @param id
     *      The ID of the user.
     * @param user
     *      The content of the user.
     * @return ScimUserPOSTPUT
     * @throws OpenApiRequestException if an error occurs while attempting to invoke the API
     */
    @Nullable
    public ScimUserPOSTPUT updateUserUsingPUT( @Nonnull final String id,  @Nonnull final ScimUserPOSTPUT user) throws OpenApiRequestException {
        return updateUserUsingPUT(id, user, null);
    }
}
