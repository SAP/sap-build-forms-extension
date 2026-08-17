package com.sap.bfx.valuehelp.adapter;

import com.sap.bfx.valuehelp.model.ValueHelp;
import com.sap.bfx.valuehelp.model.ValueHelpDef;
import com.sap.bfx.valuehelp.service.ValueHelpService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
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
     *
     * @param vdh
     * @param locale
     * @return
     */
    @Override
    public ValueHelp query(ValueHelpDef vdh, Locale locale) {
        ValueHelp newValueHelp = new ValueHelp();
        newValueHelp.setId(vdh.getId());
        newValueHelp.setLocale(locale);

        Optional<ValueHelp> existingValueHelp =
                service.findValueLatestVersionByIdLocale(vdh.getId(), locale.toString());
        if (existingValueHelp.isPresent()) {
            newValueHelp.setVersion(existingValueHelp.get().getVersion());
        } else {
            newValueHelp.setVersion(0);
        }
        newValueHelp.setValues(getValues(locale));
        return newValueHelp;
    }

    /**
     * This method needs to be implemented by the concrete adapter. It should return a list of maps, where each map
     * represents a row of the value help and contains key-value pairs for the columns defined in the ValueHelpDef.
     *
     * @param locale locale/language that is requested
     * @return valuehle as list of maps, where each map represents a row of the value help and contains key-value
     * pairs for the columns defined in the ValueHelpDef
     */
    protected abstract List<Map<String, String>> getValues(Locale locale);

}
