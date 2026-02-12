package com.sap.bfx.usermanagement.ldap;

import com.sap.bfx.usermanagement.exception.MultipleUsersFoundException;
import com.sap.bfx.usermanagement.exception.UserManagementRuntimeException;
import com.sap.bfx.usermanagement.utility.User;
import com.sap.bfx.usermanagement.utility.query.ldap.LdapUserMapper;
import lombok.extern.slf4j.Slf4j;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * low level search for ldap-users
 */
@Slf4j
public final class LdapQueryExecutor {

    private static final LdapQueryExecutor INSTANCE = new LdapQueryExecutor();

    private final InitialLdapContextProvider contextProvider = InitialLdapContextProvider.getInstance();
    private final LdapConfigProvider configProvider = LdapConfigProvider.getInstance();
    private final LdapUserMapper ldapUserMapper = LdapUserMapper.getInstance();
    private final LdapAssociationProvider cache = LdapAssociationProvider.getInstance();
    //private final PropertyProviderLocal propertyProvider = JndiUtils.getEjbByInterface(PropertyProviderLocal.class);

    private LdapQueryExecutor() {
    }

    public static LdapQueryExecutor getInstance() {
        return INSTANCE;
    }

    public String getPathBySearchBaseUser(User searchBaseUser) {
        if (searchBaseUser == null) {
            throw new UserManagementRuntimeException("The searchBaseUser must not be null");
        }
        // example for dn
        // CN=<UserLogonName>,OU=Users,OU=IDM_Managed,DC=int,DC=lidl,DC=net";
        String key = searchBaseUser.getDn().substring(searchBaseUser.getDn().indexOf("DC="));
        if (!configProvider.getRootPathsHashMap().containsKey(key)) {
            throw new UserManagementRuntimeException("The domain '" + key + "' is unknown inside the properties");
        }
        return configProvider.getRootPathsHashMap().get(key);
    }

    public String getPathBySearchBaseBusinessSystem(String searchBaseBusinessSystem) {
        if (searchBaseBusinessSystem == null) {
            throw new UserManagementRuntimeException("The searchBaseBusinessSystem must not be null");
        }
        if (!configProvider.getBSToRootPathsHashMap().containsKey(searchBaseBusinessSystem)) {
            throw new UserManagementRuntimeException("The searchBaseBusinessSystem '" + searchBaseBusinessSystem + "' is unknown inside the properties");
        }
        return configProvider.getBSToRootPathsHashMap().get(searchBaseBusinessSystem);
    }

    public User asSingleResult(List<User> user) {
        if (user == null || user.isEmpty()) {
            return null;
        } else if (user.size() == 1) {
            return user.get(0);
        } else {
            StringBuilder sb = new StringBuilder();
            for (User act : user) {
                if (!sb.isEmpty()) {
                    sb.append(",");
                }
                sb.append(act.getDn());
            }
            throw new MultipleUsersFoundException(user.get(0).getUserName(), sb.toString());
        }
    }

    public List<User> queryViaUser(String filter, User searchBaseUser) {
        return this.query(this.getPathBySearchBaseUser(searchBaseUser), filter);
    }

    public List<User> queryViaUser(String filter, long maxResult, User searchBaseUser) {
        return this.query(this.getPathBySearchBaseUser(searchBaseUser), filter, maxResult);
    }

    public List<User> queryViaBS(String filter, String searchBaseBusinessSystem) {
        return this.query(this.getPathBySearchBaseBusinessSystem(searchBaseBusinessSystem), filter);
    }

    public List<User> queryViaBS(String filter, long maxResult, String searchBaseBusinessSystem) {
        return this.query(this.getPathBySearchBaseBusinessSystem(searchBaseBusinessSystem), filter, maxResult);
    }

    public List<User> query(String path, String filter) {
        return this.query(path, filter, configProvider.getLdapMaxSearchResults());
    }

    public List<User> query(String path, String filter, long maxResult) {
        InitialLdapContext ctx = contextProvider.getInitialLdapContext();
        List<User> users = new ArrayList<User>();
        try {
            SearchControls ctrl = new SearchControls();
            ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctrl.setReturningObjFlag(true);
            ctrl.setCountLimit(maxResult);
            ctrl.setReturningAttributes(ldapUserMapper.getAttributesForUser());
            NamingEnumeration<?> enumeration = ctx.search(path, filter, ctrl);
            long count = 0;
            while (count < maxResult && enumeration.hasMore()) {
                SearchResult ldapResult = (SearchResult) enumeration.next();
                User user = ldapUserMapper.mapToUser(ldapResult);
                cache.setFromUser(user);
                users.add(user);
                count++;
            }

        } catch (NamingException e) {
            throw new UserManagementRuntimeException("path:" + path + ",filter:" + filter, e);
        }
        return users;
    }

    List<User> query(String filter, boolean abortPathsLoopOnFirstHit) {
        return this.query(configProvider.getRootPaths(), filter, abortPathsLoopOnFirstHit);
    }

    List<User> query(String filter, long maxResult, boolean abortPathsLoopOnFirstHit) {
        return this.query(configProvider.getRootPaths(), filter, maxResult, abortPathsLoopOnFirstHit);
    }

    List<User> query(List<String> paths, String filter, boolean abortPathsLoopOnFirstHit) {
        return this.query(paths, filter, configProvider.getLdapMaxSearchResults(), abortPathsLoopOnFirstHit);
    }

    List<User> query(List<String> paths, String filter, long maxResult, boolean abortPathsLoopOnFirstHit) {
        InitialLdapContext ctx = contextProvider.getInitialLdapContext();
        List<User> users = new ArrayList<User>();
        for (String aPath : paths) {
            try {
                SearchControls ctrl = new SearchControls();
                ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
                ctrl.setReturningObjFlag(true);
                ctrl.setCountLimit(maxResult);
                ctrl.setReturningAttributes(ldapUserMapper.getAttributesForUser());
                NamingEnumeration<?> enumeration = ctx.search(aPath, filter, ctrl);
                long count = 0;
                while (count < maxResult && users.size() < maxResult && enumeration.hasMore()) {
                    SearchResult ldapResult = (SearchResult) enumeration.next();
                    User user = ldapUserMapper.mapToUser(ldapResult);
                    cache.setFromUser(user);
                    users.add(user);
                    count++;
                }
                if (abortPathsLoopOnFirstHit && !users.isEmpty() || users.size() == maxResult) {
                    break;
                }
            } catch (NamingException e) {
                throw new UserManagementRuntimeException("path:" + aPath + ",filter:" + filter, e);
            }
        }
        return users;
    }

    public List<String> getUniqueIdentifierFromLdap(String rootPath, String filter, boolean groupSearch) {
        List<String> uniqueIdentifier = new ArrayList<String>();
        String[] attribs = new String[1];
        if (groupSearch) {
            attribs[0] = LdapConstants.LDAP_ATTRIBUTE_DISTINGUISHED_NAME;
        } else {
            // the ldap attribute name from ldap, e.g. userPrincipalName (after
            // Ldap migration) or sAMAccountName (before Ldap migration)
            attribs[0] = LdapUserMapper.LDAP_ATTRIBUTE_USERPRINCIPALNAME;
        }

        byte[] cookie = null;
        LdapContext ctx = null;
        try {
            Hashtable<?, ?> props = LdapConfigProvider.getInstance().getLdapProperties(InitialLdapContextProvider.getInstance().getDestinationName(), InitialLdapContextProvider.getInstance().getOnPremDestinationCrName(), InitialLdapContextProvider.getInstance().getOnPremDestinationCrNamespace());
            ctx = new InitialLdapContext(props, null);
            SearchControls ctrl = new SearchControls();
            ctrl.setSearchScope(SearchControls.SUBTREE_SCOPE);
            ctrl.setReturningObjFlag(true);
            ctrl.setCountLimit(LdapConstants.LDAP_COUNT_LIMIT);
            ctrl.setReturningAttributes(attribs);
            ctx.setRequestControls(new Control[] { new PagedResultsControl(LdapConfigProvider.getInstance().getLdapMaxSearchResults(), false) });
            do {
                NamingEnumeration<?> enumeration = ctx.search(rootPath, filter, ctrl);
                while (enumeration.hasMore()) {
                    SearchResult ldapResult = (SearchResult) enumeration.next();
                    Attributes resultAttribs = ldapResult.getAttributes();
                    String attributeValue = getLdapAttributeValue(resultAttribs, attribs[0]);
                    if (null != attributeValue) {
                        uniqueIdentifier.add(attributeValue);
                    }
                }
                Control[] controls = ctx.getResponseControls();
                if (controls != null) {
                    for (int l = 0; l < controls.length; l++) {
                        if (controls[l] instanceof PagedResultsResponseControl prrc) {
                            cookie = prrc.getCookie();
                        }
                    }
                }
                ctx.setRequestControls(new Control[] { new PagedResultsControl(LdapConfigProvider.getInstance().getLdapMaxSearchResults(), cookie, false) });
            } while (cookie != null);
        } catch (Exception e) {
            log.error("getUniqueIdentifierFromLdap() error in path:{}, filter:{}", rootPath, filter, e);
        } finally {
            if (null != ctx) {
                try {
                    ctx.close();
                } catch (Exception e) {
                    log.error("getUniqueIdentifierFromLdap() error in path:{}, filter:{}", rootPath, filter, e);
                }
            }
        }
        return uniqueIdentifier;
    }

    private String getLdapAttributeValue(Attributes attributes, String ldapAttributeName) throws NamingException {
        String value = null;
        if (null == attributes || null == attributes.get(ldapAttributeName)) {
            return null;
        }
        NamingEnumeration<?> values = attributes.get(ldapAttributeName).getAll();
        if (values.hasMore()) {
            Object next = values.next();
            value = (next == null) ? null : String.valueOf(next);
        }
        return value;
    }
}
