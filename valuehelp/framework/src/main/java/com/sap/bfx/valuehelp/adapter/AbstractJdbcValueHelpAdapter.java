package com.sap.bfx.valuehelp.adapter;

import com.sap.bfx.valuehelp.model.ValueHelp;
import com.sap.bfx.valuehelp.model.ValueHelpDef;
import com.sap.bfx.valuehelp.service.ValueHelpService;

import java.util.HashMap;
import java.util.Locale;
import java.util.Optional;

public abstract class AbstractJdbcValueHelpAdapter implements ValueHelpAdapter {

    protected final ValueHelpService service;

    protected AbstractJdbcValueHelpAdapter(ValueHelpService service) {
        this.service = service;
    }

    /**
     * @param vhd
     * @return
     */
    @Override
    public boolean check(ValueHelpDef vhd) {
        return false;
    }

    /**
     * @param vdh
     * @param locale
     * @return
     */
    @Override
    public ValueHelp query(ValueHelpDef vdh, Locale locale) {
        ValueHelp newValueHelp = new ValueHelp();
        newValueHelp.setId(vdh.getId());
        newValueHelp.setLocale(locale);

        Optional<ValueHelp> existingValueHelp = service.findValueLatestVersionByIdLocale(vdh.getId(), locale.toString());
        if (existingValueHelp.isPresent()) {
            newValueHelp.setVersion(existingValueHelp.get().getVersion());
        } else {
            newValueHelp.setVersion(0);
        }
        newValueHelp.setValues(getValues(locale));
        return newValueHelp;
    }

    protected abstract HashMap<String, String> getValues(Locale locale);

}
