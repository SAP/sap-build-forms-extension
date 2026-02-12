package com.sap.bfx.usermanagement.utility.query.ldap;

import com.sap.bfx.usermanagement.exception.IllegalQueryException;
import com.sap.bfx.usermanagement.utility.query.ICondition;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LdapCondition implements ICondition {

	private String attribute;
	private String value;

	public LdapCondition() {
		super();
	}

	public LdapCondition(String attribute, String value) {
		super();
		this.attribute = attribute;
		this.value = value;
	}

    @Override
	public String toQueryString() throws IllegalQueryException {
		if (attribute == null) {
			throw new IllegalQueryException("attribute must not be null!");
		}
		return value != null ? "(" + attribute + "=" + LdapEscapeUtil.escapeFilterValue(value) + ")" : "(!(" + attribute + "=*))";
	}

}