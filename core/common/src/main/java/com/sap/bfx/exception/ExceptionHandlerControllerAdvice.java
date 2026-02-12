package com.sap.bfx.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
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
public class ExceptionHandlerControllerAdvice {

    /**
     * Handles generic Exception and returns a structured response.
     *
     * @param e the Exception to handle
     * @return a ResponseEntity containing an ExceptionInfo with details about the exception
     */
    @ExceptionHandler(Throwable.class)
    public ResponseEntity<ExceptionInfo> handleGenericException(Exception e) {
        log.error("An unexpected error occurred: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                new ExceptionInfo(HttpStatus.INTERNAL_SERVER_ERROR.value(), "An unexpected error occurred", "N/A", "N/A"));
    }

    /**
     * Handles FormsCoreException and returns a structured response.
     *
     * @param e the FormsCoreException to handle
     * @return a ResponseEntity containing an ExceptionInfo with details about the exception
     */
    @ExceptionHandler(FormsCoreException.class)
    public ResponseEntity<ExceptionInfo> handleFormsCoreException(FormsCoreException e) {
        logException(e);
        return createResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR, e);
    }

    /**
     * Handles NotFoundException and returns a structured response.
     *
     * @param e the NotFoundException to handle
     * @return a ResponseEntity containing an ExceptionInfo with details about the exception
     */
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ExceptionInfo> handleNotFoundException(NotFoundException e) {
        logException(e);
        return createResponseEntity(HttpStatus.NOT_FOUND, e);
    }

    /**
     * Handles NotAuthorizedException and returns a structured response.
     *
     * @param e the NotAuthorizedException to handle
     * @return a ResponseEntity containing an ExceptionInfo with details about the exception
     */
    @ExceptionHandler(NotAuthorizedException.class)
    public ResponseEntity<ExceptionInfo> handleNotAuthorizedException(FormsCoreException e) {
        logException(e);
        return createResponseEntity(HttpStatus.UNAUTHORIZED, e);
    }

    /**
     * Handles BadRequestException and returns a structured response.
     *
     * @param e the BadRequestException to handle
     * @return a ResponseEntity containing an ExceptionInfo with details about the exception
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ExceptionInfo> handleBadRequestException(FormsCoreException e) {
        logException(e);
        return createResponseEntity(HttpStatus.BAD_REQUEST, e);
    }

    /**
     * Creates a ResponseEntity with the given status and exception details.
     *
     * @param status the HTTP status to set in the response
     * @param e      the FormsCoreException containing details about the error
     * @return a ResponseEntity containing an ExceptionInfo with details about the exception
     */
    private ResponseEntity<ExceptionInfo> createResponseEntity(HttpStatus status, FormsCoreException e) {
        return ResponseEntity.status(status)
                .cacheControl(CacheControl.noCache())
                .body(new ExceptionInfo(status.value(), e.getMessage(), e.getId(), e.getUser()));
    }

    /**
     * Logs the exception details.
     *
     * @param e the FormsCoreException to log
     */
    private void logException(FormsCoreException e) {
        log.error("guid:'" + e.getId() + "', user: '" + e.getUser() + "', message: '" + e.getMessage() + "'", e);
    }

    @Data
    @AllArgsConstructor
    static class ExceptionInfo {
        @JsonProperty("error_code")
        private int errorCode;
        @JsonProperty("message")
        private String message;
        @JsonProperty("guid")
        private String id;
        @JsonProperty("user")
        private String user;
    }
}
