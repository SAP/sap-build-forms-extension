package com.sap.bfx.valuehelp.model.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateAdapter extends XmlAdapter<String, Date> {

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");

    @Override
    public String marshal(Date v) {
        return dateFormat.format(v);
    }

    @Override
    public Date unmarshal(String v) throws Exception {
        return dateFormat.parse(v);
    }
}
