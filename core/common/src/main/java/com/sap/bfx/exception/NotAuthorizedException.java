package com.sap.bfx.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Exception for not authorized access
 */
@EqualsAndHashCode(callSuper = true)
public class NotAuthorizedException extends FormsCoreException {

    @Getter
    private String appName;
    @Getter
    private Collection<GrantedAuthority> authObjects;
    @Getter
    private String type;

    /**
     * Constructor
     *
     * @param appName     application name
     * @param authObjects authObjects
     * @param user        user
     */
    public NotAuthorizedException(String appName, Collection<GrantedAuthority> authObjects, String user) {
        super();
        this.authObjects = authObjects;
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

    /**
     * Constructor
     *
     * @param authObjects authObjects
     * @param user        user
     */
    public NotAuthorizedException(Collection<GrantedAuthority> authObjects, String user) {
        super();
        this.authObjects = authObjects;
        this.user = user;
    }

    /**
     * Constructor
     *
     * @param type type
     * @param user user
     */
    public NotAuthorizedException(String type, String user) {
        super();
        this.type = type;
        this.user = user;
    }
}
