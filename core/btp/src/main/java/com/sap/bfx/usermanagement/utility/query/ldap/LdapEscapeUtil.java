package com.sap.bfx.usermanagement.utility.query.ldap;

public final class LdapEscapeUtil {

	private LdapEscapeUtil() {
	}

	/**
	 * removes characters that allows a LDAP injection (https://www.owasp.org/index.php/Preventing_LDAP_Injection_in_Java)
	 * 
	 * @param originalFilter
	 *            that must be checked
	 * @return filtered string
	 */
	public static final String escapeFilterValue(String originalFilter) {
		if (originalFilter == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < originalFilter.length(); i++) {
			char curChar = originalFilter.charAt(i);
			switch (curChar) {
			case '\\':
				sb.append("\\5c");
				break;
			// case '*':
			// sb.append("\\2a");
			// break;
			case '(':
				sb.append("\\28");
				break;
			case ')':
				sb.append("\\29");
				break;
			case '\u0000':
				sb.append("\\00");
				break;
			default:
				sb.append(curChar);
			}
		}
		return sb.toString();
	}

}