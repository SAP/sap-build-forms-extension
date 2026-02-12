package com.sap.bfx.usermanagement.ldap;

import com.sap.bfx.usermanagement.UserService;
import com.sap.bfx.usermanagement.exception.UserManagementServiceException;
import com.sap.bfx.usermanagement.utility.User;
import com.sap.bfx.usermanagement.utility.UserSearchCriteria;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LdapUserServiceLocal extends UserService {

    // for searches on the full LDAP tree necessary (a little bit slower in
    // case not in cache regarding loop search)
    User getUserById(String id);

    // for searches on the full LDAP tree necessary (a little bit slower in
    // case not in cache regarding loop search)
    User getUserByUniqueName(String uniqueName);

    String getPathBySearchBaseUser(User searchBaseUser);

    String getPathBySearchBaseBusinessSystem(String searchBaseBusinessSystem);

    Map<String, String> getFullnameOfUsersByIdsAndRootPath(List<String> ids, String rootPath) throws UserManagementServiceException;

    Map<String, String> getFullnameOfUsersByIdsAndRootPath(Set<String> ids, String rootPath) throws UserManagementServiceException;

    String getUniqueNameOfUserByIdAndRootPath(String id, String rootPath);

    Map<String, User> getUsersByIdsAndRootPath(List<String> ids, String rootPath) throws UserManagementServiceException;

    Map<String, User> getUsersByIdsAndRootPath(Set<String> ids, String rootPath) throws UserManagementServiceException;

    String getIdOfUserByUniqueNameAndRootPath(String uniqueName, String rootPath);

    Map<String, String> getIdOfUsersByUniqueNamesAndRootPath(List<String> uniqueNames, String rootPath) throws UserManagementServiceException;

    Map<String, String> getIdOfUsersByUniqueNamesAndRootPath(Set<String> uniqueNames, String rootPath) throws UserManagementServiceException;

    Map<String, User> getUsersByUniqueNamesAndRootPath(List<String> uniqueNames, String rootPath) throws UserManagementServiceException;

    Map<String, User> getUsersByUniqueNamesAndRootPath(Set<String> uniqueNames, String rootPath) throws UserManagementServiceException;

    String determineFullnameOfUserByUniqueNameAndRootPath(String uniqueName, String rootPath);

    String determineFullnameOfUserByIdAndRootPath(String id, String rootPath);

    String getDisplayNameOfUserAndRootPath(String uniqueName, String rootPath);

    String getFullNameOfUserAndRootPath(String uniqueName, String rootPath);

    List<User> findByCriteriaAndRootPath(UserSearchCriteria userSearchCriteria, String rootPath);

    List<User> findByCriteriaAndRootPaths(UserSearchCriteria userSearchCriteria, List<String> rootPaths);

    User getUserByIdAndRootPath(String id, String rootPath);

    User getUserByUniqueNameAndRootPath(String uniqueName, String rootPath);
}
