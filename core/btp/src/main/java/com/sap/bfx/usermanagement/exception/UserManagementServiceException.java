package com.sap.bfx.usermanagement.exception;

import java.io.Serial;

public class UserManagementServiceException extends Exception {

	@Serial
	private static final long serialVersionUID = 1L;

	public UserManagementServiceException() {
		super();
	}

	public UserManagementServiceException(String message) {
		super(message);
	}

	public UserManagementServiceException(Throwable cause) {
		super(cause);
	}

	public UserManagementServiceException(String message, Throwable cause) {
		super(message, cause);
	}

}