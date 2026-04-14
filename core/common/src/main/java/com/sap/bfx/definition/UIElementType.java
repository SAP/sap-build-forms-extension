package com.sap.bfx.definition;

import com.sap.bfx.utils.Identifier;

public enum UIElementType implements Identifier {

    Alert(Constants.TYPE_ALERT),
    Attachment(Constants.TYPE_ATTACHMENT),
    AutoComplete(Constants.TYPE_AUTO_COMPLETE),
    Button(Constants.TYPE_BUTTON),
    Checkbox(Constants.TYPE_CHECKBOX),
    Currency(Constants.TYPE_CURRENCY),
    DateRangePicker(Constants.TYPE_DATE_RANGE_PICKER),
    Dialog(Constants.TYPE_DIALOG),
    DocForm(Constants.TYPE_DOC_FORMS),
    Dummy(Constants.TYPE_DUMMY),
    Form(Constants.TYPE_FORM),
    Group(Constants.TYPE_GROUP),
    Icon(Constants.TYPE_ICON),
    Image(Constants.TYPE_IMAGE),
    Link(Constants.TYPE_LINK),
    Mixin(Constants.TYPE_MIXIN),
    Input(Constants.TYPE_INPUT),
    MultiSelect(Constants.TYPE_MULTI_SELECT),
    Radio(Constants.TYPE_RADIO),
    SearchHelp(Constants.TYPE_SEARCH_HELP),
    Select(Constants.TYPE_SELECT),
    Segment(Constants.TYPE_SEGMENT),
    Table(Constants.TYPE_TABLE),
    Text(Constants.TYPE_TEXT),
    TextEdit(Constants.TYPE_TEXT_EDIT),
    Toolbar(Constants.TYPE_TOOLBAR),
    Wizard(Constants.TYPE_WIZARD);

    private final String identifier;

    UIElementType(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public java.lang.String getIdentifier() {
        return this.identifier;
    }

}
