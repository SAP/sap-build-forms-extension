package com.sap.bfx.usermanagement;

import com.sap.bfx.usermanagement.ldap.LdapContextInterceptor;
import com.sap.bfx.usermanagement.ldap.LdapUserService;
import com.sap.bfx.usermanagement.ldap.LdapUserServiceLocal;
import com.sap.bfx.usermanagement.scim.ScimGroupService;
import com.sap.bfx.usermanagement.scim.ScimUserService;

import java.lang.reflect.Proxy;

public final class UserManagementFactoryV1 extends UserManagementFactory {

    private static final UserManagementFactoryV1 INSTANCE = new UserManagementFactoryV1();

    private UserManagementFactoryV1() {
    }

    public static UserManagementFactoryV1 getInstance() {
        return INSTANCE;
    }

    @Override
    public UserService createScimUserService() {
        return new ScimUserService();
    }

    @Override
    public GroupService createScimGroupService() {
        return new ScimGroupService();
    }

    @Override
    public LdapUserServiceLocal createLdapUserService(String destinationName, String onPremDestinationCrName, String onPremDestinationCrNamespace) {
        LdapUserService originalLdapUserService = new LdapUserService();
        return (LdapUserServiceLocal) Proxy.newProxyInstance(
                originalLdapUserService.getClass().getClassLoader(),
                originalLdapUserService.getClass().getInterfaces(),
                new LdapContextInterceptor(originalLdapUserService, destinationName, onPremDestinationCrName, onPremDestinationCrNamespace)
        );
    }

    @Override
    public GroupService createLdapGroupService() {
        return null;
    }
}
