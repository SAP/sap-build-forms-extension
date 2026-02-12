package com.sap.bfx.usermanagement.ldap;

import com.sap.bfx.usermanagement.exception.UserManagementServiceException;
import com.sap.bfx.usermanagement.utility.User;
import com.sap.bfx.usermanagement.utility.query.ICondition;
import com.sap.bfx.usermanagement.utility.query.ldap.LdapCompositeCondition;
import com.sap.bfx.usermanagement.utility.query.ldap.LdapCondition;
import com.sap.bfx.usermanagement.utility.query.ldap.LdapConditionUtil;
import com.sap.bfx.usermanagement.utility.query.ldap.LdapUserMapper;

import java.util.*;

/**
 * search of multiple instances
 */
public final class LdapBulkExecutor {

    private static final LdapBulkExecutor INSTANCE = new LdapBulkExecutor();
    private static final int BULKSIZE = 500;
    private static final LdapUserAttributeProvider BY_USER_NAME = new LdapUserAttributeProvider() {
        @Override
        public String getUserAttribute(User user) {
            return user == null ? null : user.getUserName();
        }
    };
    private static final LdapUserAttributeProvider BY_ID = new LdapUserAttributeProvider() {
        @Override
        public String getUserAttribute(User user) {
            return user == null ? null : user.getId();
        }
    };
    private final LdapQueryExecutor ldapQueryExecutor = LdapQueryExecutor.getInstance();
    private final LdapAssociationProvider cache = LdapAssociationProvider.getInstance();

    private LdapBulkExecutor() {
    }

    public static LdapBulkExecutor getInstance() {
        return INSTANCE;
    }

    /**
     * find of multiple ids, try select by "primary key" first
     */
    public Map<String, User> findByIds(List<String> ids, User searchBaseUser) throws UserManagementServiceException {
        return this.findByIds(new HashSet<String>(ids), searchBaseUser);
    }

    public Map<String, User> findByIds(Set<String> ids, User searchBaseUser) throws UserManagementServiceException {
        Set<String> userNames = cache.getUserNameFromId(ids);
        ArrayList<User> resultAsList = new ArrayList<User>();
        Set<String> remainder = new HashSet<String>(ids);
        if (!userNames.isEmpty()) {
            resultAsList.addAll(this.findByUserNames(userNames, searchBaseUser).values());
            for (User aResultUser : resultAsList) {
                // null check in case cache contains userNames that are no longer
                // exist and findByUniqueNames() returns null objects
                if (aResultUser != null) {
                    remainder.remove(aResultUser.getId());
                }
            }
        }
        if (!remainder.isEmpty()) {
            // now select remainder
            resultAsList.addAll(this.findByAttribute(LdapUserMapper.LDAP_ATTRIBUTE_EMPLOYEEID, ids, BY_ID, searchBaseUser, null, null).values());
        }
        Map<String, User> idToUser = new HashMap<String, User>();
        for (User aResultUser : resultAsList) {
            if (aResultUser != null && ids.contains(aResultUser.getId())) {
                idToUser.put(aResultUser.getId(), aResultUser);
            }
        }
        return idToUser;
    }

    public Map<String, User> findByIds(List<String> ids, String searchBaseBusinessSystem) throws UserManagementServiceException {
        return this.findByIds(new HashSet<String>(ids), searchBaseBusinessSystem);
    }

    public Map<String, User> findByIds(Set<String> ids, String searchBaseBusinessSystem) throws UserManagementServiceException {
        Set<String> userNames = cache.getUserNameFromId(ids);
        ArrayList<User> resultAsList = new ArrayList<User>();
        Set<String> remainder = new HashSet<String>(ids);
        if (!userNames.isEmpty()) {
            resultAsList.addAll(this.findByUserNames(userNames, searchBaseBusinessSystem).values());
            for (User aResultUser : resultAsList) {
                // null check in case cache contains userNames that are no longer
                // exist and findByUniqueNames() returns null objects
                if (aResultUser != null) {
                    remainder.remove(aResultUser.getId());
                }
            }
        }
        if (!remainder.isEmpty()) {
            // now select remainder
            resultAsList.addAll(this.findByAttribute(LdapUserMapper.LDAP_ATTRIBUTE_EMPLOYEEID, ids, BY_ID, null, searchBaseBusinessSystem, null).values());
        }
        Map<String, User> idToUser = new HashMap<String, User>();
        for (User aResultUser : resultAsList) {
            if (aResultUser != null && ids.contains(aResultUser.getId())) {
                idToUser.put(aResultUser.getId(), aResultUser);
            }
        }
        return idToUser;
    }

    /**
     * Search by user names, performant due "primary key" select in tree
     */
    public Map<String, User> findByUserNames(List<String> userNames, User searchBaseUser) throws UserManagementServiceException {
        return this.findByUserNames(new HashSet<String>(userNames), searchBaseUser);
    }

    public Map<String, User> findByUserNames(Set<String> userNames, User searchBaseUser) throws UserManagementServiceException {
        return this.findByAttribute(LdapUserMapper.LDAP_ATTRIBUTE_USERPRINCIPALNAME, userNames, BY_USER_NAME, searchBaseUser, null, null);
    }

    public Map<String, User> findByUserNames(List<String> userNames, String searchBaseBusinessSystem) throws UserManagementServiceException {
        return this.findByUserNames(new HashSet<String>(userNames), searchBaseBusinessSystem);
    }

    public Map<String, User> findByUserNames(Set<String> userNames, String searchBaseBusinessSystem) throws UserManagementServiceException {
        return this.findByAttribute(LdapUserMapper.LDAP_ATTRIBUTE_USERPRINCIPALNAME, userNames, BY_USER_NAME, null, searchBaseBusinessSystem, null);
    }

    public Map<String, User> findByIdsAndRootPath(List<String> ids, String rootPath) throws UserManagementServiceException {
        return findByIdsAndRootPath(new HashSet<String>(ids), rootPath);
    }

    public Map<String, User> findByIdsAndRootPath(Set<String> ids, String rootPath) throws UserManagementServiceException {
        Set<String> ldapIDs = cache.getUserNameFromId(ids);
        ArrayList<User> resultAsList = new ArrayList<User>();
        Set<String> remainder = new HashSet<String>(ids);
        if (!ldapIDs.isEmpty()) {
            resultAsList.addAll(this.findByUniqueNamesAndRootPath(ldapIDs, rootPath).values());
            for (User aResultUser : resultAsList) {
                // null check in case cache contains ldapIDs that are no longer
                // exist and findByLdapIDsAndRootPath() returns null objects
                if (aResultUser != null) {
                    remainder.remove(aResultUser.getId());
                }
            }
        }
        if (!remainder.isEmpty()) {
            // now select remainder
            resultAsList.addAll(this.findByAttribute(LdapUserMapper.LDAP_ATTRIBUTE_EMPLOYEEID, ids, BY_ID, null, null, rootPath).values());
        }
        Map<String, User> gpsIDToUser = new HashMap<String, User>();
        for (User aResultUser : resultAsList) {
            if (aResultUser != null && ids.contains(aResultUser.getId())) {
                gpsIDToUser.put(aResultUser.getId(), aResultUser);
            }
        }
        return gpsIDToUser;
    }

    public Map<String, User> findByUniqueNamesAndRootPath(List<String> uniqueNames, String rootPath) throws UserManagementServiceException {
        return findByUniqueNamesAndRootPath(new HashSet<String>(uniqueNames), rootPath);
    }

    public Map<String, User> findByUniqueNamesAndRootPath(Set<String> uniqueNames, String rootPath) throws UserManagementServiceException {
        return this.findByAttribute(LdapUserMapper.LDAP_ATTRIBUTE_USERPRINCIPALNAME, uniqueNames, BY_USER_NAME, null, null, rootPath);
    }

    /**
     * @return Map mit user zu Attribut, ggf. null, wenn kein User gefunden
     * werden konnte
     */
    private Map<String, User> findByAttribute(String attribute, Set<String> values, LdapUserAttributeProvider provider, User searchBaseUser, String searchBaseBusinessSystem, String rootPath)
            throws UserManagementServiceException {
        Map<String, User> map = new HashMap<String, User>();

        if (values != null && !values.isEmpty()) {
            Set<String> setValues = new HashSet<String>(values);
            // eventuelle Leerstrings / nulls bereinigen
            setValues.remove("");
            setValues.remove(null);
            if (setValues.isEmpty()) {
                return map;
            }
            int beginIndex = 0;
            List<String> cleanValues = new ArrayList<String>(setValues);
            int endIndex = Math.min(beginIndex + BULKSIZE, cleanValues.size());
            while (endIndex > beginIndex) {
                List<String> subList = cleanValues.subList(beginIndex, endIndex);
                String[] valuesArr = subList.toArray(new String[subList.size()]);
                ICondition c = new LdapCondition(attribute, valuesArr[0]);
                for (int i = 1; i < valuesArr.length; i++) {
                    c = LdapConditionUtil.add(c, LdapCompositeCondition.CompositionType.OR, attribute, valuesArr[i]);
                }
                List<User> users = null;
                if (null != searchBaseUser) {
                    users = ldapQueryExecutor.queryViaUser(c.toQueryString(), BULKSIZE, searchBaseUser);
                } else if (null != searchBaseBusinessSystem) {
                    users = ldapQueryExecutor.queryViaBS(c.toQueryString(), BULKSIZE, searchBaseBusinessSystem);
                } else {
                    users = ldapQueryExecutor.query(rootPath, c.toQueryString(), BULKSIZE);
                }
                for (User user : users) {
                    map.put(provider.getUserAttribute(user), user);
                }
                for (String val : valuesArr) {
                    if (!map.containsKey(val)) {
                        map.put(val, null);
                    }
                }
                beginIndex = endIndex;
                endIndex = Math.min((beginIndex + BULKSIZE), (cleanValues.size()));
            }
        }
        return map;
    }
}
