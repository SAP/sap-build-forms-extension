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

import java.util.ArrayList;
import java.util.List;

import static com.sap.bfx.valuehelp.model.ValueHelpDef.TTL_STATIC;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "valueHelpDef")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlValueHelpDef {

    @XmlElement
    @NotNull(message = "ID cannot be null")
    @NotBlank(message = "ID is mandatory")
    private String id;

    @XmlElement
    @NotNull(message = "Time to live cannot be null")
    @Min(value = TTL_STATIC, message = "Time to live should not be less than " + TTL_STATIC)
    private Long ttl;

    @XmlElement
    @NotNull(message = "Adapter cannot be null")
    @NotBlank(message = "Adapter is mandatory")
    private String adapter;

    @XmlElement
    @NotNull(message = "Config cannot be null")
    private String config;

    @XmlElement
    private String description;

    @XmlElement
    private ArrayList<String> languages;

    @XmlElement
    private String keyKey;

    @XmlElement
    private List<String> valueKeys;

    @XmlElement
    private String formatTemplate;

    @XmlElement
    private String valueHelpType;

    @XmlElement(name = "valueHelp")
    private List<XmlValueHelpValue> valueHelpValues;
}