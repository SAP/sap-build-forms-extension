package com.sap.bfx.usermanagement.ldap;

import com.sap.bfx.usermanagement.utility.User;

public interface LdapUserAttributeProvider {

	String getUserAttribute(User user);

}
