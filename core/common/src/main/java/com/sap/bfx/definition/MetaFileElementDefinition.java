package com.sap.bfx.definition;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.Strings;

@EqualsAndHashCode(callSuper = true)
@Data
public class MetaFileElementDefinition extends ElementDefinition {
    private String path;
    private String mixinName;
    private int version;

    public MetaFileElementDefinition() {
        super(UIElementType.Mixin);
    }

    public String getKindCode() {
        if (isKindClasspath()) {
            return "cp";
        } else if (isKindFile()) {
            return "f";
        }
        return "";
    }

    public boolean isKindClasspath() {
        return Strings.CI.startsWith(path, "classpath:");
    }

    public boolean isKindFile() {
        return Strings.CI.startsWith(path, "file:");
    }
}
