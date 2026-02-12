package com.sap.bfx.usermanagement.ldap;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * interceptor for services, assures recycling of contexts in invocation-chains assures close
 */
public final class LdapContextInterceptor implements InvocationHandler {

	private final InitialLdapContextProvider ldapContextProvider = InitialLdapContextProvider.getInstance();

	private final LdapUserServiceLocal ldapUserService;

	public LdapContextInterceptor(LdapUserServiceLocal ldapUserService, String destinationName, String onPremDestinationCrName, String onPremDestinationCrNamespace) {
		this.ldapUserService = ldapUserService;
        ldapContextProvider.setDestinationName(destinationName);
        ldapContextProvider.setOnPremDestinationCrName(onPremDestinationCrName);
        ldapContextProvider.setOnPremDestinationCrNamespace(onPremDestinationCrNamespace);
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		// remember old state
		boolean previousContext = ldapContextProvider.hasInitialLdapContext();
		try {
			return method.invoke(ldapUserService, args);
		} finally {
			if (ldapContextProvider.hasInitialLdapContext() && !previousContext) {
				// only state changes
				ldapContextProvider.cleanupAndClose();
			}
		}
	}
}