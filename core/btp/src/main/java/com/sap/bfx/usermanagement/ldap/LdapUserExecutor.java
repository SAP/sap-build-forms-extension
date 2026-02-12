package com.sap.bfx.usermanagement.ldap;

import com.sap.bfx.usermanagement.exception.IllegalQueryException;
import com.sap.bfx.usermanagement.exception.UserManagementRuntimeException;
import com.sap.bfx.usermanagement.utility.User;
import com.sap.bfx.usermanagement.utility.UserSearchCriteria;
import com.sap.bfx.usermanagement.utility.query.ldap.LdapEscapeUtil;
import com.sap.bfx.usermanagement.utility.query.ldap.LdapUserSearchFilterCreator;
import com.sap.bfx.usermanagement.utility.query.ldap.LdapUserType;
import org.apache.commons.lang3.StringUtils;

import javax.naming.NameNotFoundException;
import java.util.*;

/**
 * executes ldap-queries
 */
public final class LdapUserExecutor {

    private static final LdapUserExecutor INSTANCE = new LdapUserExecutor();
    private final LdapAssociationProvider cache = LdapAssociationProvider.getInstance();
    private final LdapQueryExecutor ldapQueryExecutor = LdapQueryExecutor.getInstance();
    private final LdapUserSearchFilterCreator filterCreator = LdapUserSearchFilterCreator.getInstance();
    //private final LdapGroupsToSubgroupsProviderLocal ldapGroupsToSubgroupsProvider = JndiUtils.getEjbByInterface(LdapGroupsToSubgroupsProviderLocal.class);

    private LdapUserExecutor() {
    }

    public static LdapUserExecutor getInstance() {
        return INSTANCE;
    }

    public User findById(String id, User searchBaseUser) {
        LdapUserType[] userTypes = new LdapUserType[]{LdapUserType.USER};
        return this.findById(id, searchBaseUser, userTypes);
    }

    private User findById(String id, User searchBaseUser, LdapUserType... userTypes) {
        String theId = LdapEscapeUtil.escapeFilterValue(StringUtils.trim(id));
        User user = this.findByIdInCache(theId, searchBaseUser, null);
        if (user == null || !user.getId().equals(theId)) {
            // changed meanwhile, so query again
            UserSearchCriteria usc = new UserSearchCriteria();
            usc.setId(theId);
            usc.setLdapUserTypes(userTypes);
            user = ldapQueryExecutor.asSingleResult(this.findByCriteria(usc, searchBaseUser));
        }
        return user;
    }

    public User findByUserName(String userName, User searchBaseUser) {
        LdapUserType[] userTypes = new LdapUserType[]{LdapUserType.USER};
        return this.findByUserName(userName, searchBaseUser, userTypes);
    }

    private User findByUserName(String userName, User searchBaseUser, LdapUserType... userTypes) {
        String theUserName = LdapEscapeUtil.escapeFilterValue(StringUtils.trim(userName));
        User user = this.findByUserNameInCache(theUserName, searchBaseUser, null, null);
        if (user == null || !user.getUserName().equals(theUserName)) {
            // changed meanwhile, so query again
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setUserName(theUserName);
            criteria.setLdapUserTypes(userTypes);
            user = ldapQueryExecutor.asSingleResult(this.findByCriteria(criteria, searchBaseUser));
        }
        return user;
    }

    public List<User> findByCriteria(UserSearchCriteria usc, User searchBaseUser) {
        try {
            enrichMemberOfGroups(usc);
            String filter = filterCreator.toFilterString(usc);
            if (usc.getMaxResults() > 0) {
                return ldapQueryExecutor.queryViaUser(filter, usc.getMaxResults(), searchBaseUser);
            } else {
                return ldapQueryExecutor.queryViaUser(filter, searchBaseUser);
            }
        } catch (IllegalQueryException e) {
            throw new UserManagementRuntimeException("Illegal query, should never happen here...", e);
        }
    }

    private void enrichMemberOfGroups(UserSearchCriteria usc) {
        if (null != usc.getMemberOfGroups() && 0 < usc.getMemberOfGroups().length) {
            List<String> memberOfGroups = Arrays.asList(usc.getMemberOfGroups());
            Set<String> allRelevantMemberOfGroups = Collections.synchronizedSet(new HashSet<String>());
            memberOfGroups.parallelStream().forEach(aRootGroup -> {
                //List<String> subgroups = ldapGroupsToSubgroupsProvider.getSubgroupsOfGroup(aRootGroup);
                synchronized (allRelevantMemberOfGroups) {
                    // TODO add subgroups if relevant
                    /*if (aRootGroup.startsWith(Constants.CN_PREFIX)) {
                        allRelevantMemberOfGroups.add(aRootGroup);
                    } else {
                        allRelevantMemberOfGroups.add(Constants.CN_PREFIX.concat(aRootGroup));
                    }
                    if (!subgroups.isEmpty()) {
                        allRelevantMemberOfGroups.addAll(subgroups);
                    }*/
                    allRelevantMemberOfGroups.add(aRootGroup);
                }
            });
            usc.setMemberOfGroups(allRelevantMemberOfGroups.toArray(new String[allRelevantMemberOfGroups.size()]));
        }
    }

    public User findById(String id) {
        LdapUserType[] userTypes = new LdapUserType[]{LdapUserType.USER};
        return this.findById(id, userTypes);
    }

    private User findById(String id, LdapUserType... userTypes) {
        String theId = LdapEscapeUtil.escapeFilterValue(StringUtils.trim(id));
        User user = this.findByIdInCache(theId, null, null);
        if (user == null || !user.getId().equals(theId)) {
            // changed meanwhile, so query again
            UserSearchCriteria usc = new UserSearchCriteria();
            usc.setId(theId);
            usc.setLdapUserTypes(userTypes);
            user = ldapQueryExecutor.asSingleResult(this.findByCriteria(usc, true));
        }
        return user;
    }

    private User findByIdInCache(String escapedId, User searchBaseUser, String searchBaseBusinessSystem) {
        String searchPath = cache.getPathFromId(escapedId);
        if (null != searchBaseUser) {
            String refRootPath = ldapQueryExecutor.getPathBySearchBaseUser(searchBaseUser);
            if (!StringUtils.endsWithIgnoreCase(searchPath, refRootPath)) {
                return null;
            }
        }
        if (null != searchBaseBusinessSystem) {
            String refRootPath = ldapQueryExecutor.getPathBySearchBaseBusinessSystem(searchBaseBusinessSystem);
            if (!StringUtils.endsWithIgnoreCase(searchPath, refRootPath)) {
                return null;
            }
        }
        if (searchPath != null) {
            try {
                return ldapQueryExecutor.asSingleResult(ldapQueryExecutor.query(searchPath, "(cn=*)"));
            } catch (UserManagementRuntimeException e) {
                if (e.getCause() instanceof NameNotFoundException) {
                    // in case a NameNotFoundException the searchPath from the
                    // cache is obsolete, e.g. LDAP entry was deleted but in
                    // furthermore in
                    // the cache
                    cache.removeCacheEntryById(escapedId);
                }
            }
        }
        return null;
    }

    public User findByUserName(String userName) {
        LdapUserType[] userTypes = new LdapUserType[]{LdapUserType.USER};
        return this.findByUserName(userName, userTypes);
    }

    private User findByUserName(String userName, LdapUserType... userTypes) {
        String theUserName = LdapEscapeUtil.escapeFilterValue(StringUtils.trim(userName));
        User user = this.findByUserNameInCache(theUserName, null, null, null);
        if (user == null || !user.getUserName().equals(theUserName)) {
            // changed meanwhile, so query again
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setUserName(theUserName);
            criteria.setLdapUserTypes(userTypes);
            user = ldapQueryExecutor.asSingleResult(this.findByCriteria(criteria, true));
        }
        return user;
    }

    private User findByUserNameInCache(String escapedUserName, User searchBaseUser, String searchBaseBusinessSystem, String rootPath) {
        String searchPath = cache.getPathFromUserName(escapedUserName);
        if (null != searchBaseUser) {
            String refRootPath = ldapQueryExecutor.getPathBySearchBaseUser(searchBaseUser);
            if (!StringUtils.endsWithIgnoreCase(searchPath, refRootPath)) {
                return null;
            }
        }
        if (null != searchBaseBusinessSystem) {
            String refRootPath = ldapQueryExecutor.getPathBySearchBaseBusinessSystem(searchBaseBusinessSystem);
            if (!StringUtils.endsWithIgnoreCase(searchPath, refRootPath)) {
                return null;
            }
        }
        if (null != rootPath) {
            if (!StringUtils.endsWithIgnoreCase(searchPath, rootPath)) {
                return null;
            }
        }
        if (searchPath != null) {
            try {
                return ldapQueryExecutor.asSingleResult(ldapQueryExecutor.query(searchPath, "(cn=*)"));
            } catch (UserManagementRuntimeException e) {
                if (e.getCause() instanceof NameNotFoundException) {
                    // in case a NameNotFoundException the searchPath from the
                    // cache is obsolete, e.g. LDAP entry was deleted but is
                    // furthermore in
                    // the cache
                    cache.removeCacheEntryByUserName(escapedUserName);
                }
            }
        }
        return null;
    }

    public List<User> findByCriteria(UserSearchCriteria usc, boolean abortPathsLoopOnFirstHit) {
        try {
            enrichMemberOfGroups(usc);
            String filter = filterCreator.toFilterString(usc);
            if (usc.getMaxResults() > 0) {
                return ldapQueryExecutor.query(filter, usc.getMaxResults(), abortPathsLoopOnFirstHit);
            } else {
                return ldapQueryExecutor.query(filter, abortPathsLoopOnFirstHit);
            }
        } catch (IllegalQueryException e) {
            throw new UserManagementRuntimeException("Illegal query, should never happen here...", e);
        }
    }

    public User findById(String id, String searchBaseBusinessSystem) {
        LdapUserType[] userTypes = new LdapUserType[]{LdapUserType.USER};
        return this.findById(id, searchBaseBusinessSystem, userTypes);
    }

    private User findById(String id, String searchBaseBusinessSystem, LdapUserType... userTypes) {
        String theId = LdapEscapeUtil.escapeFilterValue(StringUtils.trim(id));
        User user = this.findByIdInCache(theId, null, searchBaseBusinessSystem);
        if (user == null || !user.getId().equals(theId)) {
            // changed meanwhile, so query again
            UserSearchCriteria usc = new UserSearchCriteria();
            usc.setId(theId);
            usc.setLdapUserTypes(userTypes);
            user = ldapQueryExecutor.asSingleResult(this.findByCriteria(usc, searchBaseBusinessSystem));
        }
        return user;
    }

    public List<User> findByCriteria(UserSearchCriteria usc, String searchBaseBusinessSystem) {
        try {
            enrichMemberOfGroups(usc);
            String filter = filterCreator.toFilterString(usc);
            if (usc.getMaxResults() > 0) {
                return ldapQueryExecutor.queryViaBS(filter, usc.getMaxResults(), searchBaseBusinessSystem);
            } else {
                return ldapQueryExecutor.queryViaBS(filter, searchBaseBusinessSystem);
            }
        } catch (IllegalQueryException e) {
            throw new UserManagementRuntimeException("Illegal query, should never happen here...", e);
        }
    }

    public User findByUserName(String userName, String searchBaseBusinessSystem) {
        LdapUserType[] userTypes = new LdapUserType[]{LdapUserType.USER};
        return this.findByUserName(userName, searchBaseBusinessSystem, userTypes);
    }

    private User findByUserName(String userName, String searchBaseBusinessSystem, LdapUserType... userTypes) {
        String theUserName = LdapEscapeUtil.escapeFilterValue(StringUtils.trim(userName));
        User user = this.findByUserNameInCache(theUserName, null, searchBaseBusinessSystem, null);
        if (user == null || !user.getUserName().equals(theUserName)) {
            // changed meanwhile, so query again
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setUserName(theUserName);
            criteria.setLdapUserTypes(userTypes);
            user = ldapQueryExecutor.asSingleResult(this.findByCriteria(criteria, searchBaseBusinessSystem));
        }
        return user;
    }

    public User findByUserNameAndRootPath(String userName, String rootPath) {
        LdapUserType[] userTypes = new LdapUserType[]{LdapUserType.USER};
        return this.findByUserNameAndRootPath(userName, rootPath, userTypes);
    }

    private User findByUserNameAndRootPath(String userName, String rootPath, LdapUserType... userTypes) {
        String theUserName = LdapEscapeUtil.escapeFilterValue(StringUtils.trim(userName));
        User user = this.findByUserNameInCache(theUserName, null, null, rootPath);
        if (user == null || !user.getUserName().equals(theUserName)) {
            // changed meanwhile, so query again
            UserSearchCriteria criteria = new UserSearchCriteria();
            criteria.setUserName(theUserName);
            criteria.setLdapUserTypes(userTypes);
            user = ldapQueryExecutor.asSingleResult(this.findByCriteriaAndRootPath(criteria, rootPath));
        }
        return user;
    }

    public List<User> findByCriteriaAndRootPath(UserSearchCriteria usc, String rootPath) {
        try {
            enrichMemberOfGroups(usc);
            String filter = filterCreator.toFilterString(usc);
            if (usc.getMaxResults() > 0) {
                return ldapQueryExecutor.query(rootPath, filter, usc.getMaxResults());
            } else {
                return ldapQueryExecutor.query(rootPath, filter);
            }
        } catch (IllegalQueryException e) {
            throw new UserManagementRuntimeException("Illegal query, should never happen here...", e);
        }
    }

    public List<User> findByCriteriaAndRootPaths(UserSearchCriteria usc, List<String> rootPaths) {
        try {
            enrichMemberOfGroups(usc);
            String filter = filterCreator.toFilterString(usc);
            if (usc.getMaxResults() > 0) {
                return ldapQueryExecutor.query(rootPaths, filter, usc.getMaxResults(), false);
            } else {
                return ldapQueryExecutor.query(rootPaths, filter, false);
            }
        } catch (IllegalQueryException e) {
            throw new UserManagementRuntimeException("Illegal query, should never happen here...", e);
        }
    }

    public User findByIdAndRootPath(String id, String rootPath) {
        LdapUserType[] userTypes = new LdapUserType[]{LdapUserType.USER};
        return this.findByIdAndRootPath(id, rootPath, userTypes);
    }

    private User findByIdAndRootPath(String id, String rootPath, LdapUserType... userTypes) {
        String theId = LdapEscapeUtil.escapeFilterValue(StringUtils.trim(id));
        User user = this.findByIdInCache(theId, null, null);
        if (user == null || !user.getId().equals(theId)) {
            // changed meanwhile, so query again
            UserSearchCriteria usc = new UserSearchCriteria();
            usc.setId(theId);
            usc.setLdapUserTypes(userTypes);
            user = ldapQueryExecutor.asSingleResult(this.findByCriteriaAndRootPath(usc, rootPath));
        }
        return user;
    }

}
