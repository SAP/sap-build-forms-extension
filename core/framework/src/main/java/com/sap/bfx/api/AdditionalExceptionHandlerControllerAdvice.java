package com.sap.bfx.api;

import com.sap.bfx.api.scenario.NotExistingFieldException;
import com.sap.bfx.api.scenario.ScenarioController;
import com.sap.bfx.api.scenario.json.FieldResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@ControllerAdvice
@Slf4j
public class AdditionalExceptionHandlerControllerAdvice {

    /**
     * Handles NotExistingFieldException and returns a structured response.
     *
     * @param notExistingFieldException the NotExistingFieldException to handle
     * @return a ResponseEntity containing a FieldResponse with details about the exception
     */
    @ExceptionHandler(NotExistingFieldException.class)
    public ResponseEntity<FieldResponse<String>> handleNotExistingFieldException(
            NotExistingFieldException notExistingFieldException) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .cacheControl(CacheControl.noCache())
                .body(new FieldResponse<>((null != notExistingFieldException.getScenarioFieldName())
                        ? notExistingFieldException.getScenarioFieldName()
                        : ScenarioController.SCENARIO_FIELD_NAME,
                        (null != notExistingFieldException.getMessage())
                                ? notExistingFieldException.getMessage()
                                : ScenarioController.N_A));
    }
}
