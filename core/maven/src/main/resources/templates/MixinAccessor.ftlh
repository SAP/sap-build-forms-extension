package ${basePackage}.metadata;

import com.sap.bfx.utils.AbstractBaseIdentifier;
import jakarta.annotation.Generated;

@Generated("com.sap.bfx.maven.TemplateService")
public final class ${className} extends AbstractBaseIdentifier {
<#list elements as item>
    public final static ${className} ${item.key} = new ${className}("${item.value}");
</#list>

    private ${className}(String identifier) {
        super(identifier);
    }
}






