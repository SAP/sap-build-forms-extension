package com.sap.bfx.p13n.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.Locale;

@Data
public class Value {
    String id;
    Locale locale;
    ArrayList<String> values;
}