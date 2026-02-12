package com.sap.bfx.usermanagement;

import com.sap.bfx.usermanagement.utility.User;
import com.sap.bfx.usermanagement.utility.UserSearchCriteria;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

    String getUniqueNameOfUser(String destinationName, String uniqueId);

    String getUniqueIdOfUser(String destinationName, String uniqueName, String originKey);

    User getUserOrNull(String destinationName, String uniqueName, String originKey);

    Map<String, User> findUsersMapByUniqueNames(String destinationName, List<String> uniqueNames, String originKey);

    List<User> findUsersListByUniqueNames(String destinationName, List<String> uniqueNames, String originKey);

    Boolean isSbpaUser(String destinationName, String uniqueName, String originKey);

    Boolean isSbpaUserByUniqueId(String destinationName, String uniqueId);

    List<User> findByCriteria(String destinationName, UserSearchCriteria userSearchCriteria);

    Map<Pair<String, String>, String> getUserAttributeByUniqueNameAndAttributeNames(String destinationName, String uniqueName, String originKey, Set<Pair<String, String>> attributePairs);

    Map<Pair<String, String>, String> getUserAttributeByUniqueNameAndAttributeNames(String destinationName, String uniqueName, String originKey, List<String> attributePrefixes, List<String> attributeNames);

    User getUserByUniqueId(String destinationName, String uniqueId);

    User getUserOrNullByUniqueId(String destinationName, String uniqueId, Boolean logException);
}
