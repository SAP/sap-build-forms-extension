package com.sap.bfx.usermanagement.exception;

import java.io.Serial;

public class UserManagementRuntimeException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = 1L;

	public UserManagementRuntimeException(String message) {
		super(message);
	}

	public UserManagementRuntimeException(String message, Throwable throwable) {
		super(message, throwable);
	}

}
