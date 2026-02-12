package com.sap.bfx.usermanagement.utility.query.ldap;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public enum LdapUserType {
	
	USER("user"),
	STORE("store");
	
	private final String usrType;

	private LdapUserType(String usrType) {
		this.usrType = usrType;
	}

    public static LdapUserType fromLdapType(String ldapType) {
		if (ldapType == null) {
			return null;
		}
		
		LdapUserType[] ldapUserTypes = LdapUserType.values();
		for (LdapUserType ldapUserType : ldapUserTypes) {
			if (ldapType.equals(ldapUserType.getUsrType())) {
				return ldapUserType;
			}
		}

        log.trace("Retrieved unknown {} from LDAP: {}", LdapUserType.class.getName(), ldapType);
		return null;
	}
}
