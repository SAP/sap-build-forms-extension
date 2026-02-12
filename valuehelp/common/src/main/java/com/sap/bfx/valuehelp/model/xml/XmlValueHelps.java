package com.sap.bfx.valuehelp.model.xml;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "helpValueDefinitions")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlValueHelps {

    @XmlElement(name = "valueHelpDef")
    @NotNull(message = "Value help defs cannot be null")
    @Size(min = 1, message = "Value help defs must contain at least one element")
    private List<XmlValueHelpDef> valueHelpDefs;

}