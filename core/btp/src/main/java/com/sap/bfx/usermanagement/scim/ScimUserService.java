package com.sap.bfx.usermanagement.scim;

import com.sap.bfx.btp.ConnectivityUtils;
import com.sap.bfx.usermanagement.UserService;
import com.sap.bfx.usermanagement.exception.IllegalQueryException;
import com.sap.bfx.usermanagement.exception.MultipleUsersFoundException;
import com.sap.bfx.usermanagement.exception.UserManagementRuntimeException;
import com.sap.bfx.usermanagement.utility.User;
import com.sap.bfx.usermanagement.utility.UserSearchCriteria;
import com.sap.bfx.usermanagement.utility.query.scim.ScimUserMapper;
import com.sap.bfx.usermanagement.utility.query.scim.ScimUserSearchFilterCreator;
import com.sap.openapi.scim.api.ScimUsersShadowUsersApi;
import com.sap.openapi.scim.model.ScimUser;
import com.sap.openapi.scim.model.ScimUsers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ScimUserService implements UserService {

    private final Set<String> sbpaGroups = Arrays.stream(new String[]{"ProcessAutomationAdmin", "ProcessAutomationDelegate", "ProcessAutomationDeveloper", "ProcessAutomationExpert", "ProcessAutomationParticipant"}).collect(Collectors.toSet());
    private final ScimUserSearchFilterCreator scimUserSearchFilterCreator = ScimUserSearchFilterCreator.getInstance();
    private final ScimUserMapper scimUserMapper = ScimUserMapper.getInstance();
    ;

    private static ScimUser getScimUser(String destinationName, String uniqueId) {
        ScimUsersShadowUsersApi scimUsrApi = new ScimUsersShadowUsersApi(ConnectivityUtils.getHttpDestination(destinationName));
        return scimUsrApi.getUserUsingGET(uniqueId);
    }

    private static ScimUser getScimUserByUniqueName(String destinationName, String uniqueName, String originKey) {
        ScimUsersShadowUsersApi scimUsrApi = new ScimUsersShadowUsersApi(ConnectivityUtils.getHttpDestination(destinationName));
        String filter = "userName eq \"" + uniqueName + "\"";
        if (StringUtils.isNotBlank(originKey)) filter += " and origin eq \"" + originKey + "\"";
        ScimUsers scimUsersResponse = scimUsrApi.getAllUsersUsingGET(100, null, null, null, filter);
        List<ScimUser> scimUsers = scimUsersResponse.getResources();
        return asSingleResult(scimUsers);
    }

    public static ScimUser asSingleResult(List<ScimUser> users) {
        if (users == null || users.isEmpty()) {
            return null;
        } else if (users.size() == 1) {
            return users.get(0);
        } else {
            throw new MultipleUsersFoundException(users.get(0).getId(), users.stream().map(ScimUser::getId).collect(Collectors.joining(",")));
        }
    }

    private static <T> T castToOwnClass(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("The object must not be null.");
        }
        // try to cast to own runtime class
        return (T) obj;
    }

    @Override
    public String getUniqueNameOfUser(String destinationName, String uniqueId) {
        try {
            ScimUser scimUser = getScimUser(destinationName, uniqueId);
            return scimUser.getUserName();
        } catch (Exception e) {
            log.error("getUniqueNameOfUser(): Error occurred for {}", uniqueId, e);
        }
        return null;
    }

    @Override
    public String getUniqueIdOfUser(String destinationName, String uniqueName, String originKey) {
        User user = getUserOrNull(destinationName, uniqueName, originKey);
        if (null != user) return user.getId();
        return null;
    }

    @Override
    public User getUserOrNull(String destinationName, String uniqueName, String originKey) {
        if (StringUtils.isNotBlank(destinationName) && StringUtils.isNotBlank(uniqueName)) {
            ScimUser scimUser = getScimUserByUniqueName(destinationName, uniqueName, originKey);
            if (null != scimUser) return ScimUserMapper.getInstance().mapToUser(scimUser);
        }
        return null;
    }

    @Override
    public Map<String, User> findUsersMapByUniqueNames(String destinationName, List<String> uniqueNames, String originKey) {
        Map<String, User> users = new HashMap<String, User>();

        // userName eq "oliver.breithaupt@sap.com" or userName eq "kai.schapeit@sap.com"
        try {
            ScimUsersShadowUsersApi scimUsrApi = new ScimUsersShadowUsersApi(ConnectivityUtils.getHttpDestination(destinationName));
            String filter = scimUserSearchFilterCreator.toFilterString(uniqueNames);
            if (StringUtils.isNotBlank(originKey)) filter = "(" + filter + ") and origin eq \"" + originKey + "\"";
            ScimUsers scimUsersResponse = scimUsrApi.getAllUsersUsingGET(100, null, null, null, filter);
            List<ScimUser> scimUsers = scimUsersResponse.getResources();
            for (ScimUser scimUser : scimUsers) {
                users.put(scimUser.getUserName(), ScimUserMapper.getInstance().mapToUser(scimUser));
            }
        } catch (IllegalQueryException e) {
            log.error("findUsersMapByUniqueNames(): Error occurred", e);
        }
        return users;
    }

    @Override
    public List<User> findUsersListByUniqueNames(String destinationName, List<String> uniqueNames, String originKey) {
        Map<String, User> tempUsers = findUsersMapByUniqueNames(destinationName, uniqueNames, originKey);
        return new ArrayList<User>(tempUsers.values());
    }

    @Override
    public Boolean isSbpaUser(String destinationName, String uniqueName, String originKey) {
        User tempUser = getUserOrNull(destinationName, uniqueName, originKey);
        if (null != tempUser) return CollectionUtils.containsAny(tempUser.getGroups(), sbpaGroups);
        return false;
    }

    @Override
    public Boolean isSbpaUserByUniqueId(String destinationName, String uniqueId) {
        User tempUser = getUserByUniqueId(destinationName, uniqueId);
        if (null != tempUser) return CollectionUtils.containsAny(tempUser.getGroups(), sbpaGroups);
        return false;
    }

    @Override
    public List<User> findByCriteria(String destinationName, UserSearchCriteria userSearchCriteria) {
        List<User> users;
        try {
            String filter = scimUserSearchFilterCreator.toFilterString(userSearchCriteria);
            ScimUsersShadowUsersApi scimUsrApi = new ScimUsersShadowUsersApi(ConnectivityUtils.getHttpDestination(destinationName));
            List<ScimUser> scimUsers = new ArrayList<ScimUser>();
            int maxResults = Long.valueOf(userSearchCriteria.getMaxResults()).intValue();
            if (0 < maxResults && 500 > maxResults) {
                ScimUsers scimUsersResponse = scimUsrApi.getAllUsersUsingGET(maxResults, null, null, null, filter);
                scimUsers.addAll(scimUsersResponse.getResources());
            } else if (500 < maxResults) {
                int count = 0;
                int startIndex = 1;
                int totalResults = -1;
                while (count < maxResults && totalResults < count) {
                    ScimUsers scimUsersResponse = scimUsrApi.getAllUsersUsingGET(500, startIndex, null, null, filter);
                    scimUsers.addAll(scimUsersResponse.getResources());
                    count += scimUsersResponse.getResources().size();
                    startIndex += scimUsersResponse.getItemsPerPage();
                    if (-1 == totalResults) totalResults = scimUsersResponse.getTotalResults();
                }
            } else {
                int count = 0;
                int startIndex = 1;
                int totalResults = -1;
                while (totalResults < count) {
                    ScimUsers scimUsersResponse = scimUsrApi.getAllUsersUsingGET(500, startIndex, null, null, filter);
                    scimUsers.addAll(scimUsersResponse.getResources());
                    count += scimUsersResponse.getResources().size();
                    startIndex += scimUsersResponse.getItemsPerPage();
                    if (-1 == totalResults) totalResults = scimUsersResponse.getTotalResults();
                }
            }
            users = new ArrayList<User>(scimUsers.stream().map(scimUserMapper::mapToUser).toList());
        } catch (IllegalQueryException e) {
            throw new UserManagementRuntimeException("Illegal query, should never happen here...", e);
        }
        return users;
    }

    @Override
    public Map<Pair<String, String>, String> getUserAttributeByUniqueNameAndAttributeNames(String destinationName, String uniqueName, String originKey, Set<Pair<String, String>> attributePairs) {
        Map<Pair<String, String>, String> result = new HashMap<Pair<String, String>, String>();
        ScimUser scimUser = getScimUserByUniqueName(destinationName, uniqueName, originKey);
        if (null != scimUser) {
            try {
                for (Pair<String, String> aAttributePair : attributePairs) {
                    Object customAttribute = scimUser.getCustomField(aAttributePair.getLeft().concat(aAttributePair.getRight()));
                    if (null != customAttribute) {
                        if (customAttribute instanceof String) {
                            result.put(aAttributePair, (String) customAttribute);
                        } else {
                            result.put(aAttributePair, castToOwnClass(customAttribute).toString());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("getUserAttributeByUniqueNameAndAttributeNames(): Error occurred", e);
            }
        }
        return result;
    }

    @Override
    public Map<Pair<String, String>, String> getUserAttributeByUniqueNameAndAttributeNames(String destinationName, String uniqueName, String originKey, List<String> attributePrefixes, List<String> attributeNames) {
        Set<Pair<String, String>> attributePairs = new HashSet<Pair<String, String>>();
        for (int i = 0; i < attributePrefixes.size(); i++) {
            attributePairs.add(new ImmutablePair<String, String>(attributePrefixes.get(i), attributeNames.get(i)));
        }
        return getUserAttributeByUniqueNameAndAttributeNames(destinationName, uniqueName, originKey, attributePairs);
    }

    @Override
    public User getUserByUniqueId(String destinationName, String uniqueId) {
        try {
            ScimUser scimUser = getScimUser(destinationName, uniqueId);
            return scimUserMapper.mapToUser(scimUser);
        } catch (Exception e) {
            log.error("getUserByUniqueId(): Error occurred for {}", uniqueId, e);
        }
        return null;
    }

    @Override
    public User getUserOrNullByUniqueId(String destinationName, String uniqueId, Boolean logException) {
        try {
            ScimUser scimUser = getScimUser(destinationName, uniqueId);
            return scimUserMapper.mapToUser(scimUser);
        } catch (Exception e) {
            if (logException) {
                log.error("getUserOrNullByUniqueId(): Error occurred for {}", uniqueId, e);
            }
        }
        return null;
    }
}
