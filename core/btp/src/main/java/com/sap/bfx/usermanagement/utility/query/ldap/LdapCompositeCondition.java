package com.sap.bfx.usermanagement.utility.query.ldap;

import com.sap.bfx.usermanagement.exception.IllegalQueryException;
import com.sap.bfx.usermanagement.utility.query.ICondition;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

@Setter
@Getter
public class LdapCompositeCondition implements ICondition {

	private ICondition[] conditions;
	private CompositionType type;

	public LdapCompositeCondition() {
		super();
	}

	public LdapCompositeCondition(CompositionType type, ICondition... pConditions) {
		super();
		this.conditions = pConditions;
		this.type = type;
	}

    @Getter
    public enum CompositionType {
		AND("&"), OR("|");

		private String character;

		private CompositionType(String character) {
			this.character = character;
		}
    }

	@Override
	public String toQueryString() throws IllegalQueryException {
		if (ArrayUtils.isEmpty(conditions)) {
			throw new IllegalQueryException("CompositeCondition contains no conditions!");
		}

		StringBuilder sb = new StringBuilder();
		CompositionType compType = getType() != null ? getType() : CompositionType.AND;
		sb.append("(").append(compType.getCharacter());

		for (int i = 0; i < conditions.length; i++) {
			ICondition c = conditions[i];
			if (c == null) {
				throw new IllegalQueryException("condition at index " + i + " is null!");
			}
			sb.append(c.toQueryString());
		}

		sb.append(")");
		return sb.toString();
	}
}