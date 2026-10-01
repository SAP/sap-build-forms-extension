package com.sap.bfx.exception;

import com.sap.bfx.security.SecurityUtils;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Base exception for all exceptions in Forms Core
 */
@EqualsAndHashCode(callSuper = true)
public class FormsCoreException extends RuntimeException {
    @Getter
    private final String id;
    @Getter
    private final Map<String, Object> details = new HashMap<>();
    @Getter
    protected String user;

    /**
     * Constructor
     */
    public FormsCoreException() {
        this(null, null);
    }

    /**
     * Constructor
     *
     * @param message message
     */
    public FormsCoreException(String message) {
        this(message, null);
    }

    /**
     * Constructor
     *
     * @param base base exception
     */
    public FormsCoreException(Throwable base) {
        this(base.getMessage(), base);
    }

    /**
     * Constructor
     *
     * @param message message
     * @param base    base exception
     */
    public FormsCoreException(String message, Throwable base) {
        super(message == null ? "" : message, base);
        user = SecurityUtils.getUserName();
        id = UUID.randomUUID().toString();
    }

}
