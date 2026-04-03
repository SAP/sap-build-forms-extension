package ${basePackage}.metadata;

import com.sap.bfx.callback.AccessClass;
import com.sap.bfx.callback.AccessClassFactory;
import com.sap.bfx.callback.DataApi;
import com.sap.bfx.callback.DataApiImpl;
import com.sap.bfx.session.ElementRow;
import com.sap.bfx.session.Form;
import com.sap.bfx.definition.ScenarioDefinition;
import jakarta.annotation.Generated;
import org.springframework.stereotype.Service;

@Generated("com.sap.bfx.maven.TemplateService")
@Service
public class AccessClassFactoryImpl implements AccessClassFactory {

    public <AC extends AccessClass> AC createAccessClass(final ScenarioDefinition sd, final Form form) {
        <#list classes?keys as key>
            if (sd.getVersion() == ${classes[key]["version"]}) {
                return (AC) new ${classes[key]["basePackage"]}.metadata.${classes[key]["className"]}${classes[key]["version"]}(form, ElementRow.ROOT);
            }
        </#list>

        throw new RuntimeException("AccessClassFactory, unhandled version " + sd.getVersion());
    }

    public DataApi createDataApi(final ScenarioDefinition sd, final Form form) {
        <#list classes?keys as key>
            if (sd.getVersion() == ${classes[key]["version"]}) {
                return new DataApiImpl(form, Mixin${classes[key]["className"]}${classes[key]["version"]}Mapping.MAP);
            }
        </#list>

        throw new RuntimeException("AccessClassFactory, unhandled version " + sd.getVersion());
    }
}