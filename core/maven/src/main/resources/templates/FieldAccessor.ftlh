package ${basePackage}.metadata;

import com.sap.bfx.utils.AbstractBaseIdentifier;
import jakarta.annotation.Generated;

@Generated("com.sap.bfx.maven.TemplateService")
public final class ${className}Fields extends AbstractBaseIdentifier {
<#list elements as item>
    public final static ${className}Fields ${item.definition.name} = new ${className}Fields("${item.definition.key}");
</#list>
<#if isRoot>
    public final static ${className}Fields Root = new ${className}Fields("${rootKey}");
</#if>

    private ${className}Fields(String identifier) {
        super(identifier);
    }
}