package com.sap.bfx.valuehelp.model.xml;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.List;
import java.util.Map;

public class ValuesAdapter extends XmlAdapter<String, List<Map<String, String>>> {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final TypeReference<List<Map<String, String>>> TYPE = new TypeReference<>() {};

    @Override
    public String marshal(List<Map<String, String>> v) throws Exception {
        if (v == null) return null;
        return MAPPER.writeValueAsString(v);
    }

    @Override
    public List<Map<String, String>> unmarshal(String v) throws Exception {
        if (v == null || v.isBlank()) return List.of();
        return MAPPER.readValue(v, TYPE);
    }
}
