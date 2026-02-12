package com.sap.bfx.usermanagement.utility.query.scim;

import com.sap.bfx.usermanagement.exception.IllegalQueryException;
import com.sap.bfx.usermanagement.utility.query.ICondition;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Iterator;

@Setter
@Getter
public class ScimCompositeCondition implements ICondition {

    private ICondition[] conditions;
    private CompositionType type;

    public ScimCompositeCondition() {
        super();
    }

    public ScimCompositeCondition(CompositionType type, ICondition... pConditions) {
        super();
        this.conditions = pConditions;
        this.type = type;
    }

    @Override
    public String toQueryString() throws IllegalQueryException {
        if (ArrayUtils.isEmpty(conditions)) {
            throw new IllegalQueryException("CompositeCondition contains no conditions!");
        }

        StringBuilder sb = new StringBuilder();
        CompositionType compType = getType() != null ? getType() : CompositionType.AND;
        //sb.append("(");
        Iterator<ICondition> iter = Arrays.stream(conditions).iterator();
        int i = 0;
        while (iter.hasNext()) {
            ICondition c = iter.next();
            if (c == null) {
                throw new IllegalQueryException("condition at index " + i + " is null!");
            }
            sb.append(c.toQueryString());
            if (iter.hasNext()) sb.append(" ").append(compType.getCharacter()).append(" ");
            i++;
        }
        //sb.append(")");
        return sb.toString();
    }

    @Getter
    public enum CompositionType {
        AND("and"), OR("or");

        private String character;

        private CompositionType(String character) {
            this.character = character;
        }
    }
}