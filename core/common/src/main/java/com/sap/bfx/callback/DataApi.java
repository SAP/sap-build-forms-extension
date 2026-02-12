package com.sap.bfx.callback;

import com.sap.bfx.definition.Message;
import com.sap.bfx.definition.Severity;
import com.sap.bfx.session.Position;
import com.sap.bfx.utils.Identifier;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public interface DataApi extends AccessClass {

    //-----------------------------------------------------------------------------------------------------------------
    // Iterators etc.

    void forEach(ElementVisitor visitor, Context<? extends AccessClass> ctx);

    //-----------------------------------------------------------------------------------------------------------------
    // Table handling

    String addRow(final Identifier id, final Context<? extends AccessClass> ctx);

    String addRow(final String rowId, final Identifier id, final Context<? extends AccessClass> ctx);

    void deleteRow(final String rowId);

    void deleteAll(final String rowId, final Identifier id);

    void deleteAll(final Identifier id);

    void filter(final Identifier id, final Predicate<String> predicate);

    void filter(final String rowId, final Identifier id, final Predicate<String> predicate);

    List<String> getRows(final String rowId, final Identifier id);

    List<String> getRows(final Identifier id);

    boolean isSelected(final String rowId);

    void setSelected(final String rowId, final boolean value);

    List<String> getSelected(final String rowId, final Identifier id);

    List<String> getSelected(final Identifier id);

    void forEach(final String rowId, final Identifier id, RowVisitor visitor, Context<? extends AccessClass> ctx);

    void forEach(final Identifier id, RowVisitor visitor, Context<? extends AccessClass> ctx);

    int getPage(final String rowId, final Identifier id);

    int getPageSize(final String rowId, final Identifier id);

    void setPage(final String rowId, final Identifier id, final int page);

    void setPageSize(final String rowId, final Identifier id, final int pageSize);

    //-----------------------------------------------------------------------------------------------------------------
    // Getting values

    Optional<Object> getOptVal(final String rowId, final Identifier id);

    Optional<Object> getOptVal(final Identifier id);

    Optional<Object> getOptVal(final String rowId, final String key);

    <T> Optional<T> getOptVal(final String rowId, final Identifier id, final Class<T> clz);

    <T> Optional<T> getOptVal(final Identifier id, final Class<T> clz);

    <T> Optional<T> getOptVal(final String rowId, final Identifier prefixId, final Identifier id, final Class<T> clz);

    <T> Optional<T> getOptVal(final Identifier prefixId, final Identifier id, final Class<T> clz);

    Optional<Object> getOptVal(final String rowId, final Identifier prefixId, final Identifier id);

    Optional<Object> getOptVal(final Identifier prefixId, final Identifier id);

    Object getValue(final String rowId, final Identifier id);

    Object getValue(final Identifier id);

    Object getValue(final String rowId, final String key);

    Object getValue(final String rowId, final Identifier prefixId, final Identifier id);

    Object getValue(final Identifier prefixId, final Identifier id);

    <T> T getValue(final String rowId, final Identifier id, final Class<T> clz);

    <T> T getValue(final Identifier id, final Class<T> clz);

    <T> T getValue(final String rowId, final Identifier prefixId, final Identifier id, final Class<T> clz);

    <T> T getValue(final Identifier prefixId, final Identifier id, final Class<T> clz);

    //-----------------------------------------------------------------------------------------------------------------
    // Setting values

    void setValue(final String rowId, final Identifier id, final Object value);

    void setValue(final Identifier id, final Object value);

    void setValue(final String rowId, final Identifier prefixId, final Identifier id, final Object value);

    void setValue(final Identifier prefixId, final Identifier id, final Object value);

    //-----------------------------------------------------------------------------------------------------------------
    // Visual attributes

    boolean isRequired(final String rowId, final String key);

    boolean isRequired(final String rowId, final Identifier id);

    boolean isRequired(final String rowId, final Identifier prefixId, final Identifier id);

    boolean isRequired(final Identifier id);

    boolean isRequired(final Identifier prefixId, final Identifier id);

    boolean isVisible(final String rowId, final Identifier id);

    boolean isVisible(final String rowId, final Identifier prefixId, final Identifier id);

    boolean isVisible(final Identifier id);

    boolean isVisible(final Identifier prefixId, final Identifier id);

    boolean isEditable(final String rowId, final Identifier id);

    boolean isEditable(final String rowId, final Identifier prefixId, final Identifier id);

    boolean isEditable(final Identifier id);

    boolean isEditable(final Identifier prefixId, final Identifier id);

    void setRequired(final String rowId, final Identifier id, final boolean value);

    void setRequired(final String rowId, final Identifier prefixId, final Identifier id, final boolean value);

    void setRequired(final Identifier id, final boolean value);

    void setRequired(final Identifier prefixId, final Identifier id, final boolean value);

    void setVisible(final String rowId, final Identifier id, final boolean value);

    void setVisible(final String rowId, final Identifier prefixId, final Identifier id, final boolean value);

    void setVisible(final Identifier prefixId, final Identifier id, final boolean value);

    void setVisible(final Identifier id, final boolean value);

    void setVisibleCascading(final String rowId, final Identifier id, final boolean value);

    void setVisibleCascading(final Identifier id, final boolean value);

    void setVisibleCascading(final Identifier prefixId, final String rowId, final Identifier id, final boolean value);

    void setVisibleCascading(final Identifier prefixId, final Identifier id, final boolean value);

    void setEditable(final String rowId, final Identifier id, final boolean value);

    void setEditable(final String rowId, final Identifier prefixId, final Identifier id, final boolean value);

    void setEditable(final Identifier prefixId, final Identifier id, final boolean value);

    void setEditable(final Identifier id, final boolean value);

    //-----------------------------------------------------------------------------------------------------------------
    // Messages

    void setMessage(final String rowId, final String key, final Message message);

    void setMessage(final String rowId, final Identifier id, final Message message);

    void setMessage(final String rowId, final Identifier prefixId, final Identifier id, final Message message);

    void setMessage(final Identifier prefixId, final Identifier id, final Message message);

    void setMessage(final Identifier id, final Message message);

    Optional<Message> getMessage(final String rowId, final Identifier id);

    Optional<Message> getMessage(final String rowId, final Identifier prefixId, final Identifier id);

    Optional<Message> getMessage(final Identifier prefixId, final Identifier id);

    Optional<Message> getMessage(final Identifier id);

    Collection<Position> getFieldsWithMessage(final String rowId, final Severity minSeverity);

    boolean hasMessages(final String rowId, final Severity minSeverity);

    boolean hasMessages(final Severity minSeverity);

    boolean hasErrors();

    boolean hasWarningsOrErrors();
}
