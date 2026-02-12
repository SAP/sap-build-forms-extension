package com.sap.bfx.usermanagement.utility.query.scim;

import com.sap.bfx.usermanagement.exception.IllegalQueryException;
import com.sap.bfx.usermanagement.utility.query.ICondition;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ScimCondition implements ICondition {

    private String attribute;
    private OperatorType operator;
    private String value;

    public ScimCondition() {
        super();
    }

    public ScimCondition(String attribute, OperatorType operator, String value) {
        super();
        this.attribute = attribute;
        this.operator = operator;
        this.value = value;
    }

    @Override
    public String toQueryString() throws IllegalQueryException {
        if (attribute == null) {
            throw new IllegalQueryException("attribute must not be null!");
        }
        return value != null ? attribute + " " + operator.getCharacter() + " \"" + value + "\"" : "";
    }

    @Getter
    public enum OperatorType {
        EQ("eq"), CO("co"), SW("sw"), PR("pr"), GT("gt"), GE("ge"), LT("lt"), LE("le");

        private String character;

        private OperatorType(String character) {
            this.character = character;
        }
    }

}