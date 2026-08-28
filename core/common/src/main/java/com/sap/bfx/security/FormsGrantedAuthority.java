package com.sap.bfx.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * This class defines the granted authorities for the Forms application.
 * Each authority corresponds to a specific permission or role within the application.
 */
public final class FormsGrantedAuthority {

    public final static GrantedAuthority SBFX_StartProcess = new SimpleGrantedAuthority("SBFX_StartProcess");
    public final static GrantedAuthority SBFX_ParticipateProcess =
            new SimpleGrantedAuthority("SBFX_ParticipateProcess");
    public final static GrantedAuthority SBFX_SearchProcess = new SimpleGrantedAuthority("SBFX_SearchProcess");
    public final static GrantedAuthority SBFX_SeeAfterStart = new SimpleGrantedAuthority("SBFX_SeeAfterStart");
    public final static GrantedAuthority SBFX_TechnicalOwner = new SimpleGrantedAuthority("SBFX_TechnicalOwner");
    public final static GrantedAuthority SBFX_BusinessOwner = new SimpleGrantedAuthority("SBFX_BusinessOwner");
    public final static GrantedAuthority SBFX_FireFighter = new SimpleGrantedAuthority("SBFX_FireFighter");

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private FormsGrantedAuthority() {
    }
}
