package com.sap.bfx.usermanagement.ldap;

import com.sap.bfx.usermanagement.exception.UserManagementServiceException;
import com.sap.bfx.usermanagement.utility.User;
import com.sap.bfx.usermanagement.utility.UserSearchCriteria;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class LdapUserService implements LdapUserServiceLocal {

    private static final String NO_USER_ERROR = "getUniqueNameOfUserByIdAndRootPath(): User not found ";
    private static final String ID_OF_USER_ERROR = "getIdOfUserByUniqueNameAndRootPath(): Error occured for ";
    private static final String FULLNAME_OF_USER1_ERROR = "determineFullnameOfUserByUniqueNameAndRootPath(): Error occured for ";
    private static final String FULLNAME_OF_USER2_ERROR = "determineFullnameOfUserByIdAndRootPath(): Error occured for ";
    private static final String UNIQUENAME_OF_USER_ERROR = "getUniqueNameOfUser(): Error occured for ";
    private static final String DISPLAYNAME_OF_USER_ERROR = "getDisplayNameOfUserAndRootPath(): Error occured for ";
    private static final String UNIQUEID_OF_USER_ERROR = "getUniqueIdOfUser(): Error occured for ";
    private static final String USER_OR_NULL_ERROR = "getUserOrNull(): Error occured for ";
    private final Set<String> sbpaGroups = Arrays.stream(new String[]{"ProcessAutomationAdmin", "ProcessAutomationDelegate", "ProcessAutomationDeveloper", "ProcessAutomationExpert", "ProcessAutomationParticipant"}).collect(Collectors.toSet());
    private final LdapUserExecutor ldapUserExecutor = LdapUserExecutor.getInstance();
    private final LdapBulkExecutor ldapBulkExecutor = LdapBulkExecutor.getInstance();
    private final LdapQueryExecutor ldapQueryExecutor = LdapQueryExecutor.getInstance();

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
            User user = ldapUserExecutor.findById(uniqueId);
            if (null != user) return user.getUserName();
        } catch (Exception e) {
            log.error(UNIQUENAME_OF_USER_ERROR + uniqueId, e);
        }
        return null;
    }

    @Override
    public String getUniqueIdOfUser(String destinationName, String uniqueName, String originKey) {
        try {
            User user = ldapUserExecutor.findByUserName(uniqueName);
            if (null != user) return user.getId();
        } catch (Exception e) {
            log.error(UNIQUEID_OF_USER_ERROR + uniqueName, e);
        }
        return null;
    }

    @Override
    public User getUserOrNull(String destinationName, String uniqueName, String originKey) {
        if (StringUtils.isNotBlank(uniqueName)) {
            try {
                return ldapUserExecutor.findByUserName(uniqueName);
            } catch (Exception e) {
                log.error(USER_OR_NULL_ERROR + uniqueName, e);
            }
        }
        return null;
    }

    @Override
    public Map<String, User> findUsersMapByUniqueNames(String destinationName, List<String> uniqueNames, String originKey) {
        Map<String, User> users = new HashMap<String, User>();
        for (String aUniqueName : uniqueNames) {
            try {
                if (StringUtils.isNotBlank(aUniqueName)) {
                    User ldapUser = ldapUserExecutor.findByUserName(aUniqueName);
                    if (null != ldapUser) users.put(aUniqueName, ldapUser);
                }
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return users;
    }

    @Override
    public List<User> findUsersListByUniqueNames(String destinationName, List<String> uniqueNames, String originKey) {
        List<User> users = new ArrayList<User>();
        Map<String, User> tempUsers = findUsersMapByUniqueNames(destinationName, uniqueNames, originKey);
        users.addAll(tempUsers.values());
        return users;
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
        return ldapUserExecutor.findByCriteria(userSearchCriteria, false);
    }

    @Override
    public Map<Pair<String, String>, String> getUserAttributeByUniqueNameAndAttributeNames(String destinationName, String uniqueName, String originKey, Set<Pair<String, String>> attributePairs) {
        Map<Pair<String, String>, String> result = new HashMap<Pair<String, String>, String>();
        User user = getUserOrNull(destinationName, uniqueName, originKey);
        if (null != user) {
            try {
                for (Pair<String, String> aAttributePair : attributePairs) {
                    //TODO clear where attribute fields are stored
                    Object customAttribute = "";//user.getCustomField(aAttributePair.getLeft().concat(aAttributePair.getRight()));
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
            User user = ldapUserExecutor.findById(uniqueId);
            if (null != user) return user;
        } catch (Exception e) {
            log.error("getUserByUniqueId(): Error occurred for {}", uniqueId, e);
        }
        return null;
    }

    @Override
    public User getUserOrNullByUniqueId(String destinationName, String uniqueId, Boolean logException) {
        try {
            User user = ldapUserExecutor.findById(uniqueId);
            if (null != user) return user;
        } catch (Exception e) {
            if (logException) {
                log.error("getUserOrNullByUniqueId(): Error occurred for {}", uniqueId, e);
            }
        }
        return null;
    }

    //TODO ab hier zugriffe auf objekte im return prüfen

    @Override
    public User getUserById(String id) {
        return ldapUserExecutor.findById(id);
    }

    @Override
    public User getUserByUniqueName(String uniqueName) {
        return ldapUserExecutor.findByUserName(uniqueName);
    }

    @Override
    public String getPathBySearchBaseUser(User searchBaseUser) {
        return ldapQueryExecutor.getPathBySearchBaseUser(searchBaseUser);
    }

    @Override
    public String getPathBySearchBaseBusinessSystem(String searchBaseBusinessSystem) {
        return ldapQueryExecutor.getPathBySearchBaseBusinessSystem(searchBaseBusinessSystem);
    }

    @Override
    public Map<String, String> getFullnameOfUsersByIdsAndRootPath(List<String> ids, String rootPath) throws UserManagementServiceException {
        Map<String, User> userMap = ldapBulkExecutor.findByIdsAndRootPath(ids, rootPath);
        return this.mapToFullnameMap(userMap);
    }

    private Map<String, String> mapToFullnameMap(Map<String, User> sourceMap) {
        Map<String, String> targetMap = new HashMap<String, String>();
        Set<String> sourceKeys = sourceMap.keySet();
        for (String aKey : sourceKeys) {
            // key of targetMap is id, value of targetMap is full name
            targetMap.put(aKey, this.buildFullname(sourceMap.get(aKey)));
        }
        return targetMap;
    }

    private String buildFullname(User user) {
        return user.getFullName() + " (" + user.getUserName() + ")";
    }

    @Override
    public Map<String, String> getFullnameOfUsersByIdsAndRootPath(Set<String> ids, String rootPath) throws UserManagementServiceException {
        Map<String, User> userMap = ldapBulkExecutor.findByIdsAndRootPath(ids, rootPath);
        return this.mapToFullnameMap(userMap);
    }

    @Override
    public String getUniqueNameOfUserByIdAndRootPath(String id, String rootPath) {
        if (id == null) {
            return null;
        }
        try {
            User user = ldapUserExecutor.findByIdAndRootPath(id, rootPath);
            if (null != user) {
                return user.getUserName();
            } else {
                String error = NO_USER_ERROR + id;
                log.error(error);
            }
        } catch (Exception e) {
            String error = NO_USER_ERROR + id;
            log.error(error, e);
        }
        return null;
    }

    @Override
    public Map<String, User> getUsersByIdsAndRootPath(List<String> ids, String rootPath) throws UserManagementServiceException {
        return ldapBulkExecutor.findByIdsAndRootPath(ids, rootPath);
    }

    @Override
    public Map<String, User> getUsersByIdsAndRootPath(Set<String> ids, String rootPath) throws UserManagementServiceException {
        return ldapBulkExecutor.findByIdsAndRootPath(ids, rootPath);
    }

    @Override
    public String getIdOfUserByUniqueNameAndRootPath(String uniqueName, String rootPath) {
        try {
            User user = ldapUserExecutor.findByUserNameAndRootPath(uniqueName, rootPath);
            return user.getId();
        } catch (Exception e) {
            String error = ID_OF_USER_ERROR + uniqueName;
            log.error(error, e);
        }
        return null;
    }

    @Override
    public Map<String, String> getIdOfUsersByUniqueNamesAndRootPath(List<String> uniqueNames, String rootPath) throws UserManagementServiceException {
        Map<String, User> userMap = ldapBulkExecutor.findByUniqueNamesAndRootPath(uniqueNames, rootPath);
        return this.mapToIdMap(userMap);
    }

    private Map<String, String> mapToIdMap(Map<String, User> sourceMap) {
        HashMap<String, String> targetMap = new HashMap<String, String>();
        Set<String> sourceKeys = sourceMap.keySet();
        for (String aKey : sourceKeys) {
            // key of targetMap is uniquename, value of targetMap is
            // id
            targetMap.put(aKey, sourceMap.get(aKey).getId());
        }
        return targetMap;
    }

    @Override
    public Map<String, String> getIdOfUsersByUniqueNamesAndRootPath(Set<String> uniqueNames, String rootPath) throws UserManagementServiceException {
        Map<String, User> userMap = ldapBulkExecutor.findByUniqueNamesAndRootPath(uniqueNames, rootPath);
        return this.mapToIdMap(userMap);
    }

    @Override
    public Map<String, User> getUsersByUniqueNamesAndRootPath(List<String> uniqueNames, String rootPath) throws UserManagementServiceException {
        return ldapBulkExecutor.findByUniqueNamesAndRootPath(uniqueNames, rootPath);
    }

    @Override
    public Map<String, User> getUsersByUniqueNamesAndRootPath(Set<String> uniqueNames, String rootPath) throws UserManagementServiceException {
        return ldapBulkExecutor.findByUniqueNamesAndRootPath(uniqueNames, rootPath);
    }

    @Override
    public String determineFullnameOfUserByUniqueNameAndRootPath(String uniqueName, String rootPath) {
        try {
            User user = ldapUserExecutor.findByUserNameAndRootPath(uniqueName, rootPath);
            return this.buildFullname(user);
        } catch (Exception e) {
            log.error(FULLNAME_OF_USER1_ERROR + "{}", uniqueName, e);
        }
        return null;
    }

    @Override
    public String determineFullnameOfUserByIdAndRootPath(String id, String rootPath) {
        try {
            User user = ldapUserExecutor.findByIdAndRootPath(id, rootPath);
            return this.buildFullname(user);
        } catch (Exception e) {
            log.error(FULLNAME_OF_USER2_ERROR + "{}", id, e);
        }
        return null;
    }

    @Override
    public String getDisplayNameOfUserAndRootPath(String uniqueName, String rootPath) {
        try {
            User user = ldapUserExecutor.findByUserNameAndRootPath(uniqueName, rootPath);
            return user.getDisplayName();
        } catch (Exception e) {
            log.error(DISPLAYNAME_OF_USER_ERROR + "{}", uniqueName, e);
        }
        return null;
    }

    @Override
    public String getFullNameOfUserAndRootPath(String uniqueName, String rootPath) {
        try {
            User user = ldapUserExecutor.findByUserNameAndRootPath(uniqueName, rootPath);
            return user.getFullName();
        } catch (Exception e) {
            log.error("getFullNameOfUserAndRootPath(): Error occured for {}", uniqueName, e);
        }
        return null;
    }

    @Override
    public List<User> findByCriteriaAndRootPath(UserSearchCriteria userSearchCriteria, String rootPath) {
        return ldapUserExecutor.findByCriteriaAndRootPath(userSearchCriteria, rootPath);
    }

    @Override
    public List<User> findByCriteriaAndRootPaths(UserSearchCriteria userSearchCriteria, List<String> rootPaths) {
        return ldapUserExecutor.findByCriteriaAndRootPaths(userSearchCriteria, rootPaths);
    }

    @Override
    public User getUserByIdAndRootPath(String id, String rootPath) {
        return ldapUserExecutor.findByIdAndRootPath(id, rootPath);
    }

    @Override
    public User getUserByUniqueNameAndRootPath(String uniqueName, String rootPath) {
        return ldapUserExecutor.findByUserNameAndRootPath(uniqueName, rootPath);
    }
}
