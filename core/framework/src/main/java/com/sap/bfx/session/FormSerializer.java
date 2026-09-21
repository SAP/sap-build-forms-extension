package com.sap.bfx.session;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.sap.bfx.definition.DateRange;
import com.sap.bfx.definition.ElementDefinition;
import com.sap.bfx.definition.LinkData;
import com.sap.bfx.definition.ScenarioDefinition;
import com.sap.bfx.exception.FormsCoreException;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * Custom serializer for Form
 */
public class FormSerializer extends StdSerializer<Form> {

    /**
     * Constructor
     */
    public FormSerializer() {
        super(Form.class);
    }

    /**
     * Constructor
     *
     * @param value       Value to serialize; can <b>not</b> be null.
     * @param gen         Generator used to output resulting Json content
     * @param serializers Provider that can be used to get serializers for
     *                    serializing Objects value contains, if any.
     * @param typeSer     Type serializer to use for including type information
     * @throws IOException
     */
    @Override
    public void serializeWithType(Form value, JsonGenerator gen, SerializerProvider serializers,
                                  TypeSerializer typeSer) throws IOException {
        serialize(value, gen, serializers);
    }

    /**
     * Serialize Form
     *
     * @param value    Value to serialize; can <b>not</b> be null.
     * @param gen      Generator used to output resulting Json content
     * @param provider Provider that can be used to get serializers for
     *                 serializing Objects value contains, if any.
     * @throws IOException
     */
    @Override
    public void serialize(Form value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        // serialize the form attributes
        if (StringUtils.isNotBlank(value.getId())) {
            gen.writeStringField(FormUtils.NM_ID, value.getId());
        }
        gen.writeNumberField(FormUtils.NM_FORM_VERSION, value.getVersion());
        if (StringUtils.isNotBlank(value.getRefId())) {
            gen.writeStringField(FormUtils.NM_REF, value.getRefId());
        }
        gen.writeStringField(FormUtils.NM_DEF_NAME, value.getScenarioName());
        gen.writeNumberField(FormUtils.NM_DEF_VERSION, value.getScenarioVersion());
        if (StringUtils.isNotBlank(value.getTemplateName())) {
            gen.writeStringField(FormUtils.NM_TEMPLATE, value.getTemplateName());
        }
        if (StringUtils.isNotBlank(value.getWorkflowAdapter())) {
            gen.writeStringField(FormUtils.NM_WORKFLOW_ADAPTER, value.getWorkflowAdapter());
        }
        if (StringUtils.isNotBlank(value.getChangedBy())) {
            gen.writeStringField(FormUtils.NM_CHANGED_BY, value.getChangedBy());
        }
        if (value.getChangedAt() != null) {
            gen.writeNumberField(FormUtils.NM_CHANGED_AT, value.getChangedAt().toEpochMilli());
        }
        if (value.getDescription() != null) {
            gen.writeStringField(FormUtils.NM_DESCRIPTION, value.getDescription());
        }
        if (value.getFinishedAt() != null) {
            gen.writeNumberField(FormUtils.NM_FINISHED_AT, value.getFinishedAt().toEpochMilli());
        }
        if (StringUtils.isNotBlank(value.getFunctionalId())) {
            gen.writeStringField(FormUtils.NM_FUNCTIONAL_ID, value.getFunctionalId());
        }
        if (StringUtils.isNotBlank(value.getStartedBy())) {
            gen.writeStringField(FormUtils.NM_CREATED_BY, value.getStartedBy());
        }
        if (value.getStartedAt() != null) {
            gen.writeNumberField(FormUtils.NM_CREATED_AT, value.getStartedAt().toEpochMilli());
        }
        if (value.getState() != null) {
            gen.writeStringField(FormUtils.NM_STATE, value.getState().name());
        }
        if (value.getDetailState() != null) {
            gen.writeStringField(FormUtils.NM_DETAIL_STATE, value.getDetailState());
        }

        // the data that is stored in rows (root itself is also a row)
        this.serializeElementRow(value.getSd(), value, gen, provider);
        gen.writeEndObject();

    }

    /**
     * Serialize an ElementRow
     *
     * @param sd
     * @param value
     * @param gen
     * @param provider
     * @throws IOException
     */
    private void serializeElementRow(ScenarioDefinition sd, ElementRow value, JsonGenerator gen,
                                     SerializerProvider provider) throws IOException {

        gen.writeStringField(FormUtils.NM_ROW_ID, value.getRowId());
        gen.writeBooleanField(FormUtils.NM_SELECTED, value.isSelected());

        gen.writeFieldName(FormUtils.NM_ELEMENTS);
        gen.writeStartArray();
        for (var it : value.getElements().values()) {
            gen.writeStartObject();
            gen.writeStringField(FormUtils.NM_KEY, it.getKey());
            gen.writeStringField(FormUtils.NM_NAME, it.getName());
            gen.writeBooleanField(FormUtils.NM_VISIBLE, it.isVisible());
            gen.writeBooleanField(FormUtils.NM_EDITABLE, it.isEditable());
            gen.writeBooleanField(FormUtils.NM_REQUIRED, it.isRequired());
            if (it.getMessage() != null) {
                gen.writeFieldName(FormUtils.NM_MESSAGE);
                gen.writeStartObject();
                gen.writeStringField(FormUtils.NM_SEVERITY, it.getMessage().getSeverity().getIdentifier());
                gen.writeStringField(FormUtils.NM_KEY, it.getMessage().getKey());
                if (it.getMessage().getParams() != null && !it.getMessage().getParams().isEmpty()) {
                    gen.writeObjectFieldStart(FormUtils.NM_PARAMS);
                    for (var entry : it.getMessage().getParams().entrySet()) {
                        gen.writeStringField(entry.getKey(), String.valueOf(entry.getValue()));
                    }
                    gen.writeEndObject();
                }
                gen.writeEndObject();
            }

            gen.writeFieldName(FormUtils.NM_VALUE);
            if (it.getValue() == null) {
                gen.writeObject(null);
            } else {
                final var ed = sd.findElementByKey(it.getKey());
                final var dt = ElementDefinition.getDataTypeClass(ed);
                if (dt == Integer.class) {
                    gen.writeNumber((Integer) it.getValue());
                } else if (dt == LocalDate.class) {
                    gen.writeString(((LocalDate) it.getValue()).toString());
                } else if (dt == LocalDateTime.class) {
                    gen.writeString(((LocalDateTime) it.getValue()).toString());
                } else if (dt == LocalTime.class) {
                    gen.writeString(((LocalTime) it.getValue()).toString());
                } else if (dt == BigDecimal.class) {
                    gen.writeNumber(it.getValue().toString());
                } else if (dt == String.class) {
                    gen.writeString((String) it.getValue());
                } else if (dt == Boolean.class) {
                    gen.writeBoolean((Boolean) it.getValue());
                } else if (dt == Table.class) {
                    final var table = (Table) it.getValue();
                    gen.writeStartObject();
                    if (StringUtils.isNotBlank(table.getSortField())) {
                        gen.writeStringField("sf", table.getSortField());
                        gen.writeStringField("sd", Table.toCode(table.getSortOrder()));
                    }
                    gen.writeArrayFieldStart("r");
                    for (var rowId : table.getRows()) {
                        gen.writeString(rowId);
                    }
                    gen.writeEndArray();
                    gen.writeObjectFieldStart("d");
                    for (var rowId : table.getData().keySet()) {
                        gen.writeObjectFieldStart(rowId);
                        this.serializeElementRow(sd, table.getData().get(rowId), gen, provider);
                        gen.writeEndObject();
                    }
                    gen.writeEndObject();
                    gen.writeNumberField("p", table.getPos());
                    gen.writeNumberField("ps", table.getPageSize());
                    gen.writeEndObject();
                } else if (dt == Attachments.class) {
                    gen.writeStartArray();
                    for (var itAtt : (Attachments) it.getValue()) {
                        gen.writeStartObject();
                        gen.writeStringField(FormUtils.NM_ID, itAtt.getId());
                        gen.writeNumberField(FormUtils.NM_POS, itAtt.getPos());
                        gen.writeStringField(FormUtils.NM_NAME, itAtt.getFileName());
                        gen.writeStringField(FormUtils.NM_CONTENT_TYPE, itAtt.getContentType());
                        gen.writeNumberField(FormUtils.NM_SIZE, itAtt.getSize());
                        gen.writeStringField(FormUtils.NM_REF, itAtt.getRef());
                        if (StringUtils.isNotBlank(itAtt.getCategory())) {
                            gen.writeStringField(FormUtils.NM_CATEGORY, itAtt.getCategory());
                        }
                        if (StringUtils.isNotBlank(itAtt.getDescription())) {
                            gen.writeStringField(FormUtils.NM_DESCRIPTION, itAtt.getDescription());
                        }
                        gen.writeEndObject();

                    }
                    gen.writeEndArray();
                } else if (dt == DateRange.class) {
                    gen.writeStartObject();
                    gen.writeStringField(FormUtils.NM_DATERANGE_FROM, ((DateRange) it.getValue()).getFrom().toString());
                    gen.writeStringField(FormUtils.NM_DATERANGE_TO, ((DateRange) it.getValue()).getTo().toString());
                    gen.writeEndObject();
                } else if (dt == LinkData.class) {
                    gen.writeStartObject();
                    gen.writeStringField(FormUtils.NM_LINK_TEXT, ((LinkData) it.getValue()).getText());
                    gen.writeStringField(FormUtils.NM_LINK_HREF, ((LinkData) it.getValue()).getHRef());
                    gen.writeEndObject();
                } else if (dt == DocFormData.class) {
                    gen.writeStartObject();
                    gen.writeStringField(FormUtils.NM_SELECTED, ((DocFormData) it.getValue()).getSelectedTab());
                    gen.writeStringField(FormUtils.NM_DOC_URL, ((DocFormData) it.getValue()).getDocUrl());
                    gen.writeEndObject();
                } else if (dt == MoneyAmount.class) {
                    gen.writeStartObject();
                    gen.writeStringField(FormUtils.NM_CURRENCY, ((MoneyAmount) it.getValue()).getCurrency());
                    gen.writeNumberField(FormUtils.NM_AMOUNT, ((MoneyAmount) it.getValue()).getAmount());
                    gen.writeEndObject();
                }
                else {
                    throw new FormsCoreException("Unhandled type " + dt.getName() + " in FormSerializer");
                }
            }
            gen.writeEndObject();
        }
        gen.writeEndArray();
    }
}
