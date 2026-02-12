package ${basePackage}.metadata;

import org.springframework.stereotype.Service;

import callback.forms.com.sap.AccessClass;
import callback.forms.com.sap.AccessClassFactory;
import callback.forms.com.sap.DataApi;
import callback.forms.com.sap.DataApiImpl;
import session.forms.com.sap.ElementRow;
import session.forms.com.sap.Form;
import definition.forms.com.sap.ScenarioDefinition;
import jakarta.annotation.Generated;

@Generated("com.sap.consulting.forms.maven.TemplateService")
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