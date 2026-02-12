package com.sap.bfx.valuehelp.model.xml;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Data
@AllArgsConstructor
@NoArgsConstructor
@XmlRootElement(name = "HelpValue")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlAbpmValueHelpValue {

    @XmlElement(name = "id", required = true)
    @NotNull(message = "ID is mandatory")
    @NotBlank(message = "ID is mandatory")
    private String id;

    @XmlElement(name = "languageCode", required = true)
    @XmlJavaTypeAdapter(LocaleAdapter.class)
    @NotNull(message = "Locale cannot be null")
    private Locale locale = new Locale("_");

    @XmlElement(name = "selection", required = true)
    @NotNull(message = "Selection cannot be null")
    private int selection;

    @XmlElement(name = "keyKey", required = true)
    private String keyKey;

    @XmlElement(name = "valueKey", required = true)
    private String valueKey;

    @XmlElement(name = "valueCollection", required = true)
    @XmlJavaTypeAdapter(ValuesAbpmAdapter.class)
    @NotNull(message = "Values cannot be null")
    private List<String> values = new ArrayList<>();

    @XmlElement(name = "hvSort", required = true)
    @XmlJavaTypeAdapter(HvSortAdapter.class)
    @NotNull(message = "Hv sort cannot be null")
    private List<String> hvSort = new ArrayList<>();
}
