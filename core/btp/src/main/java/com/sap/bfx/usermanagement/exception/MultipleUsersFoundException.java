package com.sap.bfx.usermanagement.exception;

import lombok.Getter;

import java.io.Serial;
import java.text.MessageFormat;

@Getter
public class MultipleUsersFoundException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String userId;
    private final String ids;

    public MultipleUsersFoundException(String userId, String ids) {
        super(getExceptionMessage(userId, ids));
        this.userId = userId;
        this.ids = ids;
    }

    @Override
    public String getMessage() {
        return getExceptionMessage(userId, ids);
    }

    private static String getExceptionMessage(String userId, String ids) {
        return MessageFormat.format("Multiple users found with userId {0} and ids {1}", userId, ids);
    }

}
