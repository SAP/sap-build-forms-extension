package com.sap.bfx.p13n.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * This class defines the granted authorities for the P13N (Personalization) application.
 * Each authority corresponds to a specific permission or role within the application.
 */
public final class P13NGrantedAuthorities {

    public static final GrantedAuthority SBFX_P13NEdit = new SimpleGrantedAuthority("SBFX_P13NEdit");
    public static final GrantedAuthority SBFX_P13NDisplay = new SimpleGrantedAuthority("SBFX_P13NDisplay");
    public static final GrantedAuthority SBFX_P13NEnduser = new SimpleGrantedAuthority("SBFX_P13NEnduser");
    public static final GrantedAuthority SBFX_P13NGrpcUsage = new SimpleGrantedAuthority("SBFX_P13NGrpcUsage");

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private P13NGrantedAuthorities() {
    }

}
