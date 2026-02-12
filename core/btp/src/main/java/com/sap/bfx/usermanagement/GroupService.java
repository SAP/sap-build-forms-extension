package com.sap.bfx.usermanagement;

import com.sap.bfx.usermanagement.utility.Group;
import com.sap.bfx.usermanagement.utility.GroupSearchCriteria;
import com.sap.bfx.usermanagement.utility.User;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface GroupService {

    Group getGroupOrNull(String destinationName, String uniqueName);

    Group getGroupOrNullByUniqueId(String destinationName, String uniqueId, Boolean logException);

    String getUniqueIdOfGroup(String destinationName, String uniqueName);

    String getUniqueNameOfGroup(String destinationName, String uniqueId);

    String getDisplayNameOfGroup(String destinationName, String uniqueName);

    // only be used in case of test mode = true
    String getFirstMemberIdOfGroupByUniqueName(String destinationName, String uniqueName);

    // only be used in case of test mode = true
    User getFirstMemberOfGroupByUniqueNameOrNull(String destinationName, String uniqueName);

    // only be used in case of test mode = true
    String getFirstMemberUniqueNameOfGroupByUniqueName(String destinationName, String uniqueName);

    List<User> getMembersOfGroupByUniqueName(String destinationName, String uniqueName);

    boolean hasGroupBPMMembersByUniqueName(String destinationName, String uniqueName);

    boolean hasGroupMembersByUniqueName(String destinationName, String uniqueName);

    List<Group> findByCriteria(String destinationName, GroupSearchCriteria groupSearchCriteria);

    Map<String, String> getDisplayNameOfGroupsByIds(String destinationName, List<String> ids);

    Map<String, String> getDisplayNameOfGroupsByIds(String destinationName, Set<String> ids);

}
