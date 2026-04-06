package ${basePackage}.metadata;

import com.sap.bfx.utils.Identifier;
import jakarta.annotation.Generated;

@Generated("com.sap.bfx.maven.TemplateService")
public enum ${className}Enum {
<#list elements as item>
    ${item.key}(${className}.${item.key})<#sep>,</#sep>
</#list>
    ;

    Identifier identifier;

    /**
     * @param identifier The identifier to be assigned to the enum
     */
    private ${className}Enum(final Identifier identifier) {
        this.identifier = identifier;
    }

    /**
     * @return The identifier object
     */
    public Identifier getIdentifier() {
        return this.identifier;
    }
}