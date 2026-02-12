package com.sap.bfx.usermanagement;

import com.sap.bfx.usermanagement.ldap.LdapUserServiceLocal;

public abstract class UserManagementFactory {

    public abstract UserService createScimUserService();

    public abstract GroupService createScimGroupService();

    public abstract LdapUserServiceLocal createLdapUserService(String destinationName, String onPremDestinationCrName, String onPremDestinationCrNamespace);

    public abstract GroupService createLdapGroupService();

}
