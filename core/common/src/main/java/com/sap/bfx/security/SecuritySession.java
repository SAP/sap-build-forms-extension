package com.sap.bfx.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SecuritySession {

    public static final String DUMMY_SESSION_ID = "_";

    private String id;
    private User user;
    private String tokenValue;
    @JsonIgnore
    private Jwt token;

    /**
     * Creates a dummy SecuritySession with a predefined dummy session ID.
     *
     * @return a new SecuritySession instance representing a dummy session
     */
    public static SecuritySession createDummy() {
        return new SecuritySession(DUMMY_SESSION_ID, null, null, null);
    }

    /**
     * Decodes the JWT token using the provided JwtDecoder if the token is not already decoded.
     *
     * @param jwtDecoder the JwtDecoder to use for decoding the token
     * @throws JwtException if the token cannot be decoded
     */
    public final void decodeToken(@NonNull JwtDecoder jwtDecoder) throws JwtException {
        if (token == null && tokenValue != null) {
            token = jwtDecoder.decode(tokenValue);
        }
    }

    /**
     * Checks if the session is a dummy session.
     *
     * @return true if the session ID equals the dummy session ID, false otherwise
     */
    public boolean isDummy() {
        return DUMMY_SESSION_ID.equals(id);
    }
}
