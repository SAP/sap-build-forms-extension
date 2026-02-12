package ${basePackage}.metadata;

import utils.com.sap.AbstractBaseIdentifier;
import jakarta.annotation.Generated;

@Generated("com.sap.consulting.forms.maven.TemplateService")
public final class ${className} extends AbstractBaseIdentifier {
<#list elements as item>
    public final static ${className} ${item.key} = new ${className}("${item.value}");
</#list>

    private ${className}(String identifier) {
        super(identifier);
    }
}






