package com.sap.bfx.usermanagement.utility.query;

import com.sap.bfx.usermanagement.exception.IllegalQueryException;

public interface ICondition {

	/**
	 * @return LDAP-Query String
	 * @throws IllegalQueryException
	 *             if the Condition cannot be resolved into a valid LDAP-Query
	 */
	String toQueryString() throws IllegalQueryException;

}
