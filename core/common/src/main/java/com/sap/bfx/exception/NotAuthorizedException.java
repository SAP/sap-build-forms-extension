package com.sap.bfx.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Exception for not authorized access
 */
@EqualsAndHashCode(callSuper = true)
public class NotAuthorizedException extends FormsCoreException {

    @Getter
    private String appName;
    @Getter
    private String[] roles;
    @Getter
    private String type;

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

    /**
     * Constructor
     *
     * @param appName application name
     * @param type    type
     * @param user    user
     */
    public NotAuthorizedException(String appName, String type, String user) {
        super();
        this.appName = appName;
        this.type = type;
        this.user = user;
    }
}
