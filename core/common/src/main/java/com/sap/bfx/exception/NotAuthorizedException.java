package com.sap.bfx.exception;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Exception for not authorized access
 */
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NotAuthorizedException extends FormsCoreException {

    @Getter
    private String appName;
    @Getter
    private String[] roles;

    /**
     * Constructor
     *
     * @param appName application name
     * @param roles   roles
     * @param user    user
     */
    public NotAuthorizedException(String appName, String[] roles, String user) {
        super();

        this.appName = appName;
        this.roles = roles;
        this.user = user;
    }
}
