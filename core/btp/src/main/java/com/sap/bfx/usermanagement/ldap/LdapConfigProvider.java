package com.sap.bfx.usermanagement.ldap;

import com.sap.cloud.sdk.cloudplatform.connectivity.Destination;
import com.sap.bfx.btp.ConnectivityUtils;
import com.sap.bfx.usermanagement.exception.UserManagementRuntimeException;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.Transformer;
import org.apache.commons.lang3.StringUtils;

import javax.naming.Context;
import java.util.*;

public final class LdapConfigProvider {

    private static final LdapConfigProvider ldapConfigProviderInstance = new LdapConfigProvider();

    Transformer<String, String> keyTransformer = new Transformer<String, String>() {
        @Override
        public String transform(String aPath) {
            return aPath.substring(aPath.indexOf("DC="));
        }
    };

    Transformer<String, String> valueTransformer = new Transformer<String, String>() {
        @Override
        public String transform(String aPath) {
            return aPath;
        }
    };

    private Properties readProperties;
    private Integer ldapMaxResult;
    private List<String> ldapRootPaths;
    private HashMap<String, String> ldapRootPathsHashMap;
    private HashMap<String, String> ldapBSToRootPathsHashMap;

    private LdapConfigProvider() {
    }

    public static LdapConfigProvider getInstance() {
        return ldapConfigProviderInstance;
    }

    /**
     * provides ldap properties
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Hashtable<?, ?> getLdapProperties(String destinationName, String onPremDestinationCrName, String onPremDestinationCrNamespace) {
        if (readProperties == null) {
            readLdapProperties(destinationName, onPremDestinationCrName, onPremDestinationCrNamespace);
        }
        return new Hashtable(readProperties);
    }

    private void readLdapProperties(String destinationName, String onPremDestinationCrName, String onPremDestinationCrNamespace) {
        Destination ldapDestination = ConnectivityUtils.getDestination(destinationName);
        String securityAuthentication = "simple";//p13nService.getPersonalization(LdapConstants.JAVA_NAMING_SECURITY_AUTHENTICATION);
        String ldapJavaNamingFactoryInitial = "com.sun.jndi.ldap.LdapCtxFactory";//p13nService.getPersonalization(LdapConstants.LDAP_JAVA_NAMING_FACTORY_INITIAL);
        String ldapComSunJndiLdapConnectPool = "true";//p13nService.getPersonalization(LdapConstants.LDAP_COM_SUN_JNDI_LDAP_CONNECT_POOL);
        String ldapComCunJndiLdapReadTimeout = "25000";//p13nService.getPersonalization(LdapConstants.LDAP_COM_SUN_JNDI_LDAP_READ_TIMEOUT);
        Properties props = null;
        if (StringUtils.isNotBlank(ldapDestination.get("ldap.url").get().toString()) && StringUtils.isNotBlank(ldapDestination.get("ldap.user").get().toString()) && StringUtils.isNotBlank(ldapDestination.get("ldap.password").get().toString())) {
            props = new Properties();
            if (StringUtils.isNotBlank(securityAuthentication)) {
                props.put(Context.SECURITY_AUTHENTICATION, securityAuthentication);
            }
            props.put(Context.SECURITY_PRINCIPAL, ldapDestination.get("ldap.user").get().toString());
            props.put(Context.SECURITY_CREDENTIALS, ldapDestination.get("ldap.password").get().toString());
            String targetProviderUrl = ldapDestination.get("ldap.url").get().toString();
            if (null != onPremDestinationCrName && null != onPremDestinationCrNamespace) {
                int startIndex = targetProviderUrl.indexOf("://") + 3;
                String target = targetProviderUrl.substring(startIndex, targetProviderUrl.indexOf(":", startIndex));
                targetProviderUrl = targetProviderUrl.replace(target, onPremDestinationCrName.concat(".").concat(onPremDestinationCrNamespace));
            }
            // e.g. "ldap://forms-inbox-ldap-destination.forms:389"
            props.put(Context.PROVIDER_URL, targetProviderUrl);
            if (StringUtils.isNotBlank(ldapJavaNamingFactoryInitial)) {
                props.put(Context.INITIAL_CONTEXT_FACTORY, ldapJavaNamingFactoryInitial);
            }
            if (StringUtils.isNotBlank(ldapComSunJndiLdapConnectPool)) {
                props.put(LdapConstants.COM_SUN_JNDI_LDAP_CONNECT_POOL, ldapComSunJndiLdapConnectPool);
            }
            if (StringUtils.isNotBlank(ldapComCunJndiLdapReadTimeout)) {
                props.put(LdapConstants.COM_SUN_JNDI_LDAP_READ_TIMEOUT, ldapComCunJndiLdapReadTimeout);
            }
        }
        synchronized (LdapConfigProvider.class) {
            // shortterm lock, maybe concurrent update, but doesn't matter
            readProperties = props;
        }
    }

    public int getLdapMaxSearchResults() {
        if (ldapMaxResult == null) {
            readLdapMaxSearchResults();
        }
        return ldapMaxResult;
    }

    private void readLdapMaxSearchResults() {
        Integer maxResult = 1000;
        try {
            // TODO read property from P13N server
            maxResult = Integer.parseInt("1000");//p13nService.getPersonalization(LdapConstants.LDAP_MAX_SEARCH_RESULTS));
        } catch (Exception e) {
            throw new UserManagementRuntimeException("Unable to locate application property " + LdapConstants.LDAP_MAX_SEARCH_RESULTS, e);
        }
        synchronized (LdapConfigProvider.class) {
            // shortterm lock, maybe concurrent update, but doesn't matter
            ldapMaxResult = maxResult;
        }
    }

    public List<String> getRootPaths() {
        if (ldapRootPaths == null) {
            readRootPaths();
        }
        return ldapRootPaths;
    }

    public HashMap<String, String> getRootPathsHashMap() {
        if (ldapRootPathsHashMap == null) {
            readRootPaths();
        }
        return ldapRootPathsHashMap;
    }

    private void readRootPaths() {
        List<String> rootPaths = new ArrayList<String>();
        HashMap<String, String> rootMap = new HashMap<String, String>();
        try {
            // TODO read property from P13N server
            String pathsParam = "";//p13nService.getPersonalization(LdapConstants.LDAP_ROOT_PATHS);
            String[] pathParts = pathsParam.split(";");
            rootPaths.addAll(Arrays.asList(pathParts));
            MapUtils.populateMap(rootMap, rootPaths, keyTransformer, valueTransformer);
        } catch (Exception e) {
            throw new UserManagementRuntimeException("Unable to locate application property " + LdapConstants.LDAP_ROOT_PATHS, e);
        }
        synchronized (LdapConfigProvider.class) {
            // shortterm lock, maybe concurrent update, but doesn't matter
            ldapRootPaths = rootPaths;
            ldapRootPathsHashMap = rootMap;
        }
    }

    public HashMap<String, String> getBSToRootPathsHashMap() {
        if (ldapBSToRootPathsHashMap == null) {
            readBSToRootPaths();
        }
        return ldapBSToRootPathsHashMap;
    }

    private void readBSToRootPaths() {
        HashMap<String, String> bsToRootPathsHashMap = new HashMap<String, String>();
        try {
            // TODO read property from P13N server
            String bsToRootPathsParam = "";//p13nService.getPersonalization(LdapConstants.LDAP_BS_TO_ROOT_PATHS);
            String[] pathParts = bsToRootPathsParam.split(";");
            for (String aPathPart : pathParts) {
                String[] splittParts = aPathPart.split("->");
                if (2 == splittParts.length) {
                    bsToRootPathsHashMap.put(splittParts[0], splittParts[1]);
                }
            }
        } catch (Exception e) {
            throw new UserManagementRuntimeException("Unable to locate application property " + LdapConstants.LDAP_BS_TO_ROOT_PATHS, e);
        }
        synchronized (LdapConfigProvider.class) {
            // shortterm lock, maybe concurrent update, but doesn't matter
            ldapBSToRootPathsHashMap = bsToRootPathsHashMap;
        }
    }

}
