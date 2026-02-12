package com.sap.bfx.valuehelp.model.xml;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import static com.sap.bfx.valuehelp.model.ValueHelpDef.TTL_STATIC;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "HelpValueDefinition")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlAbpmValueHelpDef {

    @XmlElement(name = "id", required = true)
    @NotNull(message = "ID cannot be null")
    @NotBlank(message = "ID is mandatory")
    private String id;

    @XmlElement(name = "timeToLive", required = true)
    @NotNull(message = "Time to live cannot be null")
    @Min(value = TTL_STATIC, message = "Time to live should not be less than " + TTL_STATIC)
    private Long ttl;

    @XmlElement(name = "system", required = true)
    @NotNull(message = "Adapter cannot be null")
    @NotBlank(message = "Adapter is mandatory")
    private String adapter;

    @XmlElement(name = "technicalName")
    private String description;

    @XmlElement(name = "HelpValues", required = true)
    private List<XmlAbpmValueHelpValue> valueHelpValues;

    @XmlElement(name = "keyKey", required = true)
    private String keyKey;

    @XmlElement(name = "valueKey", required = true)
    private String valueKey;

}
