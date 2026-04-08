package com.sap.bfx.valuehelp.model.xml;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "valueHelp")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlValueHelpValue {

    @XmlElement
    @NotNull(message = "ID is mandatory")
    @NotBlank(message = "ID is mandatory")
    private String id;

    @XmlElement
    @NotNull(message = "Version cannot be null")
    @PositiveOrZero(message = "Version must be greater than or equal 0")
    private long version;

    @XmlElement
    @XmlJavaTypeAdapter(LocaleAdapter.class)
    @NotNull(message = "Locale cannot be null")
    private Locale locale;

    @XmlElement
    @XmlJavaTypeAdapter(DateAdapter.class)
    @NotNull(message = "Valid until cannot be null")
    private Date validUntil;

    @XmlElement
    @NotNull(message = "Values cannot be null")
    private List<Map<String, String>> values;
}