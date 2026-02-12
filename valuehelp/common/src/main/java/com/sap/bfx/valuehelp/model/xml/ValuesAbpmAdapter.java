package com.sap.bfx.valuehelp.model.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ValuesAbpmAdapter extends XmlAdapter<String, List<String>> {

    @Override
    public String marshal(List<String> v) {
        StringBuilder r = new StringBuilder();
        for (String entry : v) {
            r.append(URLEncoder.encode(entry, StandardCharsets.UTF_8));
            r.append("|");
        }
        return r.length() == 0 ? null : toString().substring(0, toString().length() - 1);
    }

    @Override
    public List<String> unmarshal(String v) {
        return Arrays.stream(v.split("\\|"))
                .map(s -> URLDecoder.decode(s, StandardCharsets.UTF_8))
                .collect(Collectors.toList());
    }
}
