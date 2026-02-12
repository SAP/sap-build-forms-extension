package com.sap.bfx.usermanagement.exception;

import java.io.Serial;

public class IllegalQueryException extends UserManagementServiceException {

	@Serial
	private static final long serialVersionUID = 1L;

	public IllegalQueryException(String message) {
		super(message);
	}

}