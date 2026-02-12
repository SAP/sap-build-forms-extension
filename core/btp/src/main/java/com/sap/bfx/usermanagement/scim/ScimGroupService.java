package com.sap.bfx.usermanagement.scim;

import com.sap.bfx.btp.ConnectivityUtils;
import com.sap.bfx.usermanagement.GroupService;
import com.sap.bfx.usermanagement.utility.Group;
import com.sap.bfx.usermanagement.utility.GroupSearchCriteria;
import com.sap.bfx.usermanagement.utility.query.scim.ScimGroupMapper;
import com.sap.bfx.usermanagement.utility.User;
import com.sap.openapi.scim.api.ScimGroupsRoleCollectionsApi;
import com.sap.openapi.scim.model.ScimGroup;
import com.sap.openapi.scim.model.ScimGroupMember;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
public class ScimGroupService implements GroupService {

    private final ScimGroupMapper scimGroupMapper = ScimGroupMapper.getInstance();

    private static ScimGroup getScimGroup(String destinationName, String uniqueName) {
        ScimGroupsRoleCollectionsApi scimGrcApi = new ScimGroupsRoleCollectionsApi(ConnectivityUtils.getHttpDestination(destinationName));
        ScimGroup scimGroup = scimGrcApi.getGroupUsingGET(uniqueName);
        return scimGroup;
    }

    @Override
    public Group getGroupOrNull(String destinationName, String uniqueName) {
        if (StringUtils.isNotBlank(destinationName) && StringUtils.isNotBlank(uniqueName)) {
            try {
                ScimGroup scimGroup = getScimGroup(destinationName, uniqueName);
                return new Group(scimGroupMapper.mapToGroup(scimGroup));
            } catch (Exception e) {
                log.error("getGroupOrNull(): Error occurred for {}", uniqueName, e);
            }
        }
        return null;
    }

    @Override
    public Group getGroupOrNullByUniqueId(String destinationName, String uniqueId, Boolean logException) {
        if (StringUtils.isNotBlank(destinationName) && StringUtils.isNotBlank(uniqueId)) {
            try {
                ScimGroup scimGroup = getScimGroup(destinationName, uniqueId);
                return new Group(scimGroupMapper.mapToGroup(scimGroup));
            } catch (Exception e) {
                if (logException) {
                    log.error("getGroupOrNull(): Error occurred for {}", uniqueId, e);
                }
            }
        }
        return null;
    }

    @Override
    public String getUniqueIdOfGroup(String destinationName, String uniqueName) {
        if (StringUtils.isNotBlank(destinationName) && StringUtils.isNotBlank(uniqueName)) {
            return getScimGroup(destinationName, uniqueName).getId();
        }
        return null;
    }

    @Override
    public String getUniqueNameOfGroup(String destinationName, String uniqueId) {
        if (StringUtils.isNotBlank(destinationName) && StringUtils.isNotBlank(uniqueId)) {
            return getScimGroup(destinationName, uniqueId).getId();
        }
        return null;
    }

    @Override
    public String getDisplayNameOfGroup(String destinationName, String uniqueName) {
        if (StringUtils.isNotBlank(destinationName) && StringUtils.isNotBlank(uniqueName)) {
            return getScimGroup(destinationName, uniqueName).getDisplayName();
        }
        return null;
    }

    @Override
    public String getFirstMemberIdOfGroupByUniqueName(String destinationName, String uniqueName) {
        if (StringUtils.isNotBlank(destinationName) && StringUtils.isNotBlank(uniqueName)) {
            List<ScimGroupMember> scimGroup = getScimGroup(destinationName, uniqueName).getMembers();
            if (!scimGroup.isEmpty()) return scimGroup.get(0).getValue();
        }
        return null;
    }

    @Override
    public User getFirstMemberOfGroupByUniqueNameOrNull(String destinationName, String uniqueName) {
        return null;
    }

    @Override
    public String getFirstMemberUniqueNameOfGroupByUniqueName(String destinationName, String uniqueName) {
        return "";
    }

    @Override
    public List<User> getMembersOfGroupByUniqueName(String destinationName, String uniqueName) {
        return List.of();
    }

    @Override
    public boolean hasGroupBPMMembersByUniqueName(String destinationName, String uniqueName) {
        return false;
    }

    @Override
    public boolean hasGroupMembersByUniqueName(String destinationName, String uniqueName) {
        return false;
    }

    @Override
    public List<Group> findByCriteria(String destinationName, GroupSearchCriteria groupSearchCriteria) {
        return List.of();
    }

    @Override
    public Map<String, String> getDisplayNameOfGroupsByIds(String destinationName, List<String> ids) {
        return Map.of();
    }

    @Override
    public Map<String, String> getDisplayNameOfGroupsByIds(String destinationName, Set<String> ids) {
        return Map.of();
    }
}
