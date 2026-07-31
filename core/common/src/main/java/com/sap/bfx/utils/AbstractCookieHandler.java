package com.sap.bfx.utils;

import jakarta.annotation.Nullable;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.SerializationUtils;

import java.io.Serializable;
import java.util.Base64;

public abstract class AbstractCookieHandler {
    /**
     * Retrieves a cookie by name from the request.
     *
     * @param request the HttpServletRequest from which to retrieve the cookie
     * @param name    the name of the cookie to retrieve
     * @return the Cookie object if found, or null if not found
     */
    @Nullable
    public static Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) return cookie;
            }
        }
        return null;
    }

    /**
     * Creates a cookie with the specified parameters.
     *
     * @param request  the HttpServletRequest to determine if the request is secure
     * @param name     the name of the cookie
     * @param value    the value of the cookie
     * @param httpOnly whether the cookie is HTTP-only
     * @param secure   whether the cookie should be secure
     * @param expiry   the maximum age of the cookie in seconds
     * @return a configured Cookie object
     */
    protected Cookie createCookie(HttpServletRequest request, String name, String value, boolean httpOnly,
                                  boolean secure, int expiry) {
        final var cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(httpOnly);
        if (secure) {
            cookie.setSecure(request.isSecure());
        }
        cookie.setMaxAge(expiry);

        return cookie;
    }

    /**
     * Adds a cookie to the response with the specified parameters.
     *
     * @param request  the HttpServletRequest to determine if the request is secure
     * @param response the HttpServletResponse to which the cookie will be added
     * @param name     the name of the cookie
     * @param value    the value of the cookie
     * @param httpOnly whether the cookie is HTTP-only
     * @param secure   whether the cookie should be secure
     * @param expiry   the maximum age of the cookie in seconds
     */
    protected void addCookie(HttpServletRequest request, HttpServletResponse response, String name, String value,
                             boolean httpOnly, boolean secure, int expiry) {
        final var cookie = createCookie(request, name, value, httpOnly, secure, expiry);
        response.addCookie(cookie);
    }

    /**
     * Encodes an object to a Base64 string.
     *
     * @param value the object to encode
     * @return a Base64-encoded string representation of the object
     */
    protected String encodeValue(Serializable value) {
        try {
            final var bytes = SerializationUtils.serialize(value);
            return Base64.getUrlEncoder().encodeToString(bytes);
        } catch (Exception e) {
            // TODO: Use throw FormsCoreException.from(e); instead
            throw new RuntimeException("Failed to encode value to Base64", e);
        }
    }

    /**
     * Decodes a Base64 string to an object of the specified class.
     *
     * @param value the Base64-encoded string to decode
     * @param clz   the class of the object to decode to
     * @param <T>   the type of the object
     * @return an object of type T decoded from the Base64 string
     */
    protected <T> T decodeValue(String value, Class<T> clz) {
        try {
            final var bytes = Base64.getUrlDecoder().decode(value);
            return (T) SerializationUtils.deserialize(bytes);
        } catch (Exception e) {
            // TODO: Use throw FormsCoreException.from(e); instead
            throw new RuntimeException("Failed to decode value from Base64", e);
        }
    }
}
