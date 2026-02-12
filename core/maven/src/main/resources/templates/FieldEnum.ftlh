package ${basePackage}.metadata;

import utils.com.sap.Identifier;
import jakarta.annotation.Generated;

@Generated("com.sap.consulting.forms.maven.TemplateService")
public enum ${className}FieldsEnum {
<#list elements as item>
    ${item.definition.name}(${className}Fields.${item.definition.name})<#sep>,</#sep>
</#list>
    ;

    Identifier identifier;

    /**
     * @param identifier The identifier to be assigned to the enum
     */
    private ${className}FieldsEnum(final Identifier identifier) {
        this.identifier = identifier;
    }

    /**
     * @return The identifier object
     */
    public Identifier getIdentifier() {
        return this.identifier;
    }
}