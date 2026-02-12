package com.sap.bfx.valuehelp.model.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class HvSortAdapter extends XmlAdapter<String, List<String>> {

    @Override
    public String marshal(List<String> v) {
        StringBuilder r = new StringBuilder();
        for (String entry : v) {
            r.append(entry);
            r.append("|");
        }
        return r.length() == 0 ? null : toString().substring(0, toString().length() - 1);
    }

    @Override
    public List<String> unmarshal(String v) {
        return Arrays.stream(v.split("\\|")).collect(Collectors.toList());
    }

}
