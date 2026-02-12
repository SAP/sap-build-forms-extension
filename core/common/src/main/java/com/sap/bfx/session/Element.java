package com.sap.bfx.session;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sap.bfx.definition.Message;
import com.sap.bfx.definition.Severity;
import lombok.Data;

@Data
public class Element {
    private String key;
    @JsonProperty("nm")
    private String name;
    @JsonProperty("va")
    private Object value;
    @JsonProperty("vi")
    private boolean visible;
    @JsonProperty("ed")
    private boolean editable;
    @JsonProperty("rq")
    private boolean required;
    @JsonProperty("msg")
    private Message message;

    /**
     *
     */
    public void clearMessage() {
        message = null;
    }

    /**
     * @return
     */
    public boolean hasError() {
        return message != null && message.getSeverity() == Severity.Error;
    }
}
