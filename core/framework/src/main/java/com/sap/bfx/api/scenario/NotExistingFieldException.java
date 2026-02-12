package com.sap.bfx.api.scenario;

import com.sap.bfx.exception.BadRequestException;
import lombok.Getter;
import lombok.Setter;

public class NotExistingFieldException extends BadRequestException {

    @Getter
    @Setter
    private String scenarioFieldName;

    public NotExistingFieldException(String message) {
        super(message);
    }

    public NotExistingFieldException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotExistingFieldException(Throwable cause) {
        super(cause);
    }

    public NotExistingFieldException(String message, String scenarioFieldName) {
        super(message);
        this.scenarioFieldName = scenarioFieldName;
    }

    public NotExistingFieldException(String message, Throwable cause, String scenarioFieldName) {
        super(message, cause);
        this.scenarioFieldName = scenarioFieldName;
    }

    public NotExistingFieldException(Throwable cause, String scenarioFieldName) {
        super(cause);
        this.scenarioFieldName = scenarioFieldName;
    }
}
