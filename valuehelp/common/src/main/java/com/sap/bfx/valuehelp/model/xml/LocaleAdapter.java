package com.sap.bfx.valuehelp.model.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.util.Locale;

public class LocaleAdapter extends XmlAdapter<String, Locale> {

    @Override
    public String marshal(Locale l) {
        return l.toString();
    }

    @Override
    public Locale unmarshal(String l) {
        if (l != null) {
            return new Locale(l);
        } else {
            return new Locale("_");
        }

    }
}
