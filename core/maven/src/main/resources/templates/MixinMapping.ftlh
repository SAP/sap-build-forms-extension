package ${basePackage}.metadata;

import java.util.HashMap;
import java.util.Map;

import com.sap.bfx.utils.Identifier;
import jakarta.annotation.Generated;

@Generated("com.sap.bfx.maven.TemplateService")
public class ${className} {
    public final static Map<Identifier, Map<Identifier, Identifier>> MAP = new HashMap<>();

    static {
        <#list elements as key, items>
        {
            final var m = new HashMap<Identifier, Identifier>();
            <#list items as item>
            m.put(${item.key}, ${item.value});
            </#list>
            MAP.put(${key}, m);
        }
        </#list>
    }
}
