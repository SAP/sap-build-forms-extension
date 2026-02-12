package com.sap.bfx.usermanagement.ldap;

import com.sap.bfx.usermanagement.exception.UserManagementRuntimeException;
import lombok.Getter;
import lombok.Setter;

import javax.naming.NamingException;
import javax.naming.ldap.InitialLdapContext;
import java.util.Hashtable;

/**
 * provides InitialLdapContexts associates on a threadlocal
 */
@Setter
@Getter
public final class InitialLdapContextProvider {

    private static final InitialLdapContextProvider INSTANCE = new InitialLdapContextProvider();
    private static final ThreadLocal<InitialLdapContext> ldapContextThread = new ThreadLocal<InitialLdapContext>();
    private String destinationName;
    private String onPremDestinationCrName;
    private String onPremDestinationCrNamespace;

    private InitialLdapContextProvider() {
    }

    public static InitialLdapContextProvider getInstance() {
        return INSTANCE;
    }

    public boolean hasInitialLdapContext() {
        return null != ldapContextThread.get();
    }

    public InitialLdapContext getInitialLdapContext() {
        try {
            if (null == ldapContextThread.get()) {
                createAndSetInitialLdapContext();
            }
            return ldapContextThread.get();
        } catch (NamingException e) {
            throw new UserManagementRuntimeException("unable to locate ldap", e);
        }
    }

    private void createAndSetInitialLdapContext() throws NamingException {
        Hashtable<?, ?> props = LdapConfigProvider.getInstance().getLdapProperties(getDestinationName(), getOnPremDestinationCrName(), getOnPremDestinationCrNamespace());
        InitialLdapContext ctx = new InitialLdapContext(props, null);
        ldapContextThread.set(ctx);
    }

    public void cleanupAndClose() {
        if (null != ldapContextThread.get()) {
            try {
                ldapContextThread.get().close();
                ldapContextThread.set(null);
            } catch (NamingException e) {
                throw new UserManagementRuntimeException("unable to close ctx", e);
            }
        }
    }

}