package com.sap.bfx.valuehelp.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * This class defines the granted authorities for the Value Help application.
 * Each authority corresponds to a specific permission or role within the application.
 */
public final class ValueHelpGrantedAuthorities {

    public final static GrantedAuthority SBFX_ValueHelpEdit = new SimpleGrantedAuthority("SBFX_ValueHelpEdit");
    public final static GrantedAuthority SBFX_ValueHelpDisplay = new SimpleGrantedAuthority("SBFX_ValueHelpDisplay");
    public final static GrantedAuthority SBFX_ValueHelpGrpcUsage =
            new SimpleGrantedAuthority("SBFX_ValueHelpGrpcUsage");

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private ValueHelpGrantedAuthorities() {
    }
}
